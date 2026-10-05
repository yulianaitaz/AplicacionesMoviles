# Criterios de diseño aplicados

## 1. Mostrar el valor de la aplicación desde el inicio

La pantalla de bienvenida comunica el nombre **Mi Puente Financiero** y la promesa “Control y ahorro, sin enredos”. En Inicio se muestran de inmediato saldo, meta de ahorro, movimientos y acciones frecuentes. El usuario comprende qué puede hacer sin recorrer menús profundos.

## 2. Iconos acompañados por etiquetas de texto

La barra inferior no utiliza iconos aislados: cada destino tiene texto visible (`Inicio`, `Movimientos`, `Añadir`, `Metas`, `Perfil`). Las acciones críticas también están rotuladas. Esto reduce ambigüedad y evita que el usuario deba adivinar el significado de un símbolo.

## 3. Lenguaje cercano al usuario

Se utilizan expresiones simples y directas como `Entró plata`, `Salió plata`, `Saldo total`, `Metas de ahorro` y `Movimientos`, en lugar de términos financieros excesivamente técnicos. Esto disminuye carga cognitiva.

## 4. Retroalimentación y estado visible

La interfaz muestra progreso de metas, estado de carga de la tasa en línea, confirmación visual de datos persistidos y cambios inmediatos en saldo/historial al registrar un movimiento. El usuario puede entender qué pasó después de cada acción.

## 5. Navegación consistente y control del usuario

La barra inferior conserva cinco destinos principales. La pantalla Acerca de incluye botón Atrás y no elimina el estado previo. El selector Personal/Negocio permite cambiar explícitamente el contexto financiero sin ocultar esta decisión al usuario.

## Material Design 3

El proyecto usa `MaterialTheme`, tarjetas, botones, chips, barra de navegación, campos de texto, tipografía jerárquica, esquemas de color y formas redondeadas coherentes en toda la app.
