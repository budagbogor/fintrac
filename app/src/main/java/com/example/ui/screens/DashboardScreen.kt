package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.GoalEntity
import com.example.data.model.PurchaseInquiryEntity
import com.example.domain.CashFlowSummary
import com.example.domain.FinancialEngine
import com.example.ui.components.HeaderCard
import com.example.ui.theme.DecisionGreen
import com.example.ui.theme.DecisionOrange
import com.example.ui.theme.DecisionRed

import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.RestartAlt

@Composable
fun DashboardScreen(
    summary: CashFlowSummary,
    userName: String,
    riskProfile: String,
    recentInquiries: List<PurchaseInquiryEntity>,
    goals: List<GoalEntity>,
    onOpenPurchaseAssistant: () -> Unit,
    onOpenAddTransaction: () -> Unit,
    onNavigateToCashFlow: () -> Unit,
    onNavigateToGoals: () -> Unit,
    onOpenResetModal: () -> Unit,
    onOpenGuideModal: () -> Unit,
    onOpenAiSettings: () -> Unit,
    onOpenProactiveAiModal: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderCard(
                summary = summary,
                userName = userName,
                riskProfile = riskProfile,
                onOpenPurchaseAssistant = onOpenPurchaseAssistant,
                onOpenAddTransaction = onOpenAddTransaction,
                onOpenResetModal = onOpenResetModal,
                onOpenGuideModal = onOpenGuideModal,
                onOpenAiSettings = onOpenAiSettings
            )
        }

        // Proactive AI Financial Guidance Card
        item {
            val proactiveAdvice = remember(summary, riskProfile) {
                FinancialEngine.generateProactiveAdvice(
                    totalIncome = summary.totalIncome,
                    netDisposableIncome = summary.netDisposableIncome,
                    riskProfile = riskProfile,
                    preferShariaAndGold = true
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("proactive_ai_guidance_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Panduan AI Proaktif FIRE",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Investasi Ideal: Rp ${FinancialEngine.formatRupiah(proactiveAdvice.recommendedMonthlyInvestmentAmount)} / bln",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(onClick = onOpenAiSettings) {
                            Icon(
                                Icons.Default.Psychology,
                                contentDescription = "Setting AI",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "⭐ Diprioritaskan pada Emas Logam Mulia & Sukuk/Reksa Dana Syariah untuk ketenangan finansial jangka panjang.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenProactiveAiModal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("open_proactive_ai_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Buka Rekomendasi Investasi AI", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // FIRE Kebebasan Finansial Card
        item {
            val fireStatus = remember(goals, summary) {
                FinancialEngine.calculateFireStatus(
                    goals = goals,
                    monthlyObligations = summary.totalObligations,
                    monthlyExpenses = summary.totalExpenses,
                    monthlyNetSurplus = summary.netDisposableIncome
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fire_freedom_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = MaterialTheme.colorScheme.tertiary,
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Target Kebebasan Finansial (FIRE)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = fireStatus.statusTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${fireStatus.fireProgressPercent.toInt()}% FIRE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    LinearProgressIndicator(
                        progress = { (fireStatus.fireProgressPercent / 100.0).coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = MaterialTheme.colorScheme.tertiary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total Aset Investasi", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${FinancialEngine.formatRupiah(fireStatus.totalInvestedAssets)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Passive Income / Bln", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${FinancialEngine.formatRupiah(fireStatus.monthlyPassiveIncome)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DecisionGreen)
                        }
                        Column {
                            Text("Target Uang Bebas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Rp ${FinancialEngine.formatRupiah(fireStatus.fireTargetNumber)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Estimasi Bebas Finansial: ${if (fireStatus.yearsToFire <= 0) "Sudah Bebas!" else "± ${fireStatus.yearsToFire.toInt()} Tahun lagi"} dengan konsistensi tabungan saat ini.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Aturan Alokasi 50/30/20 Card
        item {
            val budget = remember(summary) {
                FinancialEngine.calculateBudget503020(
                    totalIncome = summary.totalIncome,
                    totalObligations = summary.totalObligations,
                    totalExpenses = summary.totalExpenses,
                    netSurplus = summary.netDisposableIncome
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("budget_503020_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alokasi Ideal 50/30/20",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = onNavigateToCashFlow,
                            modifier = Modifier.testTag("view_cashflow_detail_button")
                        ) {
                            Text("Atur Cashflow", style = MaterialTheme.typography.labelMedium)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 1: Kebutuhan (50%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Kebutuhan Tetap (Max 50%)", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Rp ${FinancialEngine.formatRupiah(budget.actualNeeds)} / Rp ${FinancialEngine.formatRupiah(budget.targetNeeds)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (budget.isNeedsOk) DecisionGreen else DecisionRed
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (budget.actualNeeds / budget.targetNeeds.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (budget.isNeedsOk) DecisionGreen else DecisionRed
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Keinginan (30%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Keinginan & Gaya Hidup (Max 30%)", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "Rp ${FinancialEngine.formatRupiah(budget.actualWants)} / Rp ${FinancialEngine.formatRupiah(budget.targetWants)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (budget.isWantsOk) DecisionGreen else DecisionOrange
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (budget.actualWants / budget.targetWants.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (budget.isWantsOk) DecisionGreen else DecisionOrange
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 3: Investasi (20%)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Investasi & Tabungan (Min 20%)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Rp ${FinancialEngine.formatRupiah(budget.actualInvestments)} / Rp ${FinancialEngine.formatRupiah(budget.targetInvestments)}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (budget.isInvestmentOk) DecisionGreen else DecisionRed
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (budget.actualInvestments / budget.targetInvestments.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (budget.isInvestmentOk) DecisionGreen else DecisionRed
                    )
                }
            }
        }

        // Recent Smart Purchase Advice Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Riwayat Analisis AI 'Boleh Beli Gak Ya?'",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onOpenPurchaseAssistant) {
                    Text("Tanya AI")
                }
            }
        }

        if (recentInquiries.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Psychology,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Belum Ada Analisis Pembelian",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Bingung mau beli barang baru? Tanya AI Asisten Keuangan untuk analisis rasional dampaknya pada arus kas & investasi Anda.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onOpenPurchaseAssistant,
                            modifier = Modifier.testTag("empty_state_purchase_assistant_button")
                        ) {
                            Text("Mulai Analisis Pembelian")
                        }
                    }
                }
            }
        } else {
            items(recentInquiries.take(3)) { inquiry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (badgeText, badgeColor) = when (inquiry.decision.uppercase()) {
                            "BUY_NOW" -> "Beli Sekarang" to DecisionGreen
                            "POSTPONE" -> "Tunda ${inquiry.postponeMonths} Bln" to DecisionOrange
                            else -> "Jangan Beli" to DecisionRed
                        }

                        Surface(
                            color = badgeColor.copy(alpha = 0.15f),
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = badgeColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = inquiry.itemName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Harga: Rp ${FinancialEngine.formatRupiah(inquiry.price)} • ${if (inquiry.urgency == "NEED") "Kebutuhan" else "Keinginan"}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (inquiry.aiRationale.isNotBlank()) {
                                Text(
                                    text = inquiry.aiRationale,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 2,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = badgeColor,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Investment Goals Summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target Investasi Aktif",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(
                    onClick = onNavigateToGoals,
                    modifier = Modifier.testTag("view_all_goals_button")
                ) {
                    Text("Kelola Target")
                }
            }
        }

        items(goals.take(3)) { goal ->
            val progress = (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
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
                        Text(
                            text = goal.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Terkumpul: Rp ${FinancialEngine.formatRupiah(goal.currentAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Target: Rp ${FinancialEngine.formatRupiah(goal.targetAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Petunjuk Penggunaan Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("user_guide_dashboard_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Petunjuk Penggunaan Aplikasi",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Pelajari cara kerja Sisa Dana Bersih, Asisten AI, & Target Investasi.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onOpenGuideModal,
                        modifier = Modifier.testTag("open_user_guide_card_button")
                    ) {
                        Icon(Icons.Default.HelpOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Panduan")
                    }
                }
            }
        }

        // Reset Data for New User Option
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_data_dashboard_card"),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Reset Data / Pengguna Baru",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kosongkan data keuangan agar siap dipakai oleh pengguna baru.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onOpenResetModal,
                        modifier = Modifier.testTag("reset_data_card_button")
                    ) {
                        Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reset")
                    }
                }
            }
        }
    }
}
