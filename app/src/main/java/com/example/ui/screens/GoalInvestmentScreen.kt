package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Shield
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
import com.example.domain.AssetAllocation
import com.example.domain.FinancialEngine

import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Star

@Composable
fun GoalInvestmentScreen(
    goals: List<GoalEntity>,
    riskProfile: String,
    onOpenAddGoalModal: () -> Unit,
    onOpenQuizModal: () -> Unit,
    onTopUpGoal: (goal: GoalEntity, addedAmount: Double) -> Unit,
    onDeleteGoal: (GoalEntity) -> Unit,
    onOpenProactiveAiModal: () -> Unit = {}
) {
    val assetAlloc = remember(riskProfile) {
        FinancialEngine.getAssetAllocation(riskProfile)
    }

    var goalToTopUp by remember { mutableStateOf<GoalEntity?>(null) }
    var topUpAmountText by remember { mutableStateOf("") }

    if (goalToTopUp != null) {
        AlertDialog(
            onDismissRequest = { goalToTopUp = null },
            title = { Text("Tambah Dana Investasi") },
            text = {
                Column {
                    Text("Target: ${goalToTopUp?.title}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = topUpAmountText,
                        onValueChange = { topUpAmountText = it },
                        label = { Text("Nominal Top Up (Rp)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = topUpAmountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0 && goalToTopUp != null) {
                            onTopUpGoal(goalToTopUp!!, amt)
                        }
                        goalToTopUp = null
                        topUpAmountText = ""
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToTopUp = null }) {
                    Text("Batal")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("goal_investment_screen"),
        contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Sharia & Gold AI Banner
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF8E1) // Warm Gold background
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFF8F00),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Opsi Utama: Syariah & Emas Mulia",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D4037)
                            )
                        }

                        Button(
                            onClick = onOpenProactiveAiModal,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF8F00),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Panduan AI", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Alokasikan dana investasi Anda ke instrumen Syariah (Sukuk Negara, Reksa Dana Syariah, Saham JII) & Emas Antam untuk pertumbuhan stabil tanpa riba.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF4E342E)
                    )
                }
            }
        }

        // Risk Profile Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Profil Risiko Investasi Anda",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = when (riskProfile.uppercase()) {
                                    "CONSERVATIVE" -> "Konservatif (Pencari Keamanan)"
                                    "AGGRESSIVE" -> "Agresif (Pencari Pertumbuhan)"
                                    else -> "Moderat (Pencari Keseimbangan)"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        OutlinedButton(
                            onClick = onOpenQuizModal,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                            modifier = Modifier.testTag("quiz_modal_button")
                        ) {
                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Uji Profil")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = assetAlloc.summaryText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Dynamic Asset Allocation Breakdown Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Strategi Rekomendasi Alokasi Aset",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    AssetBar(label = "Pasar Uang / Deposito", percent = assetAlloc.moneyMarketPercent, color = Color(0xFF10B981))
                    Spacer(modifier = Modifier.height(10.dp))
                    AssetBar(label = "Obligasi Negara / SBN", percent = assetAlloc.bondsPercent, color = Color(0xFF3B82F6))
                    Spacer(modifier = Modifier.height(10.dp))
                    AssetBar(label = "Reksa Dana Pendapatan Tetap", percent = assetAlloc.mutualFundsPercent, color = Color(0xFF8B5CF6))
                    Spacer(modifier = Modifier.height(10.dp))
                    AssetBar(label = "Saham / ETF Pasar Modal", percent = assetAlloc.stocksPercent, color = Color(0xFFF59E0B))
                }
            }
        }

        // Financial Goals Header & Add FAB
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Goal-Based Investing (${goals.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onOpenAddGoalModal,
                    modifier = Modifier.testTag("add_goal_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Target")
                }
            }
        }

        if (goals.isEmpty()) {
            item { EmptyStateMessage("Belum ada target keuangan. Klik Tambah Target untuk mulai menyusun strategi.") }
        } else {
            items(goals, key = { it.id }) { goal ->
                val progress = (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0).toFloat()
                val fvProjected = FinancialEngine.calculateFutureValue(
                    principal = goal.currentAmount,
                    annualRate = goal.expectedCagrPercentage / 100.0,
                    years = goal.targetHorizonMonths / 12.0
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(goal.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { onDeleteGoal(goal) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Target: Rp ${FinancialEngine.formatRupiah(goal.targetAmount)} (${goal.targetHorizonMonths} Bulan)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "${(progress * 100).toInt()}%",
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Terkumpul: Rp ${FinancialEngine.formatRupiah(goal.currentAmount)}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Text("Proyeksi Compound (CAGR ${goal.expectedCagrPercentage}%): Rp ${FinancialEngine.formatRupiah(fvProjected)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                            }

                            TextButton(onClick = { goalToTopUp = goal }) {
                                Text("+ Top Up")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AssetBar(label: String, percent: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
            Text("$percent%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percent / 100.0).toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color
        )
    }
}
