package com.example.data.repository

import com.example.data.local.FinAdvisorDao
import com.example.data.model.*
import com.example.data.remote.GeminiPurchaseAdvisor
import com.example.data.remote.PurchaseAdviceResponse
import kotlinx.coroutines.flow.Flow

class FinAdvisorRepository(private val dao: FinAdvisorDao) {

    val allIncomes: Flow<List<IncomeEntity>> = dao.getAllIncomes()
    val allObligations: Flow<List<ObligationEntity>> = dao.getAllObligations()
    val allExpenses: Flow<List<ExpenseEntity>> = dao.getAllExpenses()
    val allGoals: Flow<List<GoalEntity>> = dao.getAllGoals()
    val allPurchaseInquiries: Flow<List<PurchaseInquiryEntity>> = dao.getAllPurchaseInquiries()
    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val allSavedPromos: Flow<List<PromoEntity>> = dao.getAllSavedPromos()

    suspend fun addIncome(income: IncomeEntity) = dao.insertIncome(income)
    suspend fun deleteIncome(income: IncomeEntity) = dao.deleteIncome(income)

    suspend fun addObligation(obligation: ObligationEntity) = dao.insertObligation(obligation)
    suspend fun deleteObligation(obligation: ObligationEntity) = dao.deleteObligation(obligation)

    suspend fun addExpense(expense: ExpenseEntity) = dao.insertExpense(expense)
    suspend fun deleteExpense(expense: ExpenseEntity) = dao.deleteExpense(expense)

    suspend fun addGoal(goal: GoalEntity) = dao.insertGoal(goal)
    suspend fun updateGoal(goal: GoalEntity) = dao.updateGoal(goal)
    suspend fun deleteGoal(goal: GoalEntity) = dao.deleteGoal(goal)

    suspend fun saveInquiry(inquiry: PurchaseInquiryEntity) = dao.insertPurchaseInquiry(inquiry)
    suspend fun saveUserProfile(profile: UserProfileEntity) = dao.saveUserProfile(profile)

    suspend fun savePromo(promo: PromoEntity) = dao.insertSavedPromo(promo)
    suspend fun deletePromo(promo: PromoEntity) = dao.deleteSavedPromo(promo)
    suspend fun isPromoSaved(title: String, merchant: String): Boolean {
        return dao.getSavedPromoByTitleAndMerchant(title, merchant) != null
    }

    suspend fun clearAllData() {
        dao.clearIncomes()
        dao.clearObligations()
        dao.clearExpenses()
        dao.clearGoals()
        dao.clearPurchaseInquiries()
        dao.clearSavedPromos()
    }

    suspend fun analyzePurchase(
        itemName: String,
        price: Double,
        urgency: String,
        netDisposableIncome: Double,
        activeGoals: List<GoalEntity>,
        customApiKey: String? = null,
        customModel: String? = null,
        customBaseUrl: String? = null
    ): PurchaseAdviceResponse {
        return GeminiPurchaseAdvisor.analyzePurchaseInquiry(
            itemName = itemName,
            price = price,
            urgency = urgency,
            netDisposableIncome = netDisposableIncome,
            activeGoals = activeGoals,
            customApiKey = customApiKey,
            customModel = customModel,
            customBaseUrl = customBaseUrl
        )
    }
}
