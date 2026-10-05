package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class GuideItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val steps: List<String>,
    val tip: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserGuideModal(
    onDismiss: () -> Unit
) {
    val guideList = remember {
        listOf(
            GuideItem(
                title = "1. Sisa Dana Bersih (Net Disposable Income)",
                subtitle = "Prinsip utama kesehatan keuangan Anda",
                icon = Icons.Default.AccountBalanceWallet,
                color = Color(0xFF10B981),
                steps = listOf(
                    "Sisa Dana Bersih = Total Pendapatan - (Tagihan Tetap + Pengeluaran Harian).",
                    "Tagihan Tetap adalah kewajiban bulanan seperti sewa, cicilan, dan listrik yang harus diprioritaskan.",
                    "Sisa Dana Bersih menunjukkan berapa 'uang dingin' yang benar-benar bebas Anda alokasikan untuk tabungan, investasi, atau hiburan."
                ),
                tip = "Jaga Sisa Dana Bersih selalu bernilai positif agar kondisi keuangan Anda sehat dan bebas stres."
            ),
            GuideItem(
                title = "2. Asisten AI 'Boleh Beli Gak Ya?'",
                subtitle = "Analisis rasional sebelum membeli barang impian",
                icon = Icons.Default.Psychology,
                color = Color(0xFF8B5CF6),
                steps = listOf(
                    "Tekan tombol 'Boleh Beli Gak Ya?' di bagian atas dashboard.",
                    "Masukkan nama barang (contoh: Gadget, Sepatu, Liburan) dan harganya.",
                    "AI akan menghitung dampak pembelian terhadap Sisa Dana Bersih dan target tabungan Anda.",
                    "Dapatkan rekomendasi instan: Beli Sekarang 🟢, Tunda Beberapa Bulan 🟧, atau Jangan Beli 🔴.",
                    "Lihat kalkulasi 'Opportunity Cost': berapa potensi berkembangnya uang tersebut jika diinvestasikan ke pasar modal/reksa dana."
                ),
                tip = "Gunakan fitur ini setiap kali tergoda melakukan impulsive buying!"
            ),
            GuideItem(
                title = "3. Target Investasi & Profil Risiko",
                subtitle = "Rencanakan masa depan keuangan dengan terukur",
                icon = Icons.Default.TrendingUp,
                color = Color(0xFF3B82F6),
                steps = listOf(
                    "Buka tab 'Target & Investasi' di menu navigasi bawah.",
                    "Isi 'Tes Profil Risiko' untuk menentukan toleransi risiko Anda (Konservatif, Moderat, atau Agresif).",
                    "Tambah Target Finansial baru seperti Dana Darurat (6x pengeluaran), DP Rumah, atau Dana Pensiun.",
                    "Gunakan tombol 'Top Up' untuk mencatat penambahan dana investasi secara berkala.",
                    "Sistem akan memproyeksikan estimasi nilai masa depan berdasarkan ekspektasi CAGR/return tahunan."
                ),
                tip = "Prioritaskan mengisi Dana Darurat terlebih dahulu sebelum masuk ke instrumen investasi berisiko."
            ),
            GuideItem(
                title = "4. Pencatatan Transaksi Harian",
                subtitle = "Catat pemasukan, tagihan, & pengeluaran dengan mudah",
                icon = Icons.Default.ReceiptLong,
                color = Color(0xFFF59E0B),
                steps = listOf(
                    "Tekan tombol '+ Catat' di header dashboard atau tab Cashflow.",
                    "Pilih jenis transaksi: Pemasukan 🟢, Tagihan Tetap 🔵, atau Pengeluaran Harian 🔴.",
                    "Pilih kategori (Gaji, Makanan, Transportasi, Hiburan, dll) dan masukkan jumlah nominalnya.",
                    "Semua grafik dan indikator skor kesehatan finansial akan terbarui secara otomatis!"
                ),
                tip = "Rutin mencatat setiap pengeluaran kecil agar kalkulasi skor finansial Anda selalu akurat."
            ),
            GuideItem(
                title = "5. Reset Data untuk Pengguna Baru",
                subtitle = "Mulai pencatatan dari nol atau pakai sample data",
                icon = Icons.Default.RestartAlt,
                color = Color(0xFFEF4444),
                steps = listOf(
                    "Tekan tombol 'Reset' (ikon putar balik) di header atas atau bagian bawah dashboard.",
                    "Ketikkan nama pengguna baru.",
                    "Pilih 'Mulai Bersih dari Nol' untuk menghapus semua data transaksi lama, atau 'Sample Data' untuk melihat contoh.",
                    "Seluruh database lokal akan direset dengan aman."
                ),
                tip = "Fitur ini sangat berguna saat Anda ingin menyerahkan aplikasi ke pengguna baru atau ingin memulai pembukuan dari awal bulan."
            )
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("user_guide_modal")
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Petunjuk Penggunaan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Panduan lengkap memahami & menggunakan aplikasi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(guideList) { index, item ->
                    var expanded by remember { mutableStateOf(index == 0) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = !expanded },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(item.color.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            item.icon,
                                            contentDescription = null,
                                            tint = item.color,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = item.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Icon(
                                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            AnimatedVisibility(visible = expanded) {
                                Column(modifier = Modifier.padding(top = 12.dp)) {
                                    Divider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    item.steps.forEachIndexed { sIdx, step ->
                                        Row(
                                            modifier = Modifier.padding(bottom = 8.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "• ",
                                                fontWeight = FontWeight.Bold,
                                                color = item.color
                                            )
                                            Text(
                                                text = step,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }

                                    Surface(
                                        color = item.color.copy(alpha = 0.08f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                Icons.Default.Lightbulb,
                                                contentDescription = null,
                                                tint = item.color,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Tips: ${item.tip}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("close_user_guide_button")
            ) {
                Text("Saya Mengerti, Mulai Pakai Aplikasi", fontWeight = FontWeight.Bold)
            }
        }
    }
}
