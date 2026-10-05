# Pitch ejecutivo — máximo 5 minutos

## 0:00–0:40 | Problema

Administrar simultáneamente gastos personales, ingresos, metas de ahorro y movimientos de un pequeño negocio suele terminar en notas separadas, hojas de cálculo o registros incompletos. El problema no es solamente saber cuánto dinero hay, sino entender rápidamente qué entra, qué sale y cuánto falta para cumplir una meta.

## 0:40–1:30 | Solución

**Mi Puente Financiero** reúne esa información en una aplicación móvil sencilla. El usuario puede separar su entorno `Personal` del entorno `Negocio`, consultar el saldo, registrar una entrada o salida en pocos toques, revisar el historial y visualizar metas de ahorro.

## 1:30–2:30 | Demostración

Mostrar Inicio y cambiar Personal/Negocio. Registrar un gasto de prueba. Abrir Movimientos y enseñar que aparece inmediatamente. Usar búsqueda o filtro. Mostrar Metas. Volver a Inicio y actualizar la tasa USD/COP de referencia.

## 2:30–3:20 | Valor para un entorno de negocio

Para una persona independiente o un pequeño negocio, la separación entre dinero personal y dinero del negocio permite tener una lectura más clara del flujo básico. La aplicación evita formularios extensos, usa lenguaje cotidiano y organiza la información en acciones que pueden completarse rápidamente desde el celular.

## 3:20–4:15 | Diseño y tecnología

La app usa Jetpack Compose y Material 3. El estado se maneja con ViewModel y StateFlow. La navegación usa Navigation Component. Los datos se almacenan de manera persistente en SQLite local. Además, existe conexión a un servicio HTTP para consultar una tasa USD/COP de referencia.

Tres criterios de diseño visibles son: valor claro desde la primera pantalla; iconos acompañados por etiquetas; y retroalimentación visible después de las acciones. También se usa lenguaje cercano al usuario y navegación consistente.

## 4:15–5:00 | Cierre

Mi Puente Financiero busca convertir el control cotidiano del dinero en una tarea rápida, comprensible y centralizada. El microproyecto demuestra una arquitectura móvil completa: interfaz declarativa, gestión de estado, navegación, persistencia local y consumo de un servicio en línea; y deja una base extensible para presupuestos, reportes y sincronización futura.
