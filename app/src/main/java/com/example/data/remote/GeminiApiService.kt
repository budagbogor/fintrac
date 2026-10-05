package com.example.data.remote

import com.example.BuildConfig
import com.example.data.model.GoalEntity
import com.example.domain.FinancialEngine
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OpenAiMessage(
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class OpenAiRequest(
    val model: String,
    val messages: List<OpenAiMessage>,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiResponse(
    val choices: List<OpenAiChoice>? = null
)

@JsonClass(generateAdapter = true)
data class OpenAiChoice(
    val message: OpenAiMessage? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GeminiGenerationConfig? = null,
    val systemInstruction: GeminiContent? = null,
    val tools: List<GeminiTool>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiTool(
    val googleSearch: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiPart(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    val content: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class PurchaseAdviceResponse(
    @Json(name = "recommendation") val recommendation: String, // BUY_NOW, POSTPONE, AVOID
    @Json(name = "postpone_months") val postponeMonths: Int = 0,
    @Json(name = "summary_reason") val summaryReason: String,
    @Json(name = "opportunity_cost_analysis") val opportunityCostAnalysis: String,
    @Json(name = "financial_tips") val financialTips: List<String> = emptyList()
)

interface GeminiApi {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val api: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApi::class.java)
    }

    val adviceAdapter = moshi.adapter(PurchaseAdviceResponse::class.java)

    fun escapeJson(s: String): String {
        val sb = java.lang.StringBuilder()
        sb.append("\"")
        for (i in s.indices) {
            val c = s[i]
            when (c) {
                '\"' -> sb.append("\\\"")
                '\\' -> sb.append("\\\\")
                '\b' -> sb.append("\\b")
                '\n' -> sb.append("\\n")
                '\r' -> sb.append("\\r")
                '\t' -> sb.append("\\t")
                else -> {
                    if (c.code < 0x20) {
                        val ss = "0000" + Integer.toHexString(c.code)
                        sb.append("\\u" + ss.substring(ss.length - 4))
                    } else {
                        sb.append(c)
                    }
                }
            }
        }
        sb.append("\"")
        return sb.toString()
    }

    suspend fun callOpenAiStyle(
        baseUrl: String,
        model: String,
        apiKey: String,
        systemPrompt: String,
        userPrompt: String,
        temperature: Float = 0.2f
    ): String? = withContext(Dispatchers.IO) {
        val cleanBaseUrl = baseUrl.trim().removeSuffix("/")
        val url = "$cleanBaseUrl/chat/completions"

        val requestBodyJson = """
            {
              "model": "$model",
              "messages": [
                {"role": "system", "content": ${escapeJson(systemPrompt)}},
                {"role": "user", "content": ${escapeJson(userPrompt)}}
              ],
              "temperature": $temperature
            }
        """.trimIndent()

        val body = requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val responseBody = response.body?.string() ?: return@withContext null
            val adapter = moshi.adapter(OpenAiResponse::class.java)
            val parsed = adapter.fromJson(responseBody)
            return@withContext parsed?.choices?.firstOrNull()?.message?.content
        }
    }

    suspend fun callGeminiStyle(
        baseUrl: String,
        model: String,
        apiKey: String,
        systemPrompt: String,
        userPrompt: String,
        searchMode: String? = null
    ): String? = withContext(Dispatchers.IO) {
        val cleanBaseUrl = baseUrl.trim().removeSuffix("/")
        val modelPath = model.removePrefix("gemini/")

        val url = if (cleanBaseUrl.contains("googleapis.com")) {
            "$cleanBaseUrl/v1beta/models/$modelPath:generateContent?key=$apiKey"
        } else {
            "$cleanBaseUrl/v1beta/models/$modelPath:generateContent?key=$apiKey"
        }

        val toolsJson = if (searchMode == "DEEP_SEARCH") {
            """, "tools": [{"googleSearch": {}}]"""
        } else {
            ""
        }

        val requestBodyJson = """
            {
              "contents": [
                {
                  "parts": [{"text": ${escapeJson(userPrompt)}}]
                }
              ],
              "systemInstruction": {
                "parts": [{"text": ${escapeJson(systemPrompt)}}]
              },
              "generationConfig": {
                "responseMimeType": "application/json",
                "temperature": 0.2
              }
              $toolsJson
            }
        """.trimIndent()

        val body = requestBodyJson.toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return@withContext null
            val responseBody = response.body?.string() ?: return@withContext null
            val adapter = moshi.adapter(GeminiResponse::class.java)
            val parsed = adapter.fromJson(responseBody)
            return@withContext parsed?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        }
    }

    suspend fun callAi(
        systemPrompt: String,
        userPrompt: String,
        customApiKey: String?,
        customBaseUrl: String?,
        customModel: String?,
        searchMode: String? = null
    ): String? {
        val apiKey = if (!customApiKey.isNullOrBlank()) customApiKey else BuildConfig.GEMINI_API_KEY
        val baseUrl = if (!customBaseUrl.isNullOrBlank()) customBaseUrl else "https://generativelanguage.googleapis.com/"
        val model = if (!customModel.isNullOrBlank()) customModel else "gemini/gemini-3.5-flash"

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "null") {
            return null
        }

        val isGeminiModel = model.startsWith("gemini/") || model.startsWith("gemini-")
        val isOpenAiStyle = baseUrl.contains("/v1") || baseUrl.contains("sumopod") || !isGeminiModel

        if (isOpenAiStyle) {
            try {
                val res = callOpenAiStyle(baseUrl, model, apiKey, systemPrompt, userPrompt)
                if (res != null) return res
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return try {
            callGeminiStyle(baseUrl, model, apiKey, systemPrompt, userPrompt, searchMode)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

object GeminiPurchaseAdvisor {

    suspend fun analyzePurchaseInquiry(
        itemName: String,
        price: Double,
        urgency: String, // NEED or WANT
        netDisposableIncome: Double,
        activeGoals: List<GoalEntity>,
        customApiKey: String? = null,
        customModel: String? = null,
        customBaseUrl: String? = null
    ): PurchaseAdviceResponse = withContext(Dispatchers.IO) {
        val apiKey = if (!customApiKey.isNullOrBlank()) customApiKey else BuildConfig.GEMINI_API_KEY

        // Fallback local evaluation if API key is not present or API fails
        val localEval = FinancialEngine.evaluatePurchaseLocal(itemName, price, urgency, netDisposableIncome)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext PurchaseAdviceResponse(
                recommendation = localEval.decision,
                postponeMonths = localEval.postponeMonths,
                summaryReason = localEval.rationale,
                opportunityCostAnalysis = "Jika uang sebesar Rp ${FinancialEngine.formatRupiah(price)} diinvestasikan selama 5 tahun dengan CAGR 8.5%, nilainya diproyeksikan menjadi Rp ${FinancialEngine.formatRupiah(localEval.projectedValue5Yr)} (Biaya Peluang Rp ${FinancialEngine.formatRupiah(localEval.opportunityCost5Yr)}).",
                financialTips = listOf(
                    "Utamakan pemenuhan dana darurat sebelum membeli barang non-esensial.",
                    "Gunakan aturan jeda 48 jam sebelum mengeksekusi pembelian keinginan.",
                    "Alokasikan minimal 20% dari sisa dana bersih ke instrumen investasi."
                )
            )
        }

        val goalsSummary = if (activeGoals.isNotEmpty()) {
            activeGoals.joinToString("; ") { "${it.title}: Target Rp ${FinancialEngine.formatRupiah(it.targetAmount)} (${it.targetHorizonMonths} bln)" }
        } else {
            "Belum ada target keuangan aktif."
        }

        val systemPrompt = """
            Anda adalah Personal Finance & Smart Investment Advisor AI yang rasional, objektif, dan suportif.
            Tugas Anda adalah menganalisis apakah pengguna sebaiknya membeli suatu barang/jasa ("Boleh Beli Gak Ya?").
            
            Format Output HARUS JSON Valid dengan struktur berikut:
            {
              "recommendation": "BUY_NOW" | "POSTPONE" | "AVOID",
              "postpone_months": number,
              "summary_reason": "penjelasan ringkas dalam Bahasa Indonesia",
              "opportunity_cost_analysis": "kalkulasi opportunity cost investasi jika uang tersebut diinvestasikan dalam 3-5 tahun",
              "financial_tips": ["tips 1", "tips 2"]
            }
        """.trimIndent()

        val userPrompt = """
            Analisislah rencana pembelian berikut:
            - Barang / Jasa: $itemName
            - Harga: Rp ${FinancialEngine.formatRupiah(price)}
            - Tingkat Urgensi: ${if (urgency.equals("NEED", true)) "Kebutuhan" else "Keinginan"}
            - Sisa Dana Bersih Bulanan Pengguna (Net Disposable Income): Rp ${FinancialEngine.formatRupiah(netDisposableIncome)}
            - Target Keuangan Aktif: $goalsSummary
            
            Berikan rekomendasi yang tegas (BUY_NOW, POSTPONE, atau AVOID), beri alasan logis, sertakan analisis opportunity cost jika uang tersebut diinvestasikan di pasar modal/reksa dana (CAGR ~8.5%), serta 2-3 saran finansial taktis.
        """.trimIndent()

        try {
            val jsonText = GeminiClient.callAi(
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                customApiKey = customApiKey,
                customBaseUrl = customBaseUrl,
                customModel = customModel
            )

            if (!jsonText.isNullOrBlank()) {
                val parsed = GeminiClient.adviceAdapter.fromJson(jsonText)
                if (parsed != null) {
                    return@withContext parsed
                }
            }
            return@withContext PurchaseAdviceResponse(
                recommendation = localEval.decision,
                postponeMonths = localEval.postponeMonths,
                summaryReason = localEval.rationale,
                opportunityCostAnalysis = "Jika uang Rp ${FinancialEngine.formatRupiah(price)} diinvestasikan selama 5 tahun (CAGR 8.5%), nilainya diproyeksikan menjadi Rp ${FinancialEngine.formatRupiah(localEval.projectedValue5Yr)}.",
                financialTips = listOf(
                    "Gunakan sisa dana bersih secara bijak.",
                    "Selalu simpan setidaknya 10-20% pendapatan untuk investasi."
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext PurchaseAdviceResponse(
                recommendation = localEval.decision,
                postponeMonths = localEval.postponeMonths,
                summaryReason = localEval.rationale,
                opportunityCostAnalysis = "Analisis Opportunity Cost: Rp ${FinancialEngine.formatRupiah(price)} berpotensi tumbuh menjadi Rp ${FinancialEngine.formatRupiah(localEval.projectedValue5Yr)} dalam 5 tahun.",
                financialTips = listOf(
                    "Pertimbangkan kebutuhan jangka panjang sebelum membeli.",
                    "Pastikan dana darurat tetap aman."
                )
            )
        }
    }
}

@JsonClass(generateAdapter = true)
data class PromoResult(
    @Json(name = "merchantName") val merchantName: String,
    @Json(name = "promoTitle") val promoTitle: String,
    @Json(name = "discountDetails") val discountDetails: String,
    @Json(name = "howToGet") val howToGet: String,
    @Json(name = "informationLink") val informationLink: String,
    @Json(name = "validityPeriod") val validityPeriod: String,
    @Json(name = "isOnline") val isOnline: Boolean,
    @Json(name = "category") val category: String = "Lainnya"
)

object LocalPromoRepository {
    fun getLocalPromos(query: String, isOnlineOnly: Boolean?): List<PromoResult> {
        val all = listOf(
            PromoResult(
                merchantName = "Indomaret",
                promoTitle = "Promo JSM (Jumat Sabtu Minggu) Indomaret",
                discountDetails = "Diskon Minyak Goreng 2L menjadi Rp 32.900, Beras Premium 5kg diskon Rp 5.000, Sabun Cair Buy 1 Get 1 Free.",
                howToGet = "Datang langsung ke gerai Indomaret terdekat, lakukan pembayaran menggunakan QRIS ShopeePay atau Kartu Debit BRI untuk tambahan diskon.",
                informationLink = "https://www.indomaret.co.id/promo",
                validityPeriod = "Berlaku setiap akhir pekan (Jumat - Minggu)",
                isOnline = false,
                category = "Lainnya"
            ),
            PromoResult(
                merchantName = "Alfamart",
                promoTitle = "Promo Gantung (Gaji Untung) Alfamart",
                discountDetails = "Diskon susu formula anak hingga 20%, deterjen cair diskon Rp 4.000, serta mie instan kartonan harga khusus.",
                howToGet = "Tunjukkan kartu member Alfagift Anda ke kasir sebelum pembayaran. Bayar dengan Gopay untuk mendapatkan cashback Rp 2.000.",
                informationLink = "https://alfamart.co.id/promo",
                validityPeriod = "25 s/d 2 hari pertama bulan berikutnya",
                isOnline = false,
                category = "Lainnya"
            ),
            PromoResult(
                merchantName = "Tokopedia",
                promoTitle = "Waktu Indonesia Belanja (WIB) Tokopedia",
                discountDetails = "Bebas Ongkir tanpa minimum belanja, Cashback gila-gilaan s/d 90% untuk kategori Gadget dan Fashion, Flash Sale mulai Rp 100.",
                howToGet = "Buka aplikasi Tokopedia selama periode WIB, klaim kupon diskon di halaman utama, pilih metode pembayaran GoPayLater untuk bonus koin.",
                informationLink = "https://www.tokopedia.com/promo",
                validityPeriod = "Tanggal 25 s/d akhir setiap bulan",
                isOnline = true,
                category = "E-Commerce"
            ),
            PromoResult(
                merchantName = "Shopee Indonesia",
                promoTitle = "Shopee Mantul Sale (SMS)",
                discountDetails = "Voucher Gratis Ongkir Rp 0, Voucher Diskon 50% untuk kategori Elektronik, Flash Sale iPhone Rp 1.000 setiap jam 12 malam.",
                howToGet = "Klaim voucher gratis ongkir di aplikasi Shopee, checkout menggunakan metode pembayaran ShopeePay atau SPayLater.",
                informationLink = "https://shopee.co.id/m/sms",
                validityPeriod = "Berlaku tanggal 25 s/d 27 setiap bulan",
                isOnline = true,
                category = "E-Commerce"
            ),
            PromoResult(
                merchantName = "Starbucks Indonesia",
                promoTitle = "Treat a Friend - Buy 1 Get 1 Free",
                discountDetails = "Beli 1 minuman ukuran Grande/Venti, dapatkan Gratis 1 minuman variant favorit (Caramel Macchiato, Green Tea Latte, atau Cafe Latte).",
                howToGet = "Khusus pengguna aplikasi Starbucks Card Indonesia atau pembayaran menggunakan LINE Bank.",
                informationLink = "https://www.starbucks.co.id/promo",
                validityPeriod = "Setiap hari Kamis dan akhir pekan",
                isOnline = false,
                category = "Kafe"
            ),
            PromoResult(
                merchantName = "McDonald's Indonesia",
                promoTitle = "Promo McSaver Hemat",
                discountDetails = "Paket Nasi + Ayam McD + Air Mineral hanya Rp 22.000, Paket Burger + Lemon Tea hanya Rp 24.000.",
                howToGet = "Pesan melalui aplikasi McDelivery atau tunjukkan kupon promo di kasir McDonald's terdekat.",
                informationLink = "https://mcdonalds.co.id/promo",
                validityPeriod = "Hingga akhir bulan ini",
                isOnline = false,
                category = "Restoran"
            ),
            PromoResult(
                merchantName = "Gojek",
                promoTitle = "Gofood Hemat Makan Nikmat",
                discountDetails = "Diskon s/d 50% untuk menu merchant pilihan, Gratis Ongkir s/d Rp 12.000 dengan minimal belanja Rp 40.000.",
                howToGet = "Pesan makanan lewat menu GoFood di aplikasi Gojek, gunakan filter 'Promo/Hemat', kupon diskon akan terpasang otomatis.",
                informationLink = "https://www.gojek.com/blog/gofood",
                validityPeriod = "Setiap hari s/d akhir tahun",
                isOnline = true,
                category = "Online"
            ),
            PromoResult(
                merchantName = "Grab Indonesia",
                promoTitle = "GrabFood Kilat Diskon 60%",
                discountDetails = "Diskon langsung s/d Rp 25.000 untuk ribuan restoran favorit, gratis ongkir kilat khusus merchant bertanda 'Kilat'.",
                howToGet = "Gunakan kode promo 'KILAT60' di halaman pembayaran GrabFood sebelum memesan.",
                informationLink = "https://www.grab.com/id/food",
                validityPeriod = "Berlaku s/d akhir bulan ini",
                isOnline = true,
                category = "Online"
            ),
            PromoResult(
                merchantName = "Solaria - Botani Square Bogor",
                promoTitle = "Promo Makan Hemat Keluarga",
                discountDetails = "Diskon 20% untuk semua menu makanan dengan minimum transaksi Rp 200.000. Lokasi: Lantai 2 Unit 12 (Samping Cinema XXI).",
                howToGet = "Datang langsung ke outlet Solaria Botani Square Bogor. Bayar menggunakan Kartu Debit/Kredit Bank Mandiri atau QRIS Livin' by Mandiri.",
                informationLink = "https://solariarestoran.co.id/",
                validityPeriod = "Setiap hari s/d Akhir Tahun",
                isOnline = false,
                category = "Restoran"
            ),
            PromoResult(
                merchantName = "HokBen - Grand Indonesia Mall",
                promoTitle = "HokBen Bento Spesial Hemat",
                discountDetails = "Paket Bento Spesial 1 & 2 diskon Rp 15.000, serta Free Ocha dingin. Lokasi: Foodprint Food Court Lantai 5, West Mall.",
                howToGet = "Tunjukkan halaman promo ini ke kasir HokBen Grand Indonesia Jakarta, lakukan pembayaran dengan GoPay atau AstraPay.",
                informationLink = "https://www.hokben.co.id/",
                validityPeriod = "Senin s/d Jumat, Pukul 14:00 - 17:00",
                isOnline = false,
                category = "Restoran"
            ),
            PromoResult(
                merchantName = "Imperial Kitchen & Dimsum - Botani Square",
                promoTitle = "Promo Dimsum Serbu Rp 9.999++",
                discountDetails = "Pilihan Siew Mai, Bakpao Telur Asin, atau Imperial Hakau hanya Rp 9.999 per porsi. Lokasi: Ground Floor (GF) Area Food & Beverage.",
                howToGet = "Khusus makan di tempat (Dine-in) di gerai Imperial Kitchen Botani Square. Berlaku tanpa minimum pembelian.",
                informationLink = "https://www.instagram.com/imperialkitchenid/",
                validityPeriod = "Senin s/d Jumat (Kecuali Hari Libur Nasional)",
                isOnline = false,
                category = "Restoran"
            ),
            PromoResult(
                merchantName = "Kopi Kenangan - Pondok Indah Mall (PIM 2)",
                promoTitle = "Promo Kopi Susu Kenangan Mantan",
                discountDetails = "Beli 2 Kopi Susu Kenangan Mantan (L) hanya Rp 30.000. Lokasi: Lantai G (Ground) dekat Lobby Utara.",
                howToGet = "Pesan langsung di outlet Kopi Kenangan PIM 2 atau pesan melalui Aplikasi Kopi Kenangan untuk pengambilan mandiri (Pick-Up).",
                informationLink = "https://kopikenangan.com/",
                validityPeriod = "Setiap hari s/d Akhir Bulan",
                isOnline = false,
                category = "Kafe"
            ),
            PromoResult(
                merchantName = "Solaria - Grand Indonesia",
                promoTitle = "Info Tenant & Menu Terfavorit",
                discountDetails = "Sajian Nasi Goreng Kambing, Mie Ayam Solaria, dan Chicken Cordon Bleu terbaik. Lokasi: Lantai 5 West Mall (Dekat Food Court).",
                howToGet = "Datang langsung ke restoran, nikmati suasana makan dengan pemandangan kota Jakarta dari Lantai 5.",
                informationLink = "https://goo.gl/maps/SolariaGrandIndonesia",
                validityPeriod = "Buka setiap hari, 10:00 - 22:00 WIB",
                isOnline = false,
                category = "Restoran"
            )
        )

        return all.filter { promo ->
            val matchesQuery = query.isBlank() || 
                    promo.merchantName.contains(query, ignoreCase = true) || 
                    promo.promoTitle.contains(query, ignoreCase = true) || 
                    promo.discountDetails.contains(query, ignoreCase = true)
            val matchesChannel = isOnlineOnly == null || promo.isOnline == isOnlineOnly
            matchesQuery && matchesChannel
        }
    }
}

object GeminiPromoSearcher {
    suspend fun searchPromos(
        query: String,
        searchMode: String, // "STANDARD" or "DEEP_SEARCH"
        isOnlineOnly: Boolean,
        customApiKey: String? = null,
        customModel: String? = null,
        customBaseUrl: String? = null
    ): List<PromoResult> = withContext(Dispatchers.IO) {
        val apiKey = if (!customApiKey.isNullOrBlank()) customApiKey else BuildConfig.GEMINI_API_KEY

        val fallbackList = LocalPromoRepository.getLocalPromos(query, if (isOnlineOnly) true else null)

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY" || apiKey == "null" || searchMode == "STANDARD") {
            return@withContext fallbackList
        }

        val systemPrompt = """
            Anda adalah Promo Hunter, Restaurant Scout, & Mall Tenant Assistant AI yang handal.
            Tugas Anda adalah melakukan riset mendalam (grounded research) menggunakan Google Search untuk menemukan:
            1. Promo, diskon, cashback, atau penawaran aktif terbaru untuk merchant, kategori, restoran, atau brand tertentu yang dicari pengguna.
            2. Informasi restoran terdekat, kafe terpopuler, mall terdekat, hotel terfavorit, atau tenant-tenant di dalam mall tertentu yang dicari pengguna.
            3. Lokasi/lantai tenant, menu favorit, jam operasional, serta penawaran khusus (bila ada) di mall/kota tersebut.
            
            Pastikan Anda mengklasifikasikan setiap item hasil riset ke dalam kategori yang tepat: "Kafe", "Restoran", "Mall", "Hotel", "E-Commerce", "Online", atau "Lainnya" (misal Supermarket/Retail fisik).
            Pastikan Anda hanya memberikan informasi yang VALID, AKTIF, dan AKURAT saat ini.
            Informasi yang diberikan HARUS mendetail termasuk cara mendapatkan (langkah/syarat/lokasi gerai offline) dan LINK/URL informasi yang jelas dan spesifik (seperti Google Maps, Instagram resmi, atau website merchant), serta masa berlakunya.
            
            Format Output HARUS berupa JSON Array Valid dengan format struktur berikut:
            [
              {
                "merchantName": "Nama Merchant / Restoran (Sertakan lokasi mall/kota jika ada, misal: 'Solaria - Botani Square')",
                "promoTitle": "Judul Promo / Info Tenant (misal: 'Promo Makan Hemat Solaria' atau 'Info Lokasi Tenant & Menu Favorit')",
                "discountDetails": "Detail diskon/lokasi tenant/menu andalan (misal: 'Diskon 20% dengan QRIS Mandiri, Lokasi Lantai 2 Unit 05')",
                "howToGet": "Cara mendapatkan promo secara jelas, jam operasional, atau petunjuk menuju lokasi tenant secara rinci",
                "informationLink": "Link/URL peta (Google Maps), website, atau media sosial tenant/promo tersebut",
                "validityPeriod": "Masa berlaku promo atau jam operasional tenant (misal: 'Berlaku s/d Akhir Bulan' atau 'Buka 10:00 - 22:00')",
                "isOnline": false,
                "category": "Kafe" | "Restoran" | "Mall" | "Hotel" | "E-Commerce" | "Online" | "Lainnya"
              }
            ]
        """.trimIndent()

        val userPrompt = """
            Cari informasi promo/diskon terbaru, restoran terdekat, atau info tenant mall untuk kata kunci: '$query'.
            Filter saluran merchant: ${if (isOnlineOnly) "Hanya Online" else "Online dan Offline"}.
            Lakukan DeepSearch & Grounded Research menggunakan Google Search untuk mendapatkan info riil (termasuk nama tenant di mall yang dituju, lantai, promo offline restoran, jam buka, rujukan link informasi valid, dan cara mendapatkannya).
        """.trimIndent()

        try {
            val jsonText = GeminiClient.callAi(
                systemPrompt = systemPrompt,
                userPrompt = userPrompt,
                customApiKey = customApiKey,
                customBaseUrl = customBaseUrl,
                customModel = customModel,
                searchMode = searchMode
            )

            if (!jsonText.isNullOrBlank()) {
                val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                val type = com.squareup.moshi.Types.newParameterizedType(List::class.java, PromoResult::class.java)
                val adapter = moshi.adapter<List<PromoResult>>(type)
                val parsed = adapter.fromJson(jsonText)
                if (!parsed.isNullOrEmpty()) {
                    return@withContext parsed
                }
            }
            return@withContext fallbackList
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext fallbackList
        }
    }
}
