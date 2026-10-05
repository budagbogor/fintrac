package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.LocalOffer
import com.example.ui.screens.PromoSearchScreen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddGoalModal
import com.example.ui.components.AddTransactionModal
import com.example.ui.components.AiSettingsModal
import com.example.ui.components.ProactiveAiAdvisorModal
import com.example.ui.components.ResetDataModal
import com.example.ui.components.RiskProfileQuizModal
import com.example.ui.components.UserGuideModal
import com.example.ui.screens.CashFlowScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GoalInvestmentScreen
import com.example.ui.screens.PurchaseAssistantScreen
import com.example.ui.theme.FinAdvisorTheme
import com.example.ui.viewmodel.FinAdvisorViewModel

enum class AppNavScreen(val title: String, val icon: ImageVector, val tag: String) {
    DASHBOARD("Ringkasan", Icons.Default.Dashboard, "nav_dashboard"),
    CASHFLOW("Arus Kas", Icons.Default.Payments, "nav_cashflow"),
    PURCHASE_AI("Boleh Beli?", Icons.Default.Psychology, "nav_purchase_ai"),
    PROMOS("Cari Promo", Icons.Default.LocalOffer, "nav_promos"),
    GOALS("Investasi", Icons.Default.TrendingUp, "nav_goals")
}

class MainActivity : ComponentActivity() {

    private val viewModel: FinAdvisorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            FinAdvisorTheme {
                val currentScreen = remember { mutableStateOf(AppNavScreen.DASHBOARD) }

                val summary by viewModel.cashFlowSummary.collectAsStateWithLifecycle()
                val incomes by viewModel.incomes.collectAsStateWithLifecycle()
                val obligations by viewModel.obligations.collectAsStateWithLifecycle()
                val expenses by viewModel.expenses.collectAsStateWithLifecycle()
                val goals by viewModel.goals.collectAsStateWithLifecycle()
                val purchaseInquiries by viewModel.purchaseInquiries.collectAsStateWithLifecycle()
                val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
                val analysisState by viewModel.purchaseAnalysisState.collectAsStateWithLifecycle()
                val promoSearchState by viewModel.promoSearchState.collectAsStateWithLifecycle()
                val savedPromos by viewModel.savedPromos.collectAsStateWithLifecycle()

                val userName = userProfile?.fullName ?: "Pengguna FinAdvisor"
                val riskProfile = userProfile?.riskProfile ?: "MODERATE"

                var showAddTransactionModal by remember { mutableStateOf(false) }
                var showAddGoalModal by remember { mutableStateOf(false) }
                var showRiskQuizModal by remember { mutableStateOf(false) }
                var showResetModal by remember { mutableStateOf(false) }
                var showGuideModal by remember { mutableStateOf(false) }
                var showAiSettingsModal by remember { mutableStateOf(false) }
                var showProactiveAiModal by remember { mutableStateOf(false) }

                val isAnyModalOpen = showAddTransactionModal || showAddGoalModal || showRiskQuizModal ||
                        showResetModal || showGuideModal || showAiSettingsModal || showProactiveAiModal

                BackHandler(enabled = !isAnyModalOpen && currentScreen.value != AppNavScreen.DASHBOARD) {
                    currentScreen.value = AppNavScreen.DASHBOARD
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_navigation_bar")
                        ) {
                            AppNavScreen.values().forEach { screen ->
                                NavigationBarItem(
                                    selected = currentScreen.value == screen,
                                    onClick = { currentScreen.value = screen },
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title) },
                                    modifier = Modifier.testTag(screen.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen.value,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_transition"
                        ) { target ->
                            when (target) {
                                AppNavScreen.DASHBOARD -> DashboardScreen(
                                    summary = summary,
                                    userName = userName,
                                    riskProfile = riskProfile,
                                    recentInquiries = purchaseInquiries,
                                    goals = goals,
                                    onOpenPurchaseAssistant = { currentScreen.value = AppNavScreen.PURCHASE_AI },
                                    onOpenAddTransaction = { showAddTransactionModal = true },
                                    onNavigateToCashFlow = { currentScreen.value = AppNavScreen.CASHFLOW },
                                    onNavigateToGoals = { currentScreen.value = AppNavScreen.GOALS },
                                    onOpenResetModal = { showResetModal = true },
                                    onOpenGuideModal = { showGuideModal = true },
                                    onOpenAiSettings = { showAiSettingsModal = true },
                                    onOpenProactiveAiModal = { showProactiveAiModal = true }
                                )

                                AppNavScreen.CASHFLOW -> CashFlowScreen(
                                    summary = summary,
                                    incomes = incomes,
                                    obligations = obligations,
                                    expenses = expenses,
                                    onOpenAddModal = { showAddTransactionModal = true },
                                    onDeleteIncome = { viewModel.deleteIncome(it) },
                                    onDeleteObligation = { viewModel.deleteObligation(it) },
                                    onDeleteExpense = { viewModel.deleteExpense(it) }
                                )

                                AppNavScreen.PURCHASE_AI -> PurchaseAssistantScreen(
                                    analysisState = analysisState,
                                    inquiryHistory = purchaseInquiries,
                                    netDisposableIncome = summary.netDisposableIncome,
                                    onAnalyze = { name, price, urgency ->
                                        viewModel.analyzePurchaseInquiry(name, price, urgency)
                                    },
                                    onReset = { viewModel.resetPurchaseAnalysis() }
                                )

                                AppNavScreen.PROMOS -> PromoSearchScreen(
                                    searchState = promoSearchState,
                                    savedPromos = savedPromos,
                                    onSearch = { query, mode, isOnline ->
                                        viewModel.searchPromos(query, mode, isOnline)
                                    },
                                    onToggleSave = { promo ->
                                        viewModel.toggleSavePromo(promo)
                                    },
                                    onUnsave = { entity ->
                                        viewModel.unsavePromo(entity)
                                    },
                                    onResetSearch = {
                                        viewModel.clearPromoSearch()
                                    }
                                )

                                AppNavScreen.GOALS -> GoalInvestmentScreen(
                                    goals = goals,
                                    riskProfile = riskProfile,
                                    onOpenAddGoalModal = { showAddGoalModal = true },
                                    onOpenQuizModal = { showRiskQuizModal = true },
                                    onTopUpGoal = { goal, amt -> viewModel.updateGoalProgress(goal, amt) },
                                    onDeleteGoal = { viewModel.deleteGoal(it) },
                                    onOpenProactiveAiModal = { showProactiveAiModal = true }
                                )
                            }
                        }
                    }
                }

                // Modals
                if (showAddTransactionModal) {
                    AddTransactionModal(
                        onDismiss = { showAddTransactionModal = false },
                        onSaveIncome = { title, amt, type, cat -> viewModel.addIncome(title, amt, type, cat) },
                        onSaveObligation = { title, amt, cat, due -> viewModel.addObligation(title, amt, cat, due) },
                        onSaveExpense = { title, amt, cat -> viewModel.addExpense(title, amt, cat) }
                    )
                }

                if (showAddGoalModal) {
                    AddGoalModal(
                        onDismiss = { showAddGoalModal = false },
                        onSaveGoal = { title, cat, targetAmt, currentAmt, months, profile ->
                            viewModel.addGoal(title, cat, targetAmt, currentAmt, months, profile)
                        }
                    )
                }

                if (showRiskQuizModal) {
                    RiskProfileQuizModal(
                        onDismiss = { showRiskQuizModal = false },
                        onSaveProfile = { profile -> viewModel.saveRiskProfileResult(profile) }
                    )
                }

                if (showResetModal) {
                    ResetDataModal(
                        onDismiss = { showResetModal = false },
                        onConfirmReset = { newUserName, withSample ->
                            viewModel.resetAllData(newUserName, withSample)
                        }
                    )
                }

                if (showGuideModal) {
                    UserGuideModal(
                        onDismiss = { showGuideModal = false }
                    )
                }

                if (showAiSettingsModal) {
                    AiSettingsModal(
                        currentProvider = userProfile?.aiProvider ?: "Sumopod",
                        currentModel = userProfile?.aiModel ?: "gemini/gemini-3.5-flash",
                        currentApiKey = userProfile?.apiKey ?: "",
                        currentBaseUrl = userProfile?.aiBaseUrl ?: "https://api.sumopod.com/v1",
                        currentPreferShariaGold = userProfile?.preferShariaAndGold ?: true,
                        onDismiss = { showAiSettingsModal = false },
                        onSaveSettings = { provider, model, apiKey, baseUrl, preferShariaGold ->
                            viewModel.saveAiSettings(provider, model, apiKey, baseUrl, preferShariaGold)
                        }
                    )
                }

                if (showProactiveAiModal) {
                    val advice = viewModel.getProactiveAiAdvice()
                    ProactiveAiAdvisorModal(
                        advice = advice,
                        aiProviderName = userProfile?.aiProvider.takeIf { !it.isNullOrBlank() } ?: "Gemini",
                        aiModelName = userProfile?.aiModel.takeIf { !it.isNullOrBlank() } ?: "gemini-1.5-flash",
                        onDismiss = { showProactiveAiModal = false },
                        onOpenAiSettings = {
                            showProactiveAiModal = false
                            showAiSettingsModal = true
                        },
                        onAddRecommendedGoal = { title, cat, targetAmt, riskProf ->
                            viewModel.addGoal(title, cat, targetAmt, 0.0, 24, riskProf)
                            showProactiveAiModal = false
                        }
                    )
                }
            }
        }
    }
}
