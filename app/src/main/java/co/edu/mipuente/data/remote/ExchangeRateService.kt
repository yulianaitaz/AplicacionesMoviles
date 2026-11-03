package co.edu.mipuente.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object ExchangeRateService {
    private const val ENDPOINT = "https://open.er-api.com/v6/latest/USD"

    /**
     * Consulta una tasa USD/COP de referencia (no es la TRM oficial).
     * Es suspend y corre en Dispatchers.IO: quien la llame no se preocupa por el hilo.
     */
    suspend fun fetchUsdCop(): Double = withContext(Dispatchers.IO) {
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
            val rate = JSONObject(body).getJSONObject("rates").getDouble("COP")
            require(rate > 0.0) { "Tasa inválida" }
            rate
        } finally {
            connection.disconnect()
        }
    }
}
