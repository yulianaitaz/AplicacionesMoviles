# Plan de Pruebas y Evidencias de Persistencia Local (SwiftData)
## Mi Puente Financiero – iOS

**Estudiante:** José Luis Sánchez López (`joselsanchez@unicauca.edu.co`)  
**Asignatura:** Electiva Desarrollo de Aplicaciones Móviles  
**Docente:** Ph.D. Cristhian Figueroa  
**Institución:** Universidad del Cauca – Departamento de Telemática  
**Área de Contribución Individual:** Persistencia local con SwiftData  
**Rama de trabajo:** `feature/ios-persistencia-jose`  

---

## 1. Introducción y Objetivos

Este documento describe la arquitectura de persistencia, las reglas de integridad de datos, las mejoras implementadas y el plan de pruebas reproducibles para el módulo de persistencia local de la aplicación **Mi Puente Financiero** en su versión nativa para iOS construida con SwiftUI y SwiftData.

### Objetivos específicos
1. Documentar los modelos de datos persistentes (`FinanceMovement`, `SavingsGoalRecord`, `FinanceSetting`) y su ciclo de vida en disco.
2. Responder con rigurosidad técnica las preguntas clave de sustentación sobre SwiftData (`ModelContainer`, `ModelContext`, macro `@Model` e integridad transaccional de saldos).
3. Detallar las mejoras introducidas al repositorio (`FinanceRepository.swift`) y modelos (`FinanceModels.swift`) para saneamiento de entradas y consistencia de datos.
4. Definir y evidenciar los casos de prueba reproducibles para registro de transacciones, cálculo y reversión de saldos, persistencia ante cierre y reapertura de la app, y administración de metas de ahorro.

---

## 2. Modelos de Persistencia (SwiftData)

La persistencia local se gestiona mediante el framework **SwiftData** de Apple (disponible a partir de iOS 17), el cual provee un mapeo objeto-relacional declarativo sobre un almacenamiento subyacente estructurado (SQLite).

### 2.1. `FinanceMovement`
Representa cada movimiento financiero (ingreso o gasto) registrado por el usuario.

* **Atributos:**
  * `id: UUID`: Identificador único universal del movimiento.
  * `title: String`: Concepto o descripción textual del movimiento (ej. *"Supermercado Líder"*).
  * `category: String`: Categoría a la que pertenece (ej. *"Súper"*, *"Transporte"*, *"Comida"*, *"Ventas"*).
  * `amount: Int64`: Monto monetario en centavos/enteros COP. Se utiliza `Int64` para evitar errores de precisión asociados a números de punto flotante (`Double`).
  * `kindRaw: String`: Almacena el valor serializado del tipo de movimiento (`"INCOME"` o `"EXPENSE"`).
  * `accountRaw: String`: Almacena la cuenta asociada (`"PERSONAL"` o `"BUSINESS"`).
  * `createdAt: Date`: Marca temporal en que se originó el registro, utilizada para la ordenación cronológica descendente.
* **Propiedades computadas:**
  * `kind: TransactionKind`: Proyección tipada (`.income` o `.expense`) con fallback seguro.
  * `account: FinanceAccount`: Proyección tipada (`.personal` o `.business`).
  * `signedAmount: Int64`: Retorna el valor firmado: `+amount` si es ingreso o `-amount` si es gasto.

### 2.2. `SavingsGoalRecord`
Representa una meta de ahorro definida por el usuario.

* **Atributos:**
  * `id: UUID`: Identificador único de la meta.
  * `name: String`: Nombre descriptivo de la meta (ej. *"Viaje a Cartagena"*).
  * `saved: Int64`: Dinero acumulado a la fecha mediante aportes.
  * `target: Int64`: Meta financiera objetivo a alcanzar.
  * `emoji: String`: Ícono visual representativo.
  * `createdAt: Date`: Fecha de creación de la meta.
* **Propiedades computadas:**
  * `progress: Double`: Porcentaje normalizado de avance entre 0.0 y 1.0 (`min(1, max(0, saved / target))`).
  * `remaining: Int64`: Monto restante exacto para cumplir el objetivo (`max(0, target - saved)`).
  * `isCompleted: Bool`: Bandera booleana que indica si se alcanzó o superó el objetivo (`saved >= target`).

### 2.3. `FinanceSetting`
Almacena pares clave-valor para la configuración global y el estado financiero consolidado.

* **Atributos:**
  * `key: String`: Clave única con anotación `@Attribute(.unique)`. Claves utilizadas:
    * `"seeded_v1"`: Marcador de control para sembrado inicial de datos demo.
    * `"personal_balance"`: Saldo consolidado actual de la cuenta personal en COP.
    * `"business_balance"`: Saldo consolidado actual de la cuenta de negocio en COP.
    * `"notifications_enabled"`: Preferencia de notificaciones activas (`"true"` / `"false"`).
  * `value: String`: Valor textual persistido.

---

## 3. Preguntas de Sustentación Teórica y Arquitectura

### 3.1. ¿Qué diferencia hay entre `ModelContainer` y `ModelContext`?

| Componente | Rol Principal | Nivel / Ámbito | Responsabilidad técnica |
|---|---|---|---|
| **`ModelContainer`** | Contenedor y Gestor del Almacenamiento | Nivel Aplicación (`App` / `WindowGroup`) | Define el esquema completo de las entidades (`[FinanceMovement.self, SavingsGoalRecord.self, FinanceSetting.self]`), inicializa el motor SQLite subyacente, gestiona la ubicación de los archivos en disco y coordina las migraciones de esquemas. |
| **`ModelContext`** | Espacio de Trabajo en Memoria (*Scratchpad*) | Nivel Vistas y Repositorio (`@MainActor`) | Rastrea los cambios de los objetos en memoria (inserciones, eliminaciones y mutaciones). Trabaja vinculado a un actor. Ningún cambio se vuelve persistente en disco hasta que se invoca explícitamente `context.save()`. |

En `MiPuenteFinancieroApp.swift`, `.modelContainer(for: [...])` crea el contenedor central, mientras que `@Environment(\.modelContext)` inyecta el contexto en memoria para ser consumido por `FinanceRepository`.

### 3.2. ¿Qué representa la anotación `@Model`?

La anotación `@Model` es una macro de Swift (Swift Syntax Macro) introducida en iOS 17 que transforma una clase estándar de Swift en un modelo persistente gestionado por SwiftData:
1. **Conformidad automática:** Hace que la clase conforme los protocolos `PersistentModel` y `Observable`.
2. **Dirty Tracking & Faulting:** Intercepta dinámicamente los accesos de lectura y escritura a las propiedades para detectar cuándo un valor ha sido modificado y debe guardarse, cargando datos bajo demanda desde la base de datos solo cuando se necesitan.
3. **Mapeo relacional:** Genera los metadatos necesarios para que el `ModelContainer` cree las tablas, columnas, restricciones de clave foránea e índices correspondientes en el archivo SQLite.
4. **Reactividad en SwiftUI:** Al conformar `Observable`, permite que cualquier vista SwiftUI que observe una instancia de `@Model` se redibuje automáticamente cuando sus valores cambian.

### 3.3. ¿Cómo mantiene el repositorio coherente el saldo al borrar un movimiento?

El saldo no se recalcula escaneando toda la tabla en cada operación; se mantiene de forma transaccional mediante aritmética segura con reversión:

1. **Determinación del impacto contable original:** Cada entidad `FinanceMovement` conoce su propiedad `signedAmount`:
   * Ingreso (`.income`): `+amount`
   * Gasto (`.expense`): `-amount`
2. **Cálculo de la operación inversa:** Al eliminar un registro en `deleteMovement(movement)`:
   $$\text{Ajuste} = - (\text{movement.signedAmount})$$
   * Si se elimina un **gasto** de $\$50.000$, el ajuste es $-(-50.000) = +50.000$. El dinero gastado se **reembolsa** al saldo.
   * Si se elimina un **ingreso** de $\$850.000$, el ajuste es $-(+850.000) = -850.000$. El dinero percibido se **descuenta** del saldo.
3. **Seguridad aritmética:** La operación se ejecuta mediante `currentBalance.addingReportingOverflow(adjustment)`. Si ocurriera un desbordamiento en enteros de 64 bits, la operación se cancela lanzando `FinanceDataError.invalidAmount`.
4. **Atomicidad de la transacción:** Se actualiza el registro en `FinanceSetting` con el nuevo saldo y se ejecuta `context.delete(movement)`. Al llamar `try context.save()`, SwiftData confirma en una única transacción atómica tanto la eliminación del registro como la actualización del saldo. Si el guardado falla, ninguna de las dos modificaciones se aplica en disco.

---

## 4. Mejoras Implementadas en la Capa de Persistencia

En la rama `feature/ios-persistencia-jose` se implementaron las siguientes mejoras técnicas sobre el código base:

1. **Saneamiento y validación estricta en `FinanceRepository.addMovement`:**
   * Se aplicó limpieza de espacios en blanco (`trimmingCharacters(in: .whitespacesAndNewlines)`) para el título y la categoría.
   * Se añadieron validaciones que impiden registrar movimientos con conceptos vacíos o constituidos solo por espacios (`FinanceDataError.invalidMovementTitle` e `invalidMovementCategory`).
2. **Ampliación de `FinanceDataError`:**
   * Se incorporaron mensajes localizados y descriptivos para los nuevos casos de validación de entradas erróneas.
3. **Consultas de progreso en `SavingsGoalRecord`:**
   * Se agregaron las propiedades computadas `remaining` (monto restante para completar la meta) e `isCompleted` (verificación de meta alcanzada), facilitando consultas limpias desde la UI sin duplicación de lógica matemática.

---

## 5. Matriz y Evidencias de Casos de Prueba

Las pruebas fueron diseñadas para verificar el comportamiento determinístico de la persistencia local.

### Caso de Prueba CP-01: Registro de gasto y actualización de saldo
* **Objetivo:** Verificar que al registrar un nuevo gasto se descuente exactamente del saldo de la cuenta activa y se inserte en la base de datos local.
* **Precondición:** Cuenta Personal seleccionada con saldo inicial de $\$2.500.000 \text{ COP}$.
* **Datos de entrada:**
  * Tipo: Gasto (`.expense`)
  * Categoría: `"Comida"`
  * Nota: `"Almuerzo con compañeros"`
  * Monto: $\$50.000 \text{ COP}$
* **Procedimiento:**
  1. Ir a la pestaña **Añadir**.
  2. Digitar el monto `50000` con el teclado numérico interactivo.
  3. Seleccionar categoría `"Comida"` y escribir la nota.
  4. Presionar el botón **"Registrar gasto"**.
  5. Navegar a **Inicio** y **Movimientos**.
* **Resultado Esperado:**
  * El saldo de la cuenta personal disminuye exactamente a $\$2.450.000 \text{ COP}$.
  * El movimiento aparece encabezando el historial en **Movimientos** con badge rojo de gasto y fecha actual.
  * La entidad queda guardada en SwiftData tras `context.save()`.

---

### Caso de Prueba CP-02: Continuidad y persistencia tras reinicio de la aplicación
* **Objetivo:** Garantizar que los datos modificados se mantengan intactos tras el cierre completo del proceso y su reapertura (persistencia real en disco).
* **Precondición:** Haber ejecutado el Caso CP-01 (Saldo: $\$2.450.000 \text{ COP}$, movimiento de $\$50.000$ existente).
* **Procedimiento:**
  1. Forzar el cierre de la aplicación desde el conmutador de aplicaciones del simulador (deslizar hacia arriba para terminar el proceso de ejecución).
  2. Detener la sesión en Xcode para asegurar que la memoria RAM del proceso sea liberada.
  3. Abrir nuevamente la aplicación desde la pantalla de inicio del simulador.
  4. Observar la pantalla **Inicio** y consultar **Movimientos**.
* **Resultado Esperado:**
  * El marcador `"seeded_v1"` evita que los datos demo sobreescriban los datos del usuario.
  * El saldo reflejado inmediatamente es $\$2.450.000 \text{ COP}$.
  * El gasto de $\$50.000$ persiste en la lista con su identificador UUID y marca temporal intactos.

---

### Caso de Prueba CP-03: Eliminación de movimiento y reversión de saldo
* **Objetivo:** Verificar que al borrar un movimiento del historial, la base de datos lo elimine físicamente y su saldo se revierta con precisión matemática.
* **Precondición:** Saldo de $\$2.450.000 \text{ COP}$ con el gasto de $\$50.000 \text{ COP}$ registrado en CP-01.
* **Procedimiento:**
  1. Ingresar a la pestaña **Movimientos**.
  2. Localizar el movimiento *"Almuerzo con compañeros"* de $\$50.000 \text{ COP}$.
  3. Presionar el botón de eliminar (ícono de papelera).
  4. Observar la actualización de la lista y volver a la pantalla **Inicio**.
* **Resultado Esperado:**
  * El registro desaparece inmediatamente de la interfaz y es eliminado del `ModelContext`.
  * Se aplica la reversión contable: $\$2.450.000 + 50.000 = \$2.500.000 \text{ COP}$.
  * El saldo en **Inicio** regresa exactamente a $\$2.500.000 \text{ COP}$.

---

### Caso de Prueba CP-04: Creación de meta de ahorro y aportes progresivos
* **Objetivo:** Comprobar la creación, almacenamiento y acumulación de aportes en metas de ahorro (`SavingsGoalRecord`).
* **Precondición:** Pestaña **Metas** abierta.
* **Datos de entrada:**
  * Nombre: `"Curso Certificación Cloud"`
  * Objetivo: $\$1.000.000 \text{ COP}$
  * Emoji: `"☁️"`
  * Aporte 1: $\$200.000 \text{ COP}$
  * Aporte 2: $\$300.000 \text{ COP}$
* **Procedimiento:**
  1. Abrir el formulario para crear nueva meta.
  2. Ingresar los datos de la meta y presionar guardar.
  3. Verificar que aparezca en la lista con progreso $0\%$.
  4. Realizar un abono de $\$200.000 \text{ COP}$. Verificar que el monto guardado sea $\$200.000$ ($20\%$).
  5. Realizar un segundo abono de $\$300.000 \text{ COP}$.
* **Resultado Esperado:**
  * La meta persiste en SQLite con `saved = 500000`, `target = 1000000`.
  * La propiedad `progress` calcula exactamente $0.5$ ($50\%$).
  * La propiedad `remaining` calcula $\$500.000 \text{ COP}$.

---

### Caso de Prueba CP-05: Validación de casos límite y manejo de errores
* **Objetivo:** Asegurar que valores anómalos no corrompan la integridad de la base de datos local.
* **Escenarios probados:**
  1. **Monto cero o negativo:** Intentar agregar un movimiento con monto `0`.
     * *Resultado:* El repositorio lanza `FinanceDataError.invalidAmount`; la app no inserta nada y presenta alerta amigable al usuario.
  2. **Concepto en blanco:** Intentar ingresar una nota compuesta únicamente por espacios en blanco (`"   "`).
     * *Resultado:* Gracias a la mejora implementada, la función sanea el texto y aplica el título por defecto según la categoría o lanza `FinanceDataError.invalidMovementTitle`, evitando registros mudos en la base de datos.
  3. **Meta con objetivo negativo o nombre vacío:**
     * *Resultado:* El repositorio lanza `FinanceDataError.invalidGoal`.

---

## 6. Entorno de Pruebas y Especificaciones

* **Dispositivo de prueba:** Simulador iOS – iPhone 16
* **Sistema Operativo:** iOS 18.0 / iOS 18.2
* **Entorno de desarrollo:** Xcode 16.0
* **Framework de Persistencia:** Apple SwiftData (`import SwiftData`)
* **Base de datos subyacente:** SQLite gestionado por CoreData/SwiftData en el contenedor de la aplicación (`Application Support/default.store`).

---

## 7. Conclusiones

1. **Robustez declarativa:** SwiftData reduce significativamente el código boilerplate comparado con CoreData tradicional o SQLite crudo en Android, manteniendo una integración nativa con el ciclo de vida de SwiftUI.
2. **Integridad referencial y transaccional:** La combinación de `@MainActor` en `FinanceRepository` con guardados explícitos (`try context.save()`) asegura que tanto los movimientos como los saldos asociados mantengan consistencia ACID en todo momento.
3. **Mantenibilidad:** La arquitectura desacoplada mediante Repository Pattern permite que las vistas y el `FinanceViewModel` consuman los datos sin depender directamente de los detalles de implementación del almacenamiento.
