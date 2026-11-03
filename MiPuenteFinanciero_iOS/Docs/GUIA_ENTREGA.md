# Guía de entrega del microproyecto SwiftUI

Basada en el documento del profesor Cristhian Figueroa, octubre de 2026.

## Requisitos obligatorios

1. Repositorio Git público con contribuciones visibles de cada integrante y ramas individuales.
2. Al menos cinco pantallas más una de descripción y créditos.
3. Navegación entre pantallas.
4. Formularios con entrada del usuario.
5. Gestión de estado de SwiftUI.
6. Datos persistidos en una base de datos local.
7. Consumo de una API en la web.
8. Módulos separados de UI, ViewModel, Red y Persistencia.

## Orden del informe escrito

1. Enlace al repositorio público.
2. Capturas reales de cinco pantallas y la pantalla de créditos.
3. **Diagrama arquitectónico hecho a mano** en papel, fotografiado y pegado en el informe. Explicación de ViewModel, Networking y Persistencia.
4. Reflexión comparativa del desarrollo con SwiftUI frente a Jetpack Compose.
5. Funcionalidades adicionales, si se desean décimas extra.

## Diagrama para dibujar en una hoja

```text
                 USUARIO
                    ↓
  SwiftUI (Inicio, Movimientos, Añadir,
          Metas, Perfil, Créditos)
                    ↓ eventos
              FinanceViewModel
          ObservableObject / @Published
              ↙               ↘
       FinanceRepository    ExchangeRateService
         SwiftData             URLSession
            ↓                     ↓
       ModelContext            API USD/COP
            ↓
      Base de datos local
   Movements · Goals · Settings
```

Indica al lado de las flechas que el ViewModel entrega estado publicado de vuelta a SwiftUI.

## Demostración en vivo

1. Cambiar entre Personal y Negocio y mostrar que cambia el saldo.
2. Crear un gasto e identificarlo en el historial.
3. Cerrar y reabrir la app. Comprobar que el gasto permanece.
4. Eliminar ese gasto. Comprobar que el saldo revierte su variación.
5. Crear una meta personalizada, añadir un aporte y verificar el porcentaje.
6. Activar o desactivar la preferencia de notificaciones y reiniciar la aplicación.
7. Pulsar actualizar tasa USD/COP. Mostrar resultado o mensaje de error controlado sin Internet.
8. Abrir Acerca de y verificar nombres de los integrantes.

## Trabajo colaborativo en Git

La forma más segura de conservar Android y SwiftUI juntos en el repositorio es añadir esta carpeta `MiPuenteFinanciero_SwiftUI` como proyecto independiente, sin sobrescribir el módulo Android.

Propuesta de ramas nuevas para SwiftUI:

- Guillermo: `feature/swiftui-ui-navigation`
- José: `feature/swiftui-persistence`
- Yennifer: `feature/swiftui-viewmodel-network`

Cada integrante debe realizar cambios reales, confirmarlos con `git commit` y subirlos con `git push -u origin NOMBRE_RAMA`. Después, abrir un Pull Request hacia `main`. Un commit vacío no demuestra una contribución técnica.

## Preguntas orales

- ¿Qué es `@State`, `@Binding` y `@ObservedObject`?
- ¿Cuál es la responsabilidad de `FinanceViewModel`?
- ¿Dónde se guarda la información y cómo se recupera al reiniciar?
- ¿Qué hace `ModelContext` y cuál es el papel de `FinanceRepository`?
- ¿Qué realiza `URLSession` y cómo se interpreta la respuesta JSON con `Codable`?
- ¿Qué función cumplen `TabView` y `NavigationStack`?
- ¿Qué diferencias encontraron entre SwiftUI y Jetpack Compose?

## Reflexión comparativa orientativa

En Android trabajamos con Kotlin, Jetpack Compose y StateFlow, mientras que en iOS utilizamos Swift, SwiftUI y propiedades publicadas en un ObservableObject. En ambos entornos encontramos una forma declarativa de construir la interfaz y un modelo de estado que permite actualizarla según los datos. La estructura de datos también se mantiene separada de las pantallas. SwiftData facilitó la persistencia integrada con el ecosistema Apple y URLSession permitió realizar la consulta web sin dependencias adicionales. La adaptación nos obligó a reorganizar la navegación y los componentes para seguir los patrones nativos de iOS, conservando las decisiones funcionales que ya habíamos validado en Android.

## Rúbrica

- Diseño 15 %
- Navegación y pantallas 10 %
- Networking 15 %
- Persistencia 20 %
- ViewModel 10 %
- Informe 10 %
- Preguntas individuales 20 %

Se contemplan hasta 0.3 puntos adicionales por funcionalidades extra, según su complejidad y valoración docente.
