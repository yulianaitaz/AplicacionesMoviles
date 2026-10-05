# Preguntas probables de sustentación individual

## ¿Por qué usar ViewModel?
Porque separa el estado y la lógica de la UI. `FinanceViewModel` expone un `StateFlow<FinanceUiState>` que Compose observa. Al cambiar el estado, la interfaz se recompone sin tener que manipular vistas manualmente.

## ¿Qué es StateFlow?
Es un flujo observable de estado que siempre posee un valor actual. En la app contiene saldo, cuenta seleccionada, movimientos, metas, preferencias y estado de la consulta en línea.

## ¿Dónde se usa Navigation Component?
En `AppNavHost.kt`. Se crea un `NavHost` con rutas declaradas en `Routes.kt` y cada `composable(...)` representa un destino.

## ¿Cómo se guardan los datos localmente?
`FinanceDbHelper` hereda de `SQLiteOpenHelper` y crea tablas `movements`, `goals` y `settings`. `FinanceRepository` centraliza las lecturas y escrituras para que el ViewModel no trabaje directamente con SQL.

## ¿Por qué Repository?
Reduce acoplamiento. La UI no conoce SQLite ni HTTP. El ViewModel pide operaciones al Repository y al servicio remoto, lo que facilita cambiar la fuente de datos en el futuro.

## ¿Cómo se demuestra la conexión en línea?
`ExchangeRateService` realiza una petición HTTP GET a un servicio público y extrae la tasa COP del JSON recibido. La pantalla Inicio presenta el resultado y un estado de carga/error.

## ¿Qué pasa si no hay Internet?
La app sigue funcionando con los datos locales. Solamente la tarjeta de tasa en línea informa que no pudo actualizarse. Esto muestra una degradación controlada.

## ¿Qué diferencia hay entre estado de UI y persistencia?
El `StateFlow` representa lo que la interfaz necesita en ese momento; SQLite conserva los datos aunque la aplicación se cierre. Al iniciar, el ViewModel carga SQLite y actualiza el StateFlow.

## ¿Cómo se cumple Material Design?
Se utiliza Material 3: `MaterialTheme`, `Card`, `Button`, `FilterChip`, `NavigationBar`, `OutlinedTextField`, tipografías, color scheme y formas consistentes.

## ¿Qué hace `Modifier.weight(1f)`?
Distribuye espacio disponible dentro del `RowScope` o `ColumnScope`. No debe importarse el antiguo símbolo interno `androidx.compose.foundation.layout.weight`; se usa directamente dentro de Row/Column.
