package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.remote.PurchaseAdviceResponse
import com.example.data.repository.FinAdvisorRepository
import com.example.domain.AssetAllocation
import com.example.domain.CashFlowSummary
import com.example.domain.FinancialEngine
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface PurchaseAnalysisUiState {
    object Idle : PurchaseAnalysisUiState
    object Loading : PurchaseAnalysisUiState
    data class Success(
        val advice: PurchaseAdviceResponse,
        val itemName: String,
        val price: Double,
        val urgency: String
    ) : PurchaseAnalysisUiState
    data class Error(val message: String) : PurchaseAnalysisUiState
}

sealed interface PromoSearchUiState {
    object Idle : PromoSearchUiState
    object Loading : PromoSearchUiState
    data class Success(val results: List<com.example.data.remote.PromoResult>) : PromoSearchUiState
    data class Error(val message: String) : PromoSearchUiState
}

class FinAdvisorViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinAdvisorRepository

    val incomes: StateFlow<List<IncomeEntity>>
    val obligations: StateFlow<List<ObligationEntity>>
    val expenses: StateFlow<List<ExpenseEntity>>
    val goals: StateFlow<List<GoalEntity>>
    val purchaseInquiries: StateFlow<List<PurchaseInquiryEntity>>
    val userProfile: StateFlow<UserProfileEntity?>
    val savedPromos: StateFlow<List<PromoEntity>>

    val cashFlowSummary: StateFlow<CashFlowSummary>

    private val _purchaseAnalysisState = MutableStateFlow<PurchaseAnalysisUiState>(PurchaseAnalysisUiState.Idle)
    val purchaseAnalysisState: StateFlow<PurchaseAnalysisUiState> = _purchaseAnalysisState.asStateFlow()

    private val _promoSearchState = MutableStateFlow<PromoSearchUiState>(PromoSearchUiState.Idle)
    val promoSearchState: StateFlow<PromoSearchUiState> = _promoSearchState.asStateFlow()

    init {
        val dao = AppDatabase.getDatabase(application).finAdvisorDao()
        repository = FinAdvisorRepository(dao)

        incomes = repository.allIncomes.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        obligations = repository.allObligations.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        expenses = repository.allExpenses.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        goals = repository.allGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        purchaseInquiries = repository.allPurchaseInquiries.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        userProfile = repository.userProfile.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        savedPromos = repository.allSavedPromos.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        cashFlowSummary = combine(incomes, obligations, expenses) { inc, obl, exp ->
            FinancialEngine.calculateCashFlow(inc, obl, exp)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CashFlowSummary(0.0, 0.0, 0.0, 0.0, 0.0, 0, "Memuat data...")
        )

        // Seed initial data if DB is empty
        viewModelScope.launch {
            seedInitialDataIfEmpty()
        }
    }

    private suspend fun seedInitialDataIfEmpty() {
        val currentInc = incomes.first()
        if (currentInc.isEmpty()) {
            seedSampleData()
        }
    }

    private suspend fun seedSampleData() {
        repository.addIncome(IncomeEntity(title = "Gaji Bulanan Utama", amount = 12500000.0, type = "ROUTINE", category = "Gaji"))
        repository.addIncome(IncomeEntity(title = "Bonus / Side Income", amount = 2500000.0, type = "NON_ROUTINE", category = "Freelance"))

        repository.addObligation(ObligationEntity(title = "Sewa / Cicilan Rumah", amount = 3500000.0, category = "SEWA", dueDayOfMonth = 5))
        repository.addObligation(ObligationEntity(title = "Cicilan Kendaraan", amount = 1800000.0, category = "CICILAN", dueDayOfMonth = 10))
        repository.addObligation(ObligationEntity(title = "Tagihan Listrik & Internet", amount = 750000.0, category = "LISTRIK", dueDayOfMonth = 15))

        repository.addExpense(ExpenseEntity(title = "Belanja Groceries / Makanan", amount = 2200000.0, category = "MAKANAN"))
        repository.addExpense(ExpenseEntity(title = "Bensin & Transportasi", amount = 850000.0, category = "TRANSPORT"))
        repository.addExpense(ExpenseEntity(title = "Hiburan & Outing Weekend", amount = 900000.0, category = "HIBURAN"))

        repository.addGoal(GoalEntity(title = "Dana Darurat (6x Pengeluaran)", category = "EMERGENCY_FUND", targetAmount = 50000000.0, currentAmount = 22500000.0, targetHorizonMonths = 12, riskProfile = "CONSERVATIVE", expectedCagrPercentage = 6.0))
        repository.addGoal(GoalEntity(title = "DP Rumah Pertama", category = "HOUSE_DP", targetAmount = 120000000.0, currentAmount = 35000000.0, targetHorizonMonths = 36, riskProfile = "MODERATE", expectedCagrPercentage = 8.5))
        repository.addGoal(GoalEntity(title = "Dana Pensiun Sejahtera", category = "RETIREMENT", targetAmount = 500000000.0, currentAmount = 45000000.0, targetHorizonMonths = 120, riskProfile = "AGGRESSIVE", expectedCagrPercentage = 11.0))

        repository.saveUserProfile(UserProfileEntity(fullName = "Budi Pratama", riskProfile = "MODERATE", monthlyTargetSavingsRatePercent = 25.0))
    }

    // AI Settings & Proactive Advice
    fun saveAiSettings(provider: String, model: String, apiKey: String, aiBaseUrl: String, preferShariaGold: Boolean) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveUserProfile(
                current.copy(
                    aiProvider = provider,
                    aiModel = model,
                    apiKey = apiKey,
                    aiBaseUrl = aiBaseUrl,
                    preferShariaAndGold = preferShariaGold
                )
            )
        }
    }

    fun getProactiveAiAdvice(): com.example.domain.ProactiveAiAdvice {
        val summary = cashFlowSummary.value
        val profile = userProfile.value ?: UserProfileEntity()
        return FinancialEngine.generateProactiveAdvice(
            totalIncome = summary.totalIncome,
            netDisposableIncome = summary.netDisposableIncome,
            riskProfile = profile.riskProfile,
            preferShariaAndGold = profile.preferShariaAndGold
        )
    }

    fun resetAllData(newUserName: String = "Pengguna Baru", withSampleData: Boolean = false) {
        viewModelScope.launch {
            repository.clearAllData()
            repository.saveUserProfile(UserProfileEntity(fullName = newUserName.ifBlank { "Pengguna Baru" }, riskProfile = "MODERATE"))
            if (withSampleData) {
                seedSampleData()
            }
        }
    }

    // Cash Flow Actions
    fun addIncome(title: String, amount: Double, type: String, category: String) {
        viewModelScope.launch {
            repository.addIncome(IncomeEntity(title = title, amount = amount, type = type, category = category))
        }
    }

    fun deleteIncome(income: IncomeEntity) {
        viewModelScope.launch {
            repository.deleteIncome(income)
        }
    }

    fun addObligation(title: String, amount: Double, category: String, dueDay: Int) {
        viewModelScope.launch {
            repository.addObligation(ObligationEntity(title = title, amount = amount, category = category, dueDayOfMonth = dueDay))
        }
    }

    fun deleteObligation(obligation: ObligationEntity) {
        viewModelScope.launch {
            repository.deleteObligation(obligation)
        }
    }

    fun addExpense(title: String, amount: Double, category: String) {
        viewModelScope.launch {
            repository.addExpense(ExpenseEntity(title = title, amount = amount, category = category))
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    // Goal Actions
    fun addGoal(title: String, category: String, targetAmount: Double, currentAmount: Double, months: Int, riskProfile: String) {
        viewModelScope.launch {
            val cagr = when(riskProfile.uppercase()) {
                "CONSERVATIVE" -> 6.0
                "AGGRESSIVE" -> 11.0
                else -> 8.5
            }
            repository.addGoal(GoalEntity(title = title, category = category, targetAmount = targetAmount, currentAmount = currentAmount, targetHorizonMonths = months, riskProfile = riskProfile, expectedCagrPercentage = cagr))
        }
    }

    fun updateGoalProgress(goal: GoalEntity, addedAmount: Double) {
        viewModelScope.launch {
            val updated = goal.copy(currentAmount = goal.currentAmount + addedAmount)
            repository.updateGoal(updated)
        }
    }

    fun deleteGoal(goal: GoalEntity) {
        viewModelScope.launch {
            repository.deleteGoal(goal)
        }
    }

    // Purchase Decision AI
    fun analyzePurchaseInquiry(itemName: String, price: Double, urgency: String) {
        viewModelScope.launch {
            _purchaseAnalysisState.value = PurchaseAnalysisUiState.Loading
            try {
                val netDisposable = cashFlowSummary.value.netDisposableIncome
                val activeGoalsList = goals.value
                val profile = userProfile.value
                val response = repository.analyzePurchase(
                    itemName = itemName,
                    price = price,
                    urgency = urgency,
                    netDisposableIncome = netDisposable,
                    activeGoals = activeGoalsList,
                    customApiKey = profile?.apiKey,
                    customModel = profile?.aiModel,
                    customBaseUrl = profile?.aiBaseUrl
                )
                
                val oppCost3yr = FinancialEngine.calculateOpportunityCost(price, 3.0)
                val oppCost5yr = FinancialEngine.calculateOpportunityCost(price, 5.0)

                // Save inquiry to log
                val inquiry = PurchaseInquiryEntity(
                    itemName = itemName,
                    price = price,
                    urgency = urgency,
                    decision = response.recommendation,
                    postponeMonths = response.postponeMonths,
                    opportunityCost3Yr = oppCost3yr,
                    opportunityCost5Yr = oppCost5yr,
                    aiRationale = response.summaryReason
                )
                repository.saveInquiry(inquiry)

                _purchaseAnalysisState.value = PurchaseAnalysisUiState.Success(
                    advice = response,
                    itemName = itemName,
                    price = price,
                    urgency = urgency
                )
            } catch (e: Exception) {
                _purchaseAnalysisState.value = PurchaseAnalysisUiState.Error(e.message ?: "Terjadi kesalahan saat memproses analisis.")
            }
        }
    }

    fun resetPurchaseAnalysis() {
        _purchaseAnalysisState.value = PurchaseAnalysisUiState.Idle
    }

    // Risk Profile Quiz
    fun saveRiskProfileResult(riskProfile: String) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveUserProfile(current.copy(riskProfile = riskProfile))
        }
    }

    fun getAssetAllocation(riskProfile: String): AssetAllocation {
        return FinancialEngine.getAssetAllocation(riskProfile)
    }

    // Promo Search Actions
    fun searchPromos(query: String, searchMode: String, isOnlineOnly: Boolean) {
        viewModelScope.launch {
            _promoSearchState.value = PromoSearchUiState.Loading
            try {
                val profile = userProfile.value
                val results = com.example.data.remote.GeminiPromoSearcher.searchPromos(
                    query = query,
                    searchMode = searchMode,
                    isOnlineOnly = isOnlineOnly,
                    customApiKey = profile?.apiKey,
                    customModel = profile?.aiModel,
                    customBaseUrl = profile?.aiBaseUrl
                )
                _promoSearchState.value = PromoSearchUiState.Success(results)
            } catch (e: Exception) {
                _promoSearchState.value = PromoSearchUiState.Error(e.message ?: "Terjadi kesalahan saat mencari promo.")
            }
        }
    }

    fun clearPromoSearch() {
        _promoSearchState.value = PromoSearchUiState.Idle
    }

    fun toggleSavePromo(promoResult: com.example.data.remote.PromoResult) {
        viewModelScope.launch {
            val alreadySaved = repository.isPromoSaved(promoResult.promoTitle, promoResult.merchantName)
            if (alreadySaved) {
                val allSaved = savedPromos.first()
                val match = allSaved.find { it.promoTitle == promoResult.promoTitle && it.merchantName == promoResult.merchantName }
                if (match != null) {
                    repository.deletePromo(match)
                }
            } else {
                val entity = PromoEntity(
                    merchantName = promoResult.merchantName,
                    promoTitle = promoResult.promoTitle,
                    discountDetails = promoResult.discountDetails,
                    howToGet = promoResult.howToGet,
                    informationLink = promoResult.informationLink,
                    validityPeriod = promoResult.validityPeriod,
                    isOnline = promoResult.isOnline,
                    isSaved = true
                )
                repository.savePromo(entity)
            }
        }
    }

    fun unsavePromo(promo: PromoEntity) {
        viewModelScope.launch {
            repository.deletePromo(promo)
        }
    }
}
