# Mi Puente Financiero para iOS

Microproyecto de la asignatura **Electiva Desarrollo de Aplicaciones Móviles**, Universidad del Cauca, desarrollado con SwiftUI.

## Inicio rápido en Xcode

1. Descomprime `MiPuenteFinanciero_SwiftUI.zip` en un Mac.
2. Abre **`MiPuenteFinanciero.xcodeproj`** con Xcode 15 o posterior.
3. Selecciona el esquema **MiPuenteFinanciero** y un simulador con **iOS 17 o posterior**.
4. Pulsa **⌘R** para compilar, instalar y ejecutar.
5. En un iPhone físico, elige tu equipo de firma en **Signing & Capabilities** y usa un identificador de paquete propio si fuera necesario.
6. Si el simulador no está instalado, descárgalo desde Xcode → Settings → Platforms.

El proyecto no requiere CocoaPods, Carthage ni paquetes de terceros. Toda la interfaz y el almacenamiento se implementan con frameworks de Apple.

> **Verificación**. Los archivos Swift se han revisado sintácticamente. La compilación final en un simulador iOS debe hacerse en macOS con Xcode. El entorno de creación no incluye `xcodebuild`.

## Qué incluye

- Inicio con saldo, selector Personal/Negocio, meta principal, acciones rápidas, últimos movimientos y tarjeta USD/COP.
- Movimientos con búsqueda, filtro por ingresos y gastos, contexto Personal/Negocio y eliminación mediante pulsación prolongada.
- Registrar movimiento con teclado numérico propio, categoría, descripción opcional y botones Entró plata / Salió plata.
- Metas con progreso, creación, edición de nombre y objetivo, registro de aportes y eliminación.
- Perfil con preferencia de notificaciones persistida, selector de entorno y acceso a Créditos.
- Pantalla Acerca de/Créditos con el equipo.
- Splash de bienvenida mientras inicia el almacenamiento.

## Arquitectura

```text
SwiftUI Screens / TabView / NavigationStack
                 ↓ acciones / ↑ estado
          FinanceViewModel
          ObservableObject
             @Published
           ↙             ↘
FinanceRepository      ExchangeRateService
    SwiftData               URLSession
   ModelContext          HTTPS y Codable
       ↓                     ↓
 Movements, Goals,       API pública
    Settings              USD → COP
```

### Persistencia local

`FinanceRepository` usa SwiftData y `ModelContext` para guardar transacciones, metas y ajustes. Los saldos se almacenan como ajustes diferenciados por cuenta. Cada ingreso aumenta el saldo y cada gasto lo disminuye. Eliminar movimientos revierte su efecto. Los registros de demostración se crean una sola vez, al iniciar una base vacía.

### Networking

`ExchangeRateService` realiza una solicitud HTTPS con `URLSession`, valida la respuesta HTTP y decodifica el JSON con `JSONDecoder`. Extrae la tasa `rates.COP` del endpoint `https://open.er-api.com/v6/latest/USD`. Se trata de una tasa pública de referencia, no de la TRM oficial. La falta de conectividad no impide usar las funciones locales.

### Gestión del estado

`FinanceViewModel` es `ObservableObject` y publica el estado con `@Published`. Las pantallas lo observan mediante `@ObservedObject`. El arranque conserva una instancia mientras funciona la app. Los formularios de metas emplean `@State` y los componentes compartidos utilizan `@Binding`.

## Estructura del código

```text
MiPuenteFinanciero/
  App/                Punto de entrada y TabView
  Models/             Modelos SwiftData
  DesignSystem/       Colores, componentes compartidos
  Persistence/        Repositorio local SwiftData
  Services/           URLSession y Codable
  ViewModels/         Estado y acciones de la interfaz
  Screens/            Pantallas SwiftUI
  Assets.xcassets/    Ícono y recursos de marca
```

## Funciones adicionales

1. Crear metas con nombre y objetivo personalizados.
2. Editar nombre y objetivo de una meta existente.
3. Registrar aportes que incrementan el porcentaje de ahorro.
4. Eliminar metas y movimientos.
5. Mantener filtros de movimientos y un selector Personal/Negocio.

## Limitaciones de esta versión

- Transferir, Servicios y Recargar se muestran como accesos conceptuales. Al tocarlos la app informa que llegarán en una versión futura. No se realizan pagos reales.
- El guardado es local, sin sincronización con iCloud ni autenticación bancaria.
- La preferencia de notificaciones se persiste, pero no se programan notificaciones del sistema.
- Las metas se administran como objetivos de ahorro, sin retirar dinero de los saldos de las cuentas al registrar aportes.
- La tasa USD/COP depende de un servicio público y puede no estar disponible.

## Equipo

- Guillermo Adolfo Cuéllar Fonseca
- José Luis Sánchez López
- Yennifer Yuliana Uni Itaz

## Entrega académica

Consulta `Docs/GUIA_ENTREGA.md` para el orden del informe, el diagrama a mano, las demostraciones y el trabajo Git por ramas.
