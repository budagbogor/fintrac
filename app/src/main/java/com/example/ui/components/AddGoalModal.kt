package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddGoalModal(
    onDismiss: () -> Unit,
    onSaveGoal: (title: String, category: String, targetAmount: Double, currentAmount: Double, months: Int, riskProfile: String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var targetAmountText by rememberSaveable { mutableStateOf("") }
    var currentAmountText by rememberSaveable { mutableStateOf("0") }
    var horizonMonthsText by rememberSaveable { mutableStateOf("12") }
    var selectedCategory by rememberSaveable { mutableStateOf("EMERGENCY_FUND") }
    var selectedRiskProfile by rememberSaveable { mutableStateOf("MODERATE") }

    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val isImeVisible = WindowInsets.isImeVisible

    BackHandler(enabled = isImeVisible) {
        keyboardController?.hide()
        focusManager.clearFocus()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_goal_modal")
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tambah Target Finansial",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Rekomendasi Quick Preset AI:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                AssistChip(
                    onClick = {
                        title = "Tabungan Emas Antam Logam Mulia"
                        selectedCategory = "EMAS"
                        selectedRiskProfile = "CONSERVATIVE"
                        if (targetAmountText.isEmpty()) targetAmountText = "25000000"
                        if (horizonMonthsText.isEmpty()) horizonMonthsText = "24"
                    },
                    label = { Text("Emas Logam Mulia", style = MaterialTheme.typography.labelSmall) }
                )
                AssistChip(
                    onClick = {
                        title = "Sukuk Tabungan (ST) / SBN Syariah"
                        selectedCategory = "SUKUK_SYARIAH"
                        selectedRiskProfile = "CONSERVATIVE"
                        if (targetAmountText.isEmpty()) targetAmountText = "50000000"
                        if (horizonMonthsText.isEmpty()) horizonMonthsText = "36"
                    },
                    label = { Text("Sukuk Syariah", style = MaterialTheme.typography.labelSmall) }
                )
                AssistChip(
                    onClick = {
                        title = "Portofolio Saham Syariah JII/ISSI"
                        selectedCategory = "SAHAM_SYARIAH"
                        selectedRiskProfile = "MODERATE"
                        if (targetAmountText.isEmpty()) targetAmountText = "100000000"
                        if (horizonMonthsText.isEmpty()) horizonMonthsText = "60"
                    },
                    label = { Text("Saham Syariah", style = MaterialTheme.typography.labelSmall) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Nama Target (misal: Dana Darurat, DP Rumah, Emas)") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("goal_title_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it },
                    label = { Text("Target Nominal (Rp)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("goal_target_amount_input"),
                    singleLine = true
                )
                OutlinedTextField(
                    value = currentAmountText,
                    onValueChange = { currentAmountText = it },
                    label = { Text("Dana Terkumpul") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = horizonMonthsText,
                onValueChange = { horizonMonthsText = it },
                label = { Text("Jangka Waktu Target (Bulan)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text("Profil Risiko Strategi", style = MaterialTheme.typography.labelLarge)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                FilterChip(
                    selected = selectedRiskProfile == "CONSERVATIVE",
                    onClick = { selectedRiskProfile = "CONSERVATIVE" },
                    label = { Text("Konservatif") }
                )
                FilterChip(
                    selected = selectedRiskProfile == "MODERATE",
                    onClick = { selectedRiskProfile = "MODERATE" },
                    label = { Text("Moderat") }
                )
                FilterChip(
                    selected = selectedRiskProfile == "AGGRESSIVE",
                    onClick = { selectedRiskProfile = "AGGRESSIVE" },
                    label = { Text("Agresif") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val targetAmt = targetAmountText.toDoubleOrNull() ?: 0.0
                    val currentAmt = currentAmountText.toDoubleOrNull() ?: 0.0
                    val months = horizonMonthsText.toIntOrNull() ?: 12
                    if (title.isNotBlank() && targetAmt > 0) {
                        onSaveGoal(title, selectedCategory, targetAmt, currentAmt, months, selectedRiskProfile)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_goal_button")
            ) {
                Text("Simpan Target Investasi", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
