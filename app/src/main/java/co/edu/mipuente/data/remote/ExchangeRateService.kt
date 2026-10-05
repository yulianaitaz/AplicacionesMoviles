package co.edu.mipuente.data.remote

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ExchangeRateService {
    private const val ENDPOINT = "https://open.er-api.com/v6/latest/USD"

    /**
     * Consulta una tasa USD/COP de referencia mediante un servicio HTTP público.
     * No se presenta como TRM oficial; se usa para demostrar conexión a un servicio en línea.
     */
    fun fetchUsdCop(): Double {
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8_000
            readTimeout = 8_000
            setRequestProperty("Accept", "application/json")
        }

        try {
            val code = connection.responseCode
            if (code !in 200..299) error("HTTP $code")
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            return json.getJSONObject("rates").getDouble("COP")
        } finally {
            connection.disconnect()
        }
    }
}
