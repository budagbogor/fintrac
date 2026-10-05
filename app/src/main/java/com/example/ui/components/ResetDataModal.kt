package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResetDataModal(
    onDismiss: () -> Unit,
    onConfirmReset: (newUserName: String, withSampleData: Boolean) -> Unit
) {
    var newUserName by remember { mutableStateOf("Pengguna Baru") }
    var resetOption by remember { mutableStateOf(0) } // 0: Kosongkan Data (Mulai dari Nol), 1: Gunakan Sample Data Baru

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("reset_data_modal")
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Reset Data / Pengguna Baru",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fitur ini digunakan untuk mengosongkan aplikasi agar siap digunakan oleh pengguna baru atau memulai ulang pencatatan.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newUserName,
                onValueChange = { newUserName = it },
                label = { Text("Nama Pengguna Baru") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_user_name_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Opsi Reset Data:", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (resetOption == 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = { resetOption = 0 },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = resetOption == 0, onClick = { resetOption = 0 })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Mulai Bersih dari Nol (0 Rp)", fontWeight = FontWeight.Bold)
                        Text("Menghapus seluruh transaksi, tagihan, dan target agar Anda dapat menginput data keuangan asli.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (resetOption == 1) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                ),
                onClick = { resetOption = 1 },
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = resetOption == 1, onClick = { resetOption = 1 })
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Reset dengan Sample Data Baru", fontWeight = FontWeight.Bold)
                        Text("Mengisi ulang aplikasi dengan contoh data pendapatan & pengeluaran awal.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    onConfirmReset(newUserName, resetOption == 1)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("confirm_reset_data_button")
            ) {
                Text("Konfirmasi & Reset Data Baru", fontWeight = FontWeight.Bold)
            }
        }
    }
}
