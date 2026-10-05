package com.example

import com.example.data.model.GoalEntity
import com.example.data.model.IncomeEntity
import com.example.data.model.ObligationEntity
import com.example.data.model.ExpenseEntity
import com.example.domain.FinancialEngine
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCashFlowCalculation() {
        val incomes = listOf(IncomeEntity(id = 1, title = "Gaji Utama", amount = 15_000_000.0))
        val obligations = listOf(ObligationEntity(id = 1, title = "Cicilan Rumah", amount = 4_000_000.0))
        val expenses = listOf(ExpenseEntity(id = 1, title = "Makan Harian", amount = 3_000_000.0))

        val summary = FinancialEngine.calculateCashFlow(incomes, obligations, expenses)
        assertEquals(15_000_000.0, summary.totalIncome, 0.01)
        assertEquals(4_000_000.0, summary.totalObligations, 0.01)
        assertEquals(3_000_000.0, summary.totalExpenses, 0.01)
        assertEquals(8_000_000.0, summary.netDisposableIncome, 0.01)
        assertTrue(summary.surplusRatioPercent > 50.0)
    }

    @Test
    fun testFireStatusCalculation() {
        val goals = listOf(
            GoalEntity(id = 1, title = "Emas Antam", category = "EMAS", targetAmount = 50_000_000.0, currentAmount = 25_000_000.0, expectedCagrPercentage = 8.5)
        )
        val fireStatus = FinancialEngine.calculateFireStatus(
            goals = goals,
            monthlyObligations = 3_000_000.0,
            monthlyExpenses = 2_000_000.0,
            monthlyNetSurplus = 5_000_000.0
        )

        assertEquals(25_000_000.0, fireStatus.totalInvestedAssets, 0.01)
        assertEquals(5_000_000.0, fireStatus.monthlyExpenses, 0.01)
        assertEquals(1_500_000_000.0, fireStatus.fireTargetNumber, 0.01)
        assertTrue(fireStatus.fireProgressPercent > 0.0)
    }

    @Test
    fun testBudget503020Calculation() {
        val budget = FinancialEngine.calculateBudget503020(
            totalIncome = 10_000_000.0,
            totalObligations = 4_000_000.0,
            totalExpenses = 2_500_000.0,
            netSurplus = 3_500_000.0
        )

        assertEquals(5_000_000.0, budget.targetNeeds, 0.01)
        assertEquals(3_000_000.0, budget.targetWants, 0.01)
        assertEquals(2_000_000.0, budget.targetInvestments, 0.01)
        assertTrue(budget.isNeedsOk)
        assertTrue(budget.isWantsOk)
        assertTrue(budget.isInvestmentOk)
    }

    @Test
    fun testProactiveAiAdviceShariaAndGold() {
        val advice = FinancialEngine.generateProactiveAdvice(
            totalIncome = 12_000_000.0,
            netDisposableIncome = 4_000_000.0,
            riskProfile = "MODERATE",
            preferShariaAndGold = true
        )

        assertNotNull(advice)
        assertTrue(advice.recommendedInstruments.isNotEmpty())
        val hasGold = advice.recommendedInstruments.any { it.category == "EMAS" }
        val hasSukuk = advice.recommendedInstruments.any { it.category == "SUKUK_SYARIAH" }
        assertTrue("Harus menyertakan instrumen Emas Logam Mulia", hasGold)
        assertTrue("Harus menyertakan instrumen Sukuk Syariah", hasSukuk)
        assertTrue(advice.shariaGoldFocusRationale.contains("Emas & Sukuk Syariah"))
    }
}


