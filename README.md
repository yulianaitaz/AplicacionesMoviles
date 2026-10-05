# Mi Puente Financiero — Taller 2 / Microproyecto Android

Aplicación móvil académica para llevar control básico de finanzas personales y de negocio: saldo, ingresos, gastos, historial, metas de ahorro y una consulta en línea de una tasa USD/COP de referencia.

## Qué incluye esta versión

- Jetpack Compose + Material 3.
- ViewModel con `StateFlow` para el estado de UI.
- Navigation Component (`NavHost`, `NavController`, rutas Compose).
- Base de datos local SQLite mediante `SQLiteOpenHelper`.
- Persistencia de movimientos, metas, balances y preferencia de notificaciones.
- Servicio en línea HTTP para consultar una tasa USD/COP de referencia.
- Permiso `INTERNET` declarado en el `AndroidManifest.xml`.
- Siete pantallas: Splash, Inicio, Movimientos, Registrar movimiento, Metas, Perfil y Acerca de/Créditos.
- Nombre e ícono propios de la aplicación.
- Tema Material 3 consistente.
- Selector Personal / Negocio.
- Búsqueda y filtros de movimientos.
- Mostrar/ocultar saldo.

## Compatibilidad usada

- Android Gradle Plugin: 9.3.0
- Gradle Wrapper: 9.6.0
- JDK: 17
- compileSdk / targetSdk: 37
- minSdk: 24

> Se usa AGP 9.3.0 porque la instalación de Android Studio usada durante el desarrollo reportó esa versión como la última compatible.

## Estructura principal

```text
app/src/main/java/co/edu/mipuente/
├── MainActivity.kt
├── config/
│   └── TeamConfig.kt
├── data/
│   ├── FinanceRepository.kt
│   ├── local/FinanceDbHelper.kt
│   └── remote/ExchangeRateService.kt
├── navigation/
│   ├── AppNavHost.kt
│   └── Routes.kt
└── ui/
    ├── MiPuenteApp.kt
    ├── components/
    ├── model/
    ├── screens/
    ├── theme/
    └── viewmodel/FinanceViewModel.kt
```
