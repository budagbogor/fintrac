package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FinAdvisorDao {
    // Incomes
    @Query("SELECT * FROM incomes ORDER BY dateEpochMs DESC")
    fun getAllIncomes(): Flow<List<IncomeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncome(income: IncomeEntity): Long

    @Delete
    suspend fun deleteIncome(income: IncomeEntity)

    @Query("DELETE FROM incomes")
    suspend fun clearIncomes()

    // Obligations
    @Query("SELECT * FROM obligations ORDER BY dueDayOfMonth ASC")
    fun getAllObligations(): Flow<List<ObligationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertObligation(obligation: ObligationEntity): Long

    @Delete
    suspend fun deleteObligation(obligation: ObligationEntity)

    @Query("DELETE FROM obligations")
    suspend fun clearObligations()

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY dateEpochMs DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses")
    suspend fun clearExpenses()

    // Goals
    @Query("SELECT * FROM financial_goals ORDER BY targetHorizonMonths ASC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("DELETE FROM financial_goals")
    suspend fun clearGoals()

    // Purchase Inquiries
    @Query("SELECT * FROM purchase_inquiries ORDER BY timestampMs DESC")
    fun getAllPurchaseInquiries(): Flow<List<PurchaseInquiryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseInquiry(inquiry: PurchaseInquiryEntity): Long

    @Query("DELETE FROM purchase_inquiries")
    suspend fun clearPurchaseInquiries()

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfileEntity)

    // Saved Promotions
    @Query("SELECT * FROM saved_promos ORDER BY timestampMs DESC")
    fun getAllSavedPromos(): Flow<List<PromoEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedPromo(promo: PromoEntity): Long

    @Delete
    suspend fun deleteSavedPromo(promo: PromoEntity)

    @Query("DELETE FROM saved_promos")
    suspend fun clearSavedPromos()

    @Query("SELECT * FROM saved_promos WHERE promoTitle = :title AND merchantName = :merchant LIMIT 1")
    suspend fun getSavedPromoByTitleAndMerchant(title: String, merchant: String): PromoEntity?
}
