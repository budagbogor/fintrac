package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.model.PurchaseInquiryEntity
import com.example.domain.FinancialEngine
import com.example.ui.theme.DecisionGreen
import com.example.ui.theme.DecisionOrange
import com.example.ui.theme.DecisionRed
import com.example.ui.viewmodel.PurchaseAnalysisUiState

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PurchaseAssistantScreen(
    analysisState: PurchaseAnalysisUiState,
    inquiryHistory: List<PurchaseInquiryEntity>,
    netDisposableIncome: Double,
    onAnalyze: (itemName: String, price: Double, urgency: String) -> Unit,
    onReset: () -> Unit
) {
    var itemName by rememberSaveable { mutableStateOf("") }
    var priceText by rememberSaveable { mutableStateOf("") }
    var urgency by rememberSaveable { mutableStateOf("WANT") } // NEED vs WANT

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    BackHandler(enabled = isImeVisible) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("purchase_assistant_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Input Form Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("purchase_inquiry_form_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Input Rencana Pembelian",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = itemName,
                        onValueChange = { itemName = it },
                        label = { Text("Nama Barang / Jasa") },
                        placeholder = { Text("misal: Sepatu Lari, Gadget Baru, Liburan") },
                        leadingIcon = { Icon(Icons.Default.ShoppingBag, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_name_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Harga Barang (Rp)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                        }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("item_price_input"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Tingkat Urgensi / Kebutuhan", style = MaterialTheme.typography.labelLarge)
                    Row(
                        modifier = Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = urgency == "NEED",
                            onClick = { urgency = "NEED" },
                            label = { Text("Kebutuhan (Mendesak)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = urgency == "WANT",
                            onClick = { urgency = "WANT" },
                            label = { Text("Keinginan (Opsional)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            val p = priceText.toDoubleOrNull() ?: 0.0
                            if (itemName.isNotBlank() && p > 0) {
                                onAnalyze(itemName, p, urgency)
                            }
                        },
                        enabled = analysisState !is PurchaseAnalysisUiState.Loading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("analyze_purchase_button")
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (analysisState is PurchaseAnalysisUiState.Loading) "Menganalisis..." else "Analisis 'Boleh Beli Gak Ya?'",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Analysis State Outcome Card
        when (analysisState) {
            is PurchaseAnalysisUiState.Loading -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Menganalisis dengan Gemini AI Advisor...",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Mengevaluasi sisa dana bersih, target investasi, dan kalkulasi opportunity cost pasar modal.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            is PurchaseAnalysisUiState.Success -> {
                val advice = analysisState.advice
                val (badgeTitle, badgeColor) = when (advice.recommendation.uppercase()) {
                    "BUY_NOW" -> "Rekomendasi: Beli Sekarang" to DecisionGreen
                    "POSTPONE" -> "Rekomendasi: Tunda ${advice.postponeMonths} Bulan" to DecisionOrange
                    else -> "Rekomendasi: Jangan Beli" to DecisionRed
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ai_analysis_result_card"),
                        colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Badge
                            Surface(
                                color = badgeColor,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = badgeTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Barang: ${analysisState.itemName} (Rp ${FinancialEngine.formatRupiah(analysisState.price)})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = advice.summaryReason,
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Opportunity Cost Card
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Opportunity Cost (Biaya Peluang)",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = advice.opportunityCostAnalysis,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            if (advice.financialTips.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Saran Finansial Taktis:", fontWeight = FontWeight.Bold)
                                advice.financialTips.forEach { tip ->
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(tip, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            OutlinedButton(
                                onClick = onReset,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Tanya Barang Lain")
                            }
                        }
                    }
                }
            }

            is PurchaseAnalysisUiState.Error -> {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Gagal Menganalisis", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Text(analysisState.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = onReset) { Text("Coba Lagi") }
                        }
                    }
                }
            }

            else -> {}
        }

        // History Section
        item {
            Text(
                text = "Riwayat Analisis Pembelian",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (inquiryHistory.isEmpty()) {
            item { EmptyStateMessage("Belum ada riwayat analisis.") }
        } else {
            items(inquiryHistory) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.itemName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            val color = when(item.decision.uppercase()) {
                                "BUY_NOW" -> DecisionGreen
                                "POSTPONE" -> DecisionOrange
                                else -> DecisionRed
                            }
                            Text(
                                text = when(item.decision.uppercase()) {
                                    "BUY_NOW" -> "Beli Sekarang"
                                    "POSTPONE" -> "Tunda ${item.postponeMonths} Bln"
                                    else -> "Jangan Beli"
                                },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Harga: Rp ${FinancialEngine.formatRupiah(item.price)} • ${if (item.urgency == "NEED") "Kebutuhan" else "Keinginan"}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (item.aiRationale.isNotBlank()) {
                            Text(item.aiRationale, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}
