package com.example.domain

import com.example.data.model.ExpenseEntity
import com.example.data.model.IncomeEntity
import com.example.data.model.ObligationEntity
import kotlin.math.ceil
import kotlin.math.pow

data class CashFlowSummary(
    val totalIncome: Double,
    val totalObligations: Double,
    val totalExpenses: Double,
    val netDisposableIncome: Double,
    val surplusRatioPercent: Double,
    val healthScore: Int, // 0 - 100
    val statusMessage: String
)

data class AssetAllocation(
    val moneyMarketPercent: Int, // Pasar Uang
    val bondsPercent: Int,       // Obligasi / SBN
    val mutualFundsPercent: Int, // Reksa Dana Campuran/Pendapatan Tetap
    val stocksPercent: Int,      // Saham / ETF
    val summaryText: String
)

data class LocalPurchaseEvaluation(
    val decision: String, // BUY_NOW, POSTPONE, AVOID
    val postponeMonths: Int,
    val opportunityCost3Yr: Double,
    val opportunityCost5Yr: Double,
    val rationale: String,
    val projectedValue5Yr: Double
)

data class FireCalculationResult(
    val totalInvestedAssets: Double,
    val monthlyExpenses: Double,
    val monthlyPassiveIncome: Double,
    val fireTargetNumber: Double,
    val fireProgressPercent: Double,
    val yearsToFire: Double,
    val statusTitle: String,
    val statusDescription: String
)

data class BudgetRule503020(
    val targetNeeds: Double,       // 50%
    val targetWants: Double,       // 30%
    val targetInvestments: Double, // 20%
    val actualNeeds: Double,       // Obligations
    val actualWants: Double,       // Expenses
    val actualInvestments: Double, // Net Surplus
    val isNeedsOk: Boolean,
    val isWantsOk: Boolean,
    val isInvestmentOk: Boolean
)

data class RecommendedInstrument(
    val name: String,
    val category: String, // "EMAS", "SUKUK_SYARIAH", "REKSA_DANA_SYARIAH", "SAHAM_SYARIAH"
    val allocationPercent: Int,
    val monthlyAmount: Double,
    val expectedReturnCagrPercent: Double,
    val riskLevel: String, // Low, Moderate, High
    val description: String,
    val isPrimaryShariaOrGold: Boolean = true
)

data class ProactiveAiAdvice(
    val summaryTitle: String,
    val financialHealthAssessment: String,
    val recommendedMonthlyInvestmentAmount: Double,
    val recommendedSavingsRatePercent: Double,
    val recommendedInstruments: List<RecommendedInstrument>,
    val shariaGoldFocusRationale: String,
    val tacticalSteps: List<String>
)


object FinancialEngine {

    const val DEFAULT_INVESTMENT_CAGR = 0.085 // 8.5% p.a. average compound return

    fun calculateFireStatus(
        goals: List<com.example.data.model.GoalEntity>,
        monthlyObligations: Double,
        monthlyExpenses: Double,
        monthlyNetSurplus: Double
    ): FireCalculationResult {
        val totalAssets = goals.sumOf { it.currentAmount }
        val totalMonthlyExpense = (monthlyObligations + monthlyExpenses).coerceAtLeast(1.0)
        
        // Average expected CAGR from goals or default 8.5%
        val avgCagr = if (goals.isNotEmpty()) {
            goals.map { it.expectedCagrPercentage / 100.0 }.average()
        } else {
            DEFAULT_INVESTMENT_CAGR
        }

        // Monthly passive income = (Total Assets * CAGR) / 12
        val annualPassiveInc = totalAssets * avgCagr
        val monthlyPassiveInc = annualPassiveInc / 12.0

        // FIRE Target Number = 25x Annual Expenses (Rule of 25)
        val fireTarget = totalMonthlyExpense * 12.0 * 25.0
        val progressPercent = if (fireTarget > 0) (totalAssets / fireTarget) * 100.0 else 0.0

        // Years to FIRE estimation
        val remainingGap = (fireTarget - totalAssets).coerceAtLeast(0.0)
        val monthlyContribution = monthlyNetSurplus.coerceAtLeast(1.0)
        val rMonthly = avgCagr / 12.0

        // Approximating compounding months to reach target
        val yearsToFire = if (remainingGap == 0.0) {
            0.0
        } else if (monthlyContribution > 0) {
            val months = kotlin.math.ln(1.0 + (remainingGap * rMonthly / monthlyContribution)) / kotlin.math.ln(1.0 + rMonthly)
            if (months.isNaN() || months.isInfinite()) 30.0 else (months / 12.0).coerceIn(0.1, 50.0)
        } else {
            50.0
        }

        val (statusTitle, statusDesc) = when {
            progressPercent >= 100.0 -> "Bebas Finansial! 🎉" to "Passive income dari investasi Anda sudah cukup untuk menanggung seluruh biaya hidup bulanan."
            progressPercent >= 50.0 -> "Langkah Emas FIRE 🚀" to "Anda sudah mencapai separuh jalan menuju kebebasan finansial jangka panjang!"
            progressPercent >= 20.0 -> "Pondasi Kuat 💪" to "Portofolio investasi mulai menghasilkan arus pasif yang stabil."
            else -> "Mulai Perjalanan FIRE 🎯" to "Rutin sisihkan sisa dana bersih ke instrumen investasi untuk mempercepat bebas finansial."
        }

        return FireCalculationResult(
            totalInvestedAssets = totalAssets,
            monthlyExpenses = totalMonthlyExpense,
            monthlyPassiveIncome = monthlyPassiveInc,
            fireTargetNumber = fireTarget,
            fireProgressPercent = progressPercent.coerceIn(0.0, 100.0),
            yearsToFire = yearsToFire,
            statusTitle = statusTitle,
            statusDescription = statusDesc
        )
    }

    fun calculateBudget503020(
        totalIncome: Double,
        totalObligations: Double,
        totalExpenses: Double,
        netSurplus: Double
    ): BudgetRule503020 {
        val inc = totalIncome.coerceAtLeast(1.0)
        val tNeeds = inc * 0.50
        val tWants = inc * 0.30
        val tInvest = inc * 0.20

        val actNeeds = totalObligations
        val actWants = totalExpenses
        val actInvest = netSurplus.coerceAtLeast(0.0)

        return BudgetRule503020(
            targetNeeds = tNeeds,
            targetWants = tWants,
            targetInvestments = tInvest,
            actualNeeds = actNeeds,
            actualWants = actWants,
            actualInvestments = actInvest,
            isNeedsOk = actNeeds <= tNeeds,
            isWantsOk = actWants <= tWants,
            isInvestmentOk = actInvest >= tInvest
        )
    }

    fun calculateCashFlow(
        incomes: List<IncomeEntity>,
        obligations: List<ObligationEntity>,
        expenses: List<ExpenseEntity>
    ): CashFlowSummary {
        val totalInc = incomes.sumOf { it.amount }
        val totalObl = obligations.filter { it.isActive }.sumOf { it.amount }
        val totalExp = expenses.sumOf { it.amount }
        val netSurplus = totalInc - (totalObl + totalExp)
        val ratio = if (totalInc > 0) (netSurplus / totalInc) * 100.0 else 0.0

        val healthScore = when {
            totalInc == 0.0 -> 0
            ratio >= 30.0 -> 95
            ratio >= 20.0 -> 80
            ratio >= 10.0 -> 65
            ratio >= 0.0 -> 45
            else -> 20
        }

        val statusMsg = when {
            totalInc == 0.0 -> "Belum ada data pendapatan."
            ratio >= 20.0 -> "Arus Kas Sangat Sehat! Anda memiliki sisa dana bersih yang ideal untuk investasi."
            ratio >= 10.0 -> "Arus Kas Cukup Baik. Pertimbangkan menekan pengeluaran variabel."
            ratio >= 0.0 -> "Arus Kas Ketat. Sisa dana bersih tipis, hindari pengeluaran tidak mendesak."
            else -> "Defisiti Arus Kas! Pengeluaran & kewajiban melebihi total pendapatan bulanan."
        }

        return CashFlowSummary(
            totalIncome = totalInc,
            totalObligations = totalObl,
            totalExpenses = totalExp,
            netDisposableIncome = netSurplus,
            surplusRatioPercent = ratio,
            healthScore = healthScore,
            statusMessage = statusMsg
        )
    }

    /**
     * Future Value compound interest: FV = P * (1 + r)^t
     */
    fun calculateFutureValue(principal: Double, annualRate: Double, years: Double): Double {
        if (principal <= 0) return 0.0
        return principal * (1.0 + annualRate).pow(years)
    }

    fun calculateOpportunityCost(price: Double, years: Double, annualRate: Double = DEFAULT_INVESTMENT_CAGR): Double {
        val futureVal = calculateFutureValue(price, annualRate, years)
        return futureVal - price
    }

    fun evaluatePurchaseLocal(
        itemName: String,
        price: Double,
        urgency: String, // NEED, WANT
        netDisposableIncome: Double
    ): LocalPurchaseEvaluation {
        val fv5yr = calculateFutureValue(price, DEFAULT_INVESTMENT_CAGR, 5.0)
        val oppCost3yr = calculateOpportunityCost(price, 3.0, DEFAULT_INVESTMENT_CAGR)
        val oppCost5yr = calculateOpportunityCost(price, 5.0, DEFAULT_INVESTMENT_CAGR)

        val isNeed = urgency.equals("NEED", ignoreCase = true)

        val decision: String
        val postponeMonths: Int
        val rationale: String

        if (netDisposableIncome <= 0) {
            decision = if (isNeed) "POSTPONE" else "AVOID"
            postponeMonths = if (isNeed) 3 else 6
            rationale = "Arus kas bulanan Anda saat ini defisit atau nol. Beli '$itemName' seharga Rp ${formatRupiah(price)} berisiko mengganggu kestabilan finansial."
        } else if (isNeed) {
            if (price <= netDisposableIncome) {
                decision = "BUY_NOW"
                postponeMonths = 0
                rationale = "Barang ini tergolong KEBUTUHAN dan harganya (Rp ${formatRupiah(price)}) masih masuk dalam sisa dana bersih bulanan Anda (Rp ${formatRupiah(netDisposableIncome)})."
            } else {
                decision = "POSTPONE"
                val monthsNeeded = ceil(price / (netDisposableIncome * 0.7)).toInt().coerceAtLeast(1)
                postponeMonths = monthsNeeded
                rationale = "Termasuk KEBUTUHAN, namun harga barang melebihi sisa dana bersih bulan ini. Disarankan menunda $monthsNeeded bulan dengan menyisihkan porsi khusus."
            }
        } else {
            // Urgency == WANT
            if (price <= netDisposableIncome * 0.2) {
                decision = "BUY_NOW"
                postponeMonths = 0
                rationale = "Termasuk KEINGINAN minor (<20% sisa dana bersih). Aman dibeli tanpa mengganggu target dana cadangan atau investasi."
            } else if (price <= netDisposableIncome) {
                decision = "POSTPONE"
                postponeMonths = 2
                rationale = "Termasuk KEINGINAN yang memakan porsi besar sisa dana bersih bulan ini. Disarankan tunda 2 bulan agar disiplin investasi tetap terjaga."
            } else {
                decision = "AVOID"
                postponeMonths = 6
                rationale = "Termasuk KEINGINAN yang melebihi sisa dana bersih bulanan. Jika uang Rp ${formatRupiah(price)} ini diinvestasikan, nilainya diproyeksikan tumbuh menjadi Rp ${formatRupiah(fv5yr)} dalam 5 tahun!"
            }
        }

        return LocalPurchaseEvaluation(
            decision = decision,
            postponeMonths = postponeMonths,
            opportunityCost3Yr = oppCost3yr,
            opportunityCost5Yr = oppCost5yr,
            rationale = rationale,
            projectedValue5Yr = fv5yr
        )
    }

    fun getAssetAllocation(riskProfile: String): AssetAllocation {
        return when (riskProfile.uppercase()) {
            "CONSERVATIVE" -> AssetAllocation(
                moneyMarketPercent = 50,
                bondsPercent = 35,
                mutualFundsPercent = 10,
                stocksPercent = 5,
                summaryText = "Fokus utama: Keamanan modal & likuiditas tinggi. Cocok untuk jangka pendek (< 2 tahun)."
            )
            "AGGRESSIVE" -> AssetAllocation(
                moneyMarketPercent = 10,
                bondsPercent = 20,
                mutualFundsPercent = 30,
                stocksPercent = 40,
                summaryText = "Fokus utama: Pertumbuhan kapital maksimal (Capital Growth). Siap menghadapi fluktuasi jangka menengah-panjang."
            )
            else -> AssetAllocation( // MODERATE
                moneyMarketPercent = 20,
                bondsPercent = 40,
                mutualFundsPercent = 25,
                stocksPercent = 15,
                summaryText = "Fokus utama: Keseimbangan antara stabilitas arus kas dan pertumbuhan investasi jangka menengah (2-5 tahun)."
            )
        }
    }

    fun calculateRiskScoreFromAnswers(answers: List<Int>): String {
        // answers array of 5 scores (1 to 3)
        val score = answers.sum()
        return when {
            score <= 7 -> "CONSERVATIVE"
            score <= 11 -> "MODERATE"
            else -> "AGGRESSIVE"
        }
    }

    fun generateProactiveAdvice(
        totalIncome: Double,
        netDisposableIncome: Double,
        riskProfile: String,
        preferShariaAndGold: Boolean = true
    ): ProactiveAiAdvice {
        val inc = totalIncome.coerceAtLeast(1.0)
        val surplus = netDisposableIncome.coerceAtLeast(0.0)
        
        // Recommended monthly investment: 100% of surplus or min 20% of income
        val recommendedMonthly = if (surplus > 0) {
            (surplus * 0.85).coerceAtLeast(inc * 0.20)
        } else {
            inc * 0.10
        }

        val savingsRatePercent = (recommendedMonthly / inc) * 100.0

        val instruments = when (riskProfile.uppercase()) {
            "CONSERVATIVE" -> listOf(
                RecommendedInstrument(
                    name = "Emas Logam Mulia (Antam / Pegadaian Digital)",
                    category = "EMAS",
                    allocationPercent = 40,
                    monthlyAmount = recommendedMonthly * 0.40,
                    expectedReturnCagrPercent = 8.5,
                    riskLevel = "Rendah",
                    description = "Aset lindung nilai inflasi terbaik, likuid, aman, dan mematuhi prinsip syariah tanpa riba."
                ),
                RecommendedInstrument(
                    name = "Sukuk Tabungan (ST / SR) / SBN Syariah",
                    category = "SUKUK_SYARIAH",
                    allocationPercent = 40,
                    monthlyAmount = recommendedMonthly * 0.40,
                    expectedReturnCagrPercent = 6.8,
                    riskLevel = "Rendah",
                    description = "Obligasi syariah terbitan negara dengan imbal hasil (kupon) tetap bulanan 100% dijamin pemerintah."
                ),
                RecommendedInstrument(
                    name = "Reksa Dana Pasar Uang Syariah",
                    category = "REKSA_DANA_SYARIAH",
                    allocationPercent = 20,
                    monthlyAmount = recommendedMonthly * 0.20,
                    expectedReturnCagrPercent = 5.5,
                    riskLevel = "Sangat Rendah",
                    description = "Instrumen sangat likuid untuk simpanan dana darurat tanpa penalti pencairan."
                )
            )
            "AGGRESSIVE" -> listOf(
                RecommendedInstrument(
                    name = "Emas Logam Mulia (Safe Haven & Hedge)",
                    category = "EMAS",
                    allocationPercent = 20,
                    monthlyAmount = recommendedMonthly * 0.20,
                    expectedReturnCagrPercent = 8.5,
                    riskLevel = "Rendah",
                    description = "Jangkar penyeimbang portofolio saat terjadi gejolak pasar modal."
                ),
                RecommendedInstrument(
                    name = "Saham Syariah & Index JII / ISSI",
                    category = "SAHAM_SYARIAH",
                    allocationPercent = 50,
                    monthlyAmount = recommendedMonthly * 0.50,
                    expectedReturnCagrPercent = 13.5,
                    riskLevel = "Tinggi",
                    description = "Fokus pertumbuhan modal maksimal melalui saham-saham syariah berfundamental kokoh."
                ),
                RecommendedInstrument(
                    name = "Sukuk Negara & Reksa Dana Sukuk Syariah",
                    category = "SUKUK_SYARIAH",
                    allocationPercent = 30,
                    monthlyAmount = recommendedMonthly * 0.30,
                    expectedReturnCagrPercent = 7.5,
                    riskLevel = "Sedang",
                    description = "Pendapatan tetap syariah sebagai bantalan arus kas investasi berkala."
                )
            )
            else -> listOf( // MODERATE
                RecommendedInstrument(
                    name = "Emas Logam Mulia (Digital / Fisik)",
                    category = "EMAS",
                    allocationPercent = 30,
                    monthlyAmount = recommendedMonthly * 0.30,
                    expectedReturnCagrPercent = 8.5,
                    riskLevel = "Rendah",
                    description = "Melindungi daya beli uang Anda dari pemangkasan nilai inflasi tahunan."
                ),
                RecommendedInstrument(
                    name = "Sukuk Negara (ST/SR) / Reksa Dana Pendapatan Tetap Syariah",
                    category = "SUKUK_SYARIAH",
                    allocationPercent = 40,
                    monthlyAmount = recommendedMonthly * 0.40,
                    expectedReturnCagrPercent = 7.2,
                    riskLevel = "Rendah-Sedang",
                    description = "Memberikan dividen/kupon halal rutin yang stabil dari aset riil negara."
                ),
                RecommendedInstrument(
                    name = "Reksa Dana Saham Syariah / Indeks Saham Syariah",
                    category = "SAHAM_SYARIAH",
                    allocationPercent = 30,
                    monthlyAmount = recommendedMonthly * 0.30,
                    expectedReturnCagrPercent = 11.0,
                    riskLevel = "Sedang-Tinggi",
                    description = "Memaksimalkan pertumbuhan uang jangka panjang menuju kebebasan finansial (FIRE)."
                )
            )
        }

        val healthText = when {
            surplus >= inc * 0.30 -> "Kondisi arus kas Anda SANGAT SEHAT (Surplus > 30%). Anda berada di jalur cepat menuju Financial Freedom!"
            surplus >= inc * 0.15 -> "Kondisi arus kas Anda CUKUP BAIK. Sisihkan surplus secara disiplin di awal bulan."
            surplus > 0 -> "Arus kas Anda POSITIF namun tipis. Efisiensikan pengeluaran variabel untuk meningkatkan alokasi investasi."
            else -> "Peringatan Arus Kas: Pengeluaran melampaui pendapatan! Evaluasi kewajiban dan tunda barang non-esensial."
        }

        return ProactiveAiAdvice(
            summaryTitle = "Strategi Bebas Finansial (FIRE)",
            financialHealthAssessment = healthText,
            recommendedMonthlyInvestmentAmount = recommendedMonthly,
            recommendedSavingsRatePercent = savingsRatePercent,
            recommendedInstruments = instruments,
            shariaGoldFocusRationale = "Portofolio diprioritaskan pada Emas & Sukuk Syariah: Memberikan ketenangan batin, bebas riba, bebas judi, dan mematuhi kaidah transaksi halal.",
            tacticalSteps = listOf(
                "Otomatiskan pendaftaran autodebet investasi tepat di hari gajian.",
                "Sediakan dana darurat di Reksa Dana Pasar Uang Syariah hingga setara 6x biaya bulanan.",
                "Disiplin rutin membeli Emas Logam Mulia secara bertahap setiap bulan.",
                "Gunakan Fitur 'Boleh Beli Gak Ya?' sebelum membeli barang di luar anggaran."
            )
        )
    }

    fun formatRupiah(amount: Double): String {
        val longVal = amount.toLong()
        val str = longVal.toString()
        val builder = StringBuilder()
        var count = 0
        for (i in str.length - 1 downTo 0) {
            builder.append(str[i])
            count++
            if (count % 3 == 0 && i != 0) {
                builder.append(".")
            }
        }
        return builder.reverse().toString()
    }
}
