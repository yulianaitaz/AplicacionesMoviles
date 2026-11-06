# Plan de pruebas del ViewModel y consumo de API – Mi Puente Financiero iOS

## 1. Objetivo

Este documento describe los casos de prueba propuestos para verificar el comportamiento del módulo `FinanceViewModel` y del servicio `ExchangeRateService` de la aplicación Mi Puente Financiero para iOS.

El propósito es documentar cómo se espera que respondan estos componentes ante diferentes operaciones de la aplicación, incluyendo la gestión de movimientos financieros, metas de ahorro, saldos, preferencias de notificaciones y consulta de la tasa de cambio USD/COP.

Este documento constituye una planificación y evidencia documental de las pruebas. Los resultados descritos como esperados no deben interpretarse como pruebas ejecutadas en un dispositivo o simulador.

## 2. Pruebas del ViewModel

El ViewModel participa en la gestión del estado de la aplicación y coordina las operaciones entre las vistas y la capa de persistencia.

### 2.1. Carga inicial de información

**Objetivo:** comprobar que el ViewModel inicialice la información almacenada localmente.

**Procedimiento propuesto:**
1. Inicializar el ViewModel con el repositorio de persistencia.
2. Solicitar la carga de la información local.
3. Verificar los movimientos, las metas de ahorro, los saldos y la configuración de notificaciones.

**Resultado esperado:** la información local debe cargarse correctamente y el estado de carga debe indicar que los datos están disponibles. Si ocurre un error, la aplicación debe informar que no fue posible cargar la información local.

### 2.2. Selección de cuenta y consulta de saldo

**Objetivo:** verificar que el saldo mostrado corresponda a la cuenta seleccionada.

**Procedimiento propuesto:**
1. Seleccionar la cuenta personal.
2. Consultar el saldo activo.
3. Cambiar a la cuenta de negocio.
4. Consultar nuevamente el saldo.

**Resultado esperado:** el saldo activo debe corresponder a la cuenta seleccionada, sin mezclar los valores de las dos cuentas.

### 2.3. Registro de movimientos financieros

**Objetivo:** comprobar el registro de ingresos y gastos.

**Procedimiento propuesto:**
1. Seleccionar una cuenta.
2. Introducir un valor y una categoría.
3. Registrar un movimiento de ingreso.
4. Registrar un movimiento de gasto.
5. Consultar el historial de movimientos.

**Resultado esperado:** los movimientos deben guardarse en la persistencia local, aparecer en el historial correspondiente y reflejarse en el saldo de la cuenta seleccionada.

También se debe verificar que los valores inválidos o los datos incompletos no produzcan registros incorrectos.

### 2.4. Eliminación de movimientos

**Objetivo:** comprobar que un movimiento pueda eliminarse correctamente.

**Procedimiento propuesto:**
1. Seleccionar un movimiento existente.
2. Solicitar su eliminación.
3. Consultar nuevamente el historial.
4. Verificar el saldo de la cuenta.

**Resultado esperado:** el movimiento eliminado no debe aparecer en el historial y el saldo debe reflejar correctamente la operación inversa correspondiente.

### 2.5. Creación y administración de metas de ahorro

**Objetivo:** verificar la gestión de metas de ahorro.

**Procedimiento propuesto:**
1. Introducir el nombre de una meta.
2. Definir un valor objetivo.
3. Crear la meta.
4. Registrar un aporte a la meta.
5. Consultar el progreso acumulado.

**Resultado esperado:** la meta debe guardarse localmente, mostrar el valor objetivo y reflejar los aportes realizados en su progreso.

También se propone comprobar la edición y eliminación de metas.

### 2.6. Preferencias de notificaciones

**Objetivo:** verificar el cambio de la configuración de notificaciones.

**Procedimiento propuesto:**
1. Consultar el estado actual de las notificaciones.
2. Desactivarlas.
3. Volver a activarlas.
4. Recargar la información local.

**Resultado esperado:** el estado seleccionado debe actualizarse y conservarse mediante el mecanismo de persistencia local.

## 3. Pruebas del servicio de Networking

El servicio `ExchangeRateService` realiza una petición HTTP mediante `URLSession` para consultar información externa de tasas de cambio.

La consulta utiliza el servicio público:

`https://open.er-api.com/v6/latest/USD`

La información de interés es el valor correspondiente a la moneda COP dentro de la respuesta.

### 3.1. Consulta exitosa de la tasa USD/COP

**Objetivo:** comprobar que el servicio pueda obtener la tasa de cambio desde la API.

**Procedimiento propuesto:**
1. Solicitar la actualización de la tasa de cambio.
2. Esperar la respuesta del servicio web.
3. Verificar que la respuesta HTTP sea válida.
4. Comprobar que la información recibida pueda interpretarse correctamente.
5. Verificar que exista un valor positivo para COP.

**Resultado esperado:** el servicio debe devolver el valor de la tasa USD/COP y el ViewModel debe actualizar la información disponible para la interfaz.

### 3.2. Respuesta HTTP inválida

**Objetivo:** comprobar el manejo de respuestas HTTP que no indiquen una operación exitosa.

**Procedimiento propuesto:**
1. Simular o provocar una respuesta HTTP fuera del rango exitoso.
2. Verificar la validación de la respuesta recibida.

**Resultado esperado:** el servicio debe informar un error de respuesta inválida y no utilizar información que no haya sido validada.

### 3.3. Respuesta JSON inválida o incompleta

**Objetivo:** comprobar que el servicio maneje respuestas que no tengan la estructura o los datos esperados.

**Procedimiento propuesto:**
1. Probar una respuesta con contenido JSON que no corresponda al modelo esperado.
2. Probar una respuesta sin el valor COP.
3. Probar una respuesta cuyo resultado no indique éxito.
4. Probar un valor COP igual o inferior a cero.

**Resultado esperado:** el servicio debe rechazar la información que no cumpla las condiciones de validación y comunicar el error correspondiente.

### 3.4. Error de conexión o tiempo de espera

**Objetivo:** verificar el comportamiento cuando no se puede acceder al servicio web.

**Procedimiento propuesto:**
1. Intentar actualizar la tasa de cambio sin conexión a Internet o con una conexión que no responda.
2. Esperar el resultado de la solicitud.

**Resultado esperado:** la aplicación debe mostrar un mensaje indicando que no fue posible actualizar la tasa y recomendar verificar la conexión. Las funciones locales de la aplicación deben seguir disponibles.

### 3.5. Control de solicitudes repetidas

**Objetivo:** verificar que no se inicie otra actualización mientras ya existe una solicitud en curso.

**Procedimiento propuesto:**
1. Iniciar una actualización de la tasa de cambio.
2. Intentar solicitar otra actualización antes de que termine la primera.
3. Observar el estado de carga.

**Resultado esperado:** el ViewModel debe evitar iniciar solicitudes simultáneas desde esta acción mientras la primera actualización esté en curso.

## 4. Resultados esperados y criterios de validación

Los siguientes criterios permiten determinar si el comportamiento observado coincide con el esperado:

| Componente | Criterio de validación |
|---|---|
| Carga local | Recuperación de movimientos, metas, saldos y preferencias. |
| Cuentas | Consulta del saldo correspondiente a la cuenta seleccionada. |
| Movimientos | Registro y eliminación de ingresos y gastos. |
| Metas | Creación, edición, eliminación y registro de aportes. |
| Notificaciones | Actualización y conservación de la preferencia. |
| API | Obtención y validación de la tasa USD/COP. |
| Errores de red | Comunicación de errores sin bloquear las funciones locales. |
| Solicitudes | Control de actualizaciones repetidas mientras hay una petición en curso. |

## 5. Alcance y limitaciones de la evidencia

Este documento presenta los casos de prueba y sus resultados esperados para el ViewModel y el servicio de Networking.

La documentación de estos casos no significa que hayan sido ejecutados satisfactoriamente. Para confirmar su funcionamiento real se requiere realizar las pruebas correspondientes en un entorno de ejecución compatible con iOS, como Xcode y un simulador o dispositivo.

La consulta de la tasa de cambio depende de la disponibilidad del servicio externo y de la conexión a Internet. El valor obtenido es una tasa pública de referencia y no debe presentarse como la TRM oficial de Colombia.

## 6. Conclusión

La definición de estos casos de prueba permite organizar la validación de los componentes principales de la aplicación Mi Puente Financiero iOS.

El ViewModel centraliza la gestión del estado y coordina las operaciones con la persistencia local y el servicio de consulta de tasas de cambio. Por su parte, el servicio de Networking permite obtener información externa y contempla validaciones para manejar respuestas inválidas y errores de conexión.

Esta planificación sirve como evidencia documental del comportamiento que se espera verificar y como guía para una futura ejecución de pruebas en un entorno iOS.
