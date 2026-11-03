# Evidencia de Pruebas: ViewModel y Conectividad HTTP

**Desarrollador:** Yennifer Yuliana Uni Itaz  
**Rama:** `feature/ios-viewmodel-red-yennifer`  
**Componentes probados:** `FinanceViewModel.swift` y `ExchangeRateService.swift`  

---

## 1. Verificación de peticiones HTTP (ExchangeRateService)

### Escenario A: Consulta exitosa (Conexión activa)
* **Comportamiento inicial:** La tarjeta USD/COP muestra estado de reposo.
* **Acción:** Activación de la actualización de la tasa de cambio.
* **Flujo ejecutado:**
  1. `isLoading` pasa a `true` (la tarjeta muestra el indicador de carga).
  2. Petición HTTP síncrona/asíncrona mediante `URLSession` retorna un código `HTTP 200 OK`.
  3. `JSONDecoder` procesa la estructura y valida el campo `rates.COP`.
  4. La propiedad `@Published tasaUSDCOP` recibe el valor actualizado y notifica a la vista.
  5. `isLoading` retorna a `false`.

### Escenario B: Fallo de red (Sin conexión a Internet)
* **Comportamiento inicial:** Conexión a Internet desactivada.
* **Acción:** Solicitud de actualización de tasa.
* **Flujo ejecutado:**
  1. `isLoading` pasa a `true`.
  2. `URLSession` arroja una excepción de conectividad (`URLError.notConnectedToInternet`).
  3. El bloque `catch` maneja la excepción sin provocar la detención (`crash`) de la aplicación.
  4. La propiedad `@Published errorMessage` actualiza su valor informando del error en pantalla.
  5. `isLoading` regresa a `false`.

---

## 2. Verificación de estado local (FinanceViewModel)

* **Prueba de concurrencia:** Ejecución de registro de movimientos mientras se realiza la petición web.
* **Resultado:** Las mutaciones sobre las propiedades locales (`movimientos`) responden en el hilo principal (`MainThread`), confirmando que la consulta HTTP asíncrona mediante `async/await` no congela la interfaz ni interrumpe la navegación.
