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

## Antes de entregar

1. Editar `app/src/main/java/co/edu/mipuente/config/TeamConfig.kt` y poner los nombres reales de todos los integrantes.
2. Compilar e instalar la app en emulador o teléfono.
3. Tomar capturas reales de las pantallas y pegarlas en el informe.
4. Crear el repositorio público y poner la URL en el informe.
5. Verificar que cada integrante haya hecho commits y tenga su propia rama.

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

## Demostración sugerida

1. Abrir la app y mostrar nombre/ícono en el launcher.
2. Pasar del Splash a Inicio.
3. Cambiar entre Personal y Negocio.
4. Registrar un gasto y mostrarlo en Movimientos.
5. Cerrar/reabrir la app para demostrar que el movimiento persiste localmente.
6. Mostrar búsqueda y filtros.
7. Abrir Metas y crear una meta de demostración.
8. En Inicio, tocar actualizar tasa USD/COP para demostrar conexión al servicio en línea.
9. Abrir Perfil → Acerca de y créditos.

## Archivos de apoyo

En `docs/` se incluyen:

- `ARQUITECTURA.md`
- `CRITERIOS_DISENO.md`
- `PITCH_5_MIN.md`
- `GIT_ENTREGA.md`
- `PREGUNTAS_SUSTENTACION.md`
- `CHECKLIST_ENTREGA.md`


## Trazabilidad de la entrega

También se incluyen `docs/REQUISITOS_TALLER2.md`, `docs/CAPTURAS_ENTREGA.md` y `docs/ARQUITECTURA.puml` para cerrar la entrega contra la rúbrica, organizar las capturas y disponer de una versión tipo Deployment Diagram.
