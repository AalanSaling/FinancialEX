package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

object CurrencyConverter {

    // Base rates relative to 1 USD
    private val usdRates = mutableMapOf(
        "USD" to 1.0,
        "PYG" to 7800.0,
        "BRL" to 5.9283, // Updated market accurate rate ~$5.928,30 / USD
        "EUR" to 0.915,
        "ARS" to 980.0,
        "CLP" to 940.0,
        "UYU" to 40.5,
        "GBP" to 0.78,
        "BTC" to 0.000016, // ~$62,000 USD
        "ETH" to 0.00038,  // ~$2,600 USD
        "SOL" to 0.0070    // ~$140 USD
    )

    val supportedCurrencies = listOf("PYG", "USD", "BRL", "EUR", "ARS", "CLP", "UYU", "GBP", "BTC", "ETH", "SOL")

    // Reference previous day rates in PYG for 1 unit of foreign currency
    private val previousPygRates = mapOf(
        "USD" to 7820.0,
        "BRL" to 1310.0,
        "EUR" to 8550.0,
        "ARS" to 7.90,
        "CLP" to 8.35,
        "UYU" to 191.0,
        "GBP" to 9950.0,
        "BTC" to 480000000.0,
        "ETH" to 20500000.0,
        "SOL" to 1080000.0
    )

    data class PygTrend(
        val isUp: Boolean,
        val percentChange: Double,
        val label: String,
        val arrowSymbol: String
    )

    fun generateHistoricalRates(
        base: String,
        target: String,
        currentRate: Double,
        timeframe: String
    ): Pair<List<Double>, String> {
        val (count, volatility, label) = when (timeframe) {
            "1D" -> Triple(24, 0.003, "últimas 24 horas")
            "3D" -> Triple(30, 0.006, "últimos 3 dias")
            "5D" -> Triple(35, 0.010, "últimos 5 dias")
            "7D" -> Triple(42, 0.015, "últimos 7 dias")
            "1M" -> Triple(30, 0.025, "último mês")
            "1Y" -> Triple(52, 0.060, "último ano")
            "5Y" -> Triple(60, 0.150, "últimos 5 anos")
            "ALL" -> Triple(80, 0.250, "todo o período")
            else -> Triple(30, 0.015, "período")
        }

        val seed = (base + target + timeframe).hashCode()
        val list = mutableListOf<Double>()

        for (i in 0 until count) {
            val t = i.toDouble() / (count - 1)
            val wave1 = kotlin.math.sin(t * 8.0 + seed) * 0.5
            val wave2 = kotlin.math.sin(t * 19.0 + seed * 2) * 0.25
            val trend = (t - 1.0) * (if (seed % 2 == 0) 0.8 else -0.8)

            val factor = 1.0 + (wave1 + wave2 + trend) * volatility
            val rate = currentRate * factor
            list.add(rate.coerceAtLeast(0.000001))
        }

        if (list.isNotEmpty()) {
            list[list.size - 1] = currentRate
        }

        return Pair(list, label)
    }

    fun getCurrencyTrend(
        baseCode: String,
        targetCode: String = "PYG",
        timeframe: String = "7D"
    ): PygTrend {
        val currentRate = convert(1.0, baseCode, targetCode)
        val (points, _) = generateHistoricalRates(baseCode, targetCode, currentRate, timeframe)

        val firstRate = points.firstOrNull() ?: currentRate
        val lastRate = points.lastOrNull() ?: currentRate
        val diff = lastRate - firstRate
        val pct = if (firstRate > 0) (diff / firstRate) * 100.0 else 0.0

        val isUp = diff >= 0
        val arrow = if (isUp) "▲" else "▼"
        val sign = if (pct >= 0) "+" else ""

        val labelText = if (targetCode.uppercase() == "PYG") {
            if (isUp) "$arrow $sign${String.format(Locale.US, "%.2f", pct)}% ($timeframe) • Guaraní desvalorizou"
            else "$arrow $sign${String.format(Locale.US, "%.2f", pct)}% ($timeframe) • Guaraní valorizou"
        } else {
            "$arrow $sign${String.format(Locale.US, "%.2f", pct)}%"
        }

        return PygTrend(isUp, pct, labelText, arrow)
    }

    fun getPygTrend(code: String): PygTrend {
        return getCurrencyTrend(code, "PYG", "7D")
    }

    /**
     * Fetches real-time market exchange rates from open live APIs (Fiat + Crypto).
     */
    suspend fun fetchLiveExchangeRates(): Boolean = withContext(Dispatchers.IO) {
        var success = false
        val fiatUrls = listOf(
            "https://open.er-api.com/v6/latest/USD",
            "https://api.exchangerate-api.com/v4/latest/USD"
        )

        for (urlString in fiatUrls) {
            try {
                val url = URL(urlString)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "GET"
                conn.connectTimeout = 5000
                conn.readTimeout = 5000

                if (conn.responseCode == 200) {
                    val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(jsonText)
                    val ratesObj = json.optJSONObject("rates")
                    if (ratesObj != null) {
                        supportedCurrencies.forEach { curr ->
                            if (ratesObj.has(curr)) {
                                val rate = ratesObj.getDouble(curr)
                                if (rate > 0) {
                                    usdRates[curr] = rate
                                }
                            }
                        }
                        success = true
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Secondary high-precision crypto fetch from Coinbase rates API
        try {
            val cbUrl = URL("https://api.coinbase.com/v2/exchange-rates?currency=USD")
            val cbConn = cbUrl.openConnection() as HttpURLConnection
            cbConn.requestMethod = "GET"
            cbConn.connectTimeout = 4000
            cbConn.readTimeout = 4000
            if (cbConn.responseCode == 200) {
                val jsonText = cbConn.inputStream.bufferedReader().use { it.readText() }
                val dataObj = JSONObject(jsonText).optJSONObject("data")
                val ratesObj = dataObj?.optJSONObject("rates")
                if (ratesObj != null) {
                    listOf("BTC", "ETH", "SOL").forEach { crypto ->
                        if (ratesObj.has(crypto)) {
                            val rate = ratesObj.getDouble(crypto)
                            if (rate > 0) {
                                usdRates[crypto] = rate
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        return@withContext success
    }

    /**
     * Converts an amount from source currency to target currency.
     */
    fun convert(amount: Double, fromCurrency: String, toCurrency: String): Double {
        val from = fromCurrency.uppercase()
        val to = toCurrency.uppercase()

        if (from == to) return amount

        val fromRateInUsd = usdRates[from] ?: 1.0
        val toRateInUsd = usdRates[to] ?: 1.0

        // Convert amount to USD first
        val amountInUsd = amount / fromRateInUsd
        // Convert USD to target currency
        return amountInUsd * toRateInUsd
    }

    fun format(amount: Double, currency: String): String {
        val curr = currency.uppercase()
        return when (curr) {
            "PYG" -> {
                val format = NumberFormat.getNumberInstance(Locale("es", "PY"))
                "₲ " + format.format(amount.toLong())
            }
            "BRL" -> {
                val format = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                format.format(amount)
            }
            "USD" -> {
                val format = NumberFormat.getCurrencyInstance(Locale.US)
                format.format(amount)
            }
            "EUR" -> {
                val format = NumberFormat.getCurrencyInstance(Locale.GERMANY)
                format.format(amount)
            }
            "ARS" -> {
                val format = NumberFormat.getNumberInstance(Locale("es", "AR"))
                "$ " + format.format(amount)
            }
            "CLP" -> {
                val format = NumberFormat.getNumberInstance(Locale("es", "CL"))
                "CLP$ " + format.format(amount.toLong())
            }
            "UYU" -> {
                val format = NumberFormat.getNumberInstance(Locale("es", "UY"))
                "\$U " + format.format(amount)
            }
            "GBP" -> {
                val format = NumberFormat.getCurrencyInstance(Locale.UK)
                format.format(amount)
            }
            "BTC" -> String.format(Locale.US, "₿ %.6f", amount)
            "ETH" -> String.format(Locale.US, "Ξ %.4f", amount)
            "SOL" -> String.format(Locale.US, "SOL %.2f", amount)
            else -> String.format(Locale.US, "%.2f %s", amount, curr)
        }
    }

    fun updateUsdRate(currency: String, rateRelativeUsd: Double) {
        usdRates[currency.uppercase()] = rateRelativeUsd
    }

    fun getRatesCopy(): Map<String, Double> = usdRates.toMap()
}
