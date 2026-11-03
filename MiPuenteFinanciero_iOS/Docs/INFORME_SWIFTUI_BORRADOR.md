# Mi Puente Financiero
## Microproyecto SwiftUI

**Universidad del Cauca**

**Asignatura**: Electiva Desarrollo de Aplicaciones Móviles

**Docente**: Ph.D. Cristhian Figueroa

**Integrantes**: Guillermo Adolfo Cuéllar Fonseca, José Luis Sánchez López y Yennifer Yuliana Uni Itaz

**Periodo académico**: 2026-2

> Este borrador tiene los apartados solicitados por el docente. Antes de entregarlo es obligatorio añadir las capturas reales generadas en Xcode, la fotografía del diagrama dibujado a mano y la URL definitiva del repositorio. Verifica también que las funciones descritas operen correctamente en el simulador.

# 1. Enlace al repositorio Git

Repositorio compartido de trabajo: https://github.com/yulianaitaz/AplicacionesMoviles

**Pendiente de verificar**: Confirmar que el directorio SwiftUI está efectivamente publicado y que aparecen las contribuciones y los Pull Requests de cada integrante.

# 2. Capturas de pantalla

Las capturas deben obtenerse directamente desde el simulador de iPhone, mediante Xcode, para documentar el resultado implementado y evitar confundir imágenes promocionales con evidencia de funcionamiento.

| Figura | Pantalla | Evidencia que debe mostrar |
|---|---|---|
| 1 | Inicio | Saldo, selector Personal/Negocio, meta y tasa de cambio |
| 2 | Movimientos | Búsqueda, filtros y lista de registros |
| 3 | Registrar movimiento | Teclado numérico, categoría y opciones de ingreso/gasto |
| 4 | Metas de ahorro | Metas registradas y progreso calculado |
| 5 | Perfil | Entorno seleccionado y preferencia de notificaciones |
| 6 | Acerca de y créditos | Nombre de la aplicación e integrantes |

**Pendiente**: Insertar las seis imágenes reales, numerarlas y acompañarlas de una descripción breve.

# 3. Descripción de la arquitectura y componentes

## Diagrama arquitectónico

**Pendiente obligatorio**: Dibujar el esquema en una hoja de papel, fotografiarlo y pegarlo aquí. El archivo `GUIA_ENTREGA.md` contiene una propuesta precisa de los componentes y las conexiones que deben figurar en ese dibujo.

## Interfaz y navegación

La interfaz se implementa de manera declarativa con SwiftUI. La navegación principal está organizada mediante `TabView`, que reúne Inicio, Movimientos, Añadir, Metas y Perfil. Dentro de Perfil, `NavigationStack` permite entrar a la pantalla de descripción y créditos. Las vistas utilizan controles propios de SwiftUI, incluyendo `TextField`, `Button`, `Toggle`, `Form`, `Picker` y `ProgressView`.

Conservamos la identidad visual de Mi Puente Financiero, con verde azulado, tonos menta y acentos de color para diferenciar ingresos y gastos. La selección de Personal y Negocio sigue presente en las pantallas donde es necesaria. Esto permite relacionar cada movimiento con su entorno y mostrar el saldo correspondiente.

## ViewModel y gestión del estado

`FinanceViewModel` implementa `ObservableObject` y utiliza propiedades `@Published`. Allí se concentra el estado necesario para dibujar las pantallas, como el entorno activo, los saldos, el borrador del registro, las metas y los mensajes del servicio remoto. Las pantallas observan la misma instancia a través de `@ObservedObject`. El ViewModel recibe las acciones del usuario, valida los datos, se comunica con el repositorio y publica el estado actualizado para que SwiftUI refresque la interfaz.

En los formularios de edición y creación de metas se emplea `@State` para manejar campos temporales. El componente de selección de entorno utiliza `@Binding`, de manera que puede reflejar y modificar el valor administrado por su vista superior.

## Persistencia local

El almacenamiento se implementa con SwiftData, que ofrece persistencia de modelos en el dispositivo mediante `ModelContainer` y `ModelContext`. El proyecto define los modelos `FinanceMovement`, `SavingsGoalRecord` y `FinanceSetting`. `FinanceRepository` organiza las operaciones de consulta, inserción, modificación y eliminación, y mantiene los saldos coherentes al registrar o retirar movimientos.

En el primer arranque se incluyen datos de ejemplo para facilitar la demostración. Ese contenido inicial se crea una sola vez gracias a una marca guardada en la base. Los movimientos que registre el usuario y sus metas personalizadas permanecen después de cerrar y volver a abrir la aplicación, siempre que los datos de la app no se eliminen del dispositivo.

Las metas permiten ingresar el nombre y el objetivo, registrar aportes al ahorro y visualizar automáticamente el porcentaje alcanzado. La preferencia de notificaciones también se guarda de forma local. En esta versión la preferencia se limita al almacenamiento y presentación del ajuste.

## Servicio en línea

La capa de red utiliza `URLSession` para consultar por HTTPS el endpoint público `open.er-api.com/v6/latest/USD`. La respuesta es decodificada con `JSONDecoder`, y el valor requerido se obtiene de la propiedad `rates.COP`. El ViewModel maneja los estados de carga, éxito y error, permitiendo que la interfaz informe si la consulta falló por falta de conexión u otro problema. La tasa mostrada tiene carácter de referencia y se distingue de la TRM oficial.

La consulta remota es independiente del almacenamiento de movimientos y metas. Así, una falla del servicio público no impide registrar gastos o consultar la información persistente.

# 4. Reflexión comparativa entre SwiftUI y Jetpack Compose

El desarrollo de Mi Puente Financiero en dos plataformas permitió comprender mejor las similitudes y las particularidades de sus entornos. En Android construimos las pantallas con Kotlin y Jetpack Compose, mientras que en iOS utilizamos Swift y SwiftUI. Ambas herramientas permiten expresar la interfaz a partir de componentes y estados, lo que facilitó reconocer estructuras equivalentes al trasladar la aplicación.

Una diferencia práctica surgió en la organización del estado. En Android trabajamos con un `FinanceViewModel` y `StateFlow` para publicar cambios. En SwiftUI implementamos `ObservableObject`, `@Published`, `@ObservedObject` y `@Binding`. Aunque la sintaxis cambia, la necesidad de mantener la lógica fuera de las pantallas se conserva. El desarrollo del ViewModel en ambas plataformas ayudó a que los cambios de saldos y movimientos se reflejaran de forma coherente.

La persistencia también exigió adaptar nuestras decisiones. La versión Android empleaba SQLite mediante `SQLiteOpenHelper` y una capa de repositorio. En iOS optamos por SwiftData, que integra el modelado de entidades y su almacenamiento persistente. Mantuvimos el repositorio como punto de acceso a los datos, lo que permitió conservar un flujo de trabajo ordenado entre interfaz, lógica y almacenamiento.

En navegación, el patrón de cinco destinos resultó trasladable, aunque los componentes concretos fueron diferentes. Android utilizaba Navigation Component, mientras que iOS se apoya en `TabView` y `NavigationStack`. También aprovechamos los controles nativos de SwiftUI para que los formularios y las hojas de edición respondieran de una manera familiar para el usuario de iPhone.

El proceso sirvió para confirmar que una buena separación de responsabilidades facilita adaptar una misma idea de producto a distintos sistemas. Al mismo tiempo, cada plataforma ofrece herramientas y convenciones propias que conviene respetar. El resultado conserva el propósito de Mi Puente Financiero y adapta su experiencia de uso al entorno iOS.

# 5. Funcionalidades adicionales

Esta versión incorpora funciones que amplían el alcance de la entrega Android anterior, principalmente la creación de metas con nombre y objetivo personalizados, la edición de metas, el registro de aportes y la eliminación de objetivos. También permite eliminar movimientos de manera controlada y recalcular el saldo según la operación eliminada.

Estas funcionalidades se proponen para valoración como características adicionales, de acuerdo con la complejidad que determine el docente. Su inclusión no supone que automáticamente se concedan décimas adicionales.

# Conclusión

Mi Puente Financiero para iOS reúne las funciones centrales de control financiero personal y de negocio en una aplicación con interfaz nativa, navegación estructurada, almacenamiento persistente y consumo de un servicio remoto. El ejercicio permitió aplicar la arquitectura MVVM y comprender las equivalencias y particularidades de SwiftUI frente a Jetpack Compose. Las pruebas finales en Xcode y la documentación de evidencias permitirán verificar el cumplimiento de los requisitos del taller.
