package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ExpenseEntity
import com.example.data.model.IncomeEntity
import com.example.data.model.ObligationEntity
import com.example.domain.CashFlowSummary
import com.example.domain.FinancialEngine

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashFlowScreen(
    summary: CashFlowSummary,
    incomes: List<IncomeEntity>,
    obligations: List<ObligationEntity>,
    expenses: List<ExpenseEntity>,
    onOpenAddModal: () -> Unit,
    onDeleteIncome: (IncomeEntity) -> Unit,
    onDeleteObligation: (ObligationEntity) -> Unit,
    onDeleteExpense: (ExpenseEntity) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Income, 1: Obligations, 2: Expenses

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onOpenAddModal,
                icon = { Icon(Icons.Default.Add, contentDescription = "Tambah Transaksi") },
                text = { Text("Catat Arus Kas") },
                modifier = Modifier.testTag("add_cashflow_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("cash_flow_screen"),
            contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Net Disposable Summary Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Kalkulasi Net Disposable Income",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Sisa Dana Bersih = Rp ${FinancialEngine.formatRupiah(summary.netDisposableIncome)}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Formula: Total Pendapatan (Rp ${FinancialEngine.formatRupiah(summary.totalIncome)}) − [Tagihan Tetap (Rp ${FinancialEngine.formatRupiah(summary.totalObligations)}) + Pengeluaran Variabel (Rp ${FinancialEngine.formatRupiah(summary.totalExpenses)})]",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Tabs Selector
            item {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Pendapatan (${incomes.size})") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Tagihan Tetap (${obligations.size})") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Pengeluaran (${expenses.size})") }
                    )
                }
            }

            // Tab Content List
            when (selectedTab) {
                0 -> { // Incomes
                    if (incomes.isEmpty()) {
                        item { EmptyStateMessage("Belum ada data pendapatan. Klik + Catat Arus Kas.") }
                    } else {
                        items(incomes, key = { it.id }) { inc ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(inc.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("${if (inc.type == "ROUTINE") "Pendapatan Rutin" else "Bonus/Non-Rutin"} • Kategori: ${inc.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        "+ Rp ${FinancialEngine.formatRupiah(inc.amount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    IconButton(onClick = { onDeleteIncome(inc) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> { // Obligations
                    if (obligations.isEmpty()) {
                        item { EmptyStateMessage("Belum ada tagihan tetap (Sewa, cicilan, listrik, dll).") }
                    } else {
                        items(obligations, key = { it.id }) { obl ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(obl.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("Jatuh tempo tgl ${obl.dueDayOfMonth} bulanan • ${obl.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        "- Rp ${FinancialEngine.formatRupiah(obl.amount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    IconButton(onClick = { onDeleteObligation(obl) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> { // Expenses
                    if (expenses.isEmpty()) {
                        item { EmptyStateMessage("Belum ada pengeluaran variabel tercatat.") }
                    } else {
                        items(expenses, key = { it.id }) { exp ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(exp.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                        Text("Kategori: ${exp.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Text(
                                        "- Rp ${FinancialEngine.formatRupiah(exp.amount)}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    IconButton(onClick = { onDeleteExpense(exp) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
