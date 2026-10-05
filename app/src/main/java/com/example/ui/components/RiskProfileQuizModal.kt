package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.FinancialEngine

data class QuizQuestion(
    val title: String,
    val options: List<QuizOption>
)

data class QuizOption(
    val text: String,
    val score: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiskProfileQuizModal(
    onDismiss: () -> Unit,
    onSaveProfile: (riskProfile: String) -> Unit
) {
    val questions = remember {
        listOf(
            QuizQuestion(
                title = "1. Apa tujuan utama investasi Anda saat ini?",
                options = listOf(
                    QuizOption("Menjaga keutuhan modal tanpa penurunan (Keamanan Utama)", 1),
                    QuizOption("Keseimbangan antara imbal hasil dan risiko terukur", 2),
                    QuizOption("Pertumbuhan aset maksimal jangka panjang meskipun berfluktuasi", 3)
                )
            ),
            QuizQuestion(
                title = "2. Berapa lama jangka waktu (time horizon) investasi Anda?",
                options = listOf(
                    QuizOption("Jangka Pendek (< 1 - 2 tahun)", 1),
                    QuizOption("Jangka Menengah (2 - 5 tahun)", 2),
                    QuizOption("Jangka Panjang (> 5 - 10+ tahun)", 3)
                )
            ),
            QuizQuestion(
                title = "3. Bagaimana reaksi Anda jika nilai investasi Anda turun 15% dalam 1 bulan?",
                options = listOf(
                    QuizOption("Panik & segera mencairkan seluruh investasi ke kas aman", 1),
                    QuizOption("Menunggu kondisi membaik & mengevaluasi strategi", 2),
                    QuizOption("Menganggapnya diskon pasar & menambah porsi investasi (Top Up)", 3)
                )
            ),
            QuizQuestion(
                title = "4. Berapa persen dari total penghasilan yang siap Anda simpan secara rutin?",
                options = listOf(
                    QuizOption("Kurang dari 10%", 1),
                    QuizOption("Antara 10% - 25%", 2),
                    QuizOption("Lebih dari 25%", 3)
                )
            ),
            QuizQuestion(
                title = "5. Apa pengalaman investasi Anda selama ini?",
                options = listOf(
                    QuizOption("Pemula / Hanya paham Deposito & Tabungan Bank", 1),
                    QuizOption("Menengah / Pernah membeli Reksa Dana & Obligasi SBN", 2),
                    QuizOption("Paham / Aktif berinvestasi di Saham, Crypto, atau Properti", 3)
                )
            )
        )
    }

    var currentStep by remember { mutableIntStateOf(0) }
    val selectedScores = remember { mutableStateListOf(0, 0, 0, 0, 0) }
    var calculatedProfile by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("risk_profile_quiz_modal")
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
                Text(
                    text = "Kuesioner Profil Risiko Investasi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (calculatedProfile == null) {
                // Showing Questions
                val q = questions[currentStep]
                LinearProgressIndicator(
                    progress = { (currentStep + 1).toFloat() / questions.size },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = q.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                q.options.forEach { option ->
                    val isSelected = selectedScores[currentStep] == option.score
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                selectedScores[currentStep] = option.score
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedScores[currentStep] = option.score }
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = option.text,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    enabled = selectedScores[currentStep] > 0,
                    onClick = {
                        if (currentStep < questions.size - 1) {
                            currentStep++
                        } else {
                            // Finish quiz
                            val profile = FinancialEngine.calculateRiskScoreFromAnswers(selectedScores)
                            calculatedProfile = profile
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (currentStep < questions.size - 1) "Lanjut" else "Lihat Hasil Profil Risiko")
                }
            } else {
                // Result view
                val profileName = when (calculatedProfile) {
                    "CONSERVATIVE" -> "Konservatif (Pencari Keamanan)"
                    "AGGRESSIVE" -> "Agresif (Pencari Pertumbuhan)"
                    else -> "Moderat (Pencari Keseimbangan)"
                }
                val alloc = FinancialEngine.getAssetAllocation(calculatedProfile!!)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Profil Risiko Anda:",
                        style = MaterialTheme.typography.labelLarge
                    )

                    Text(
                        text = profileName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = alloc.summaryText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Rekomendasi Alokasi Aset:", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Pasar Uang / Deposito: ${alloc.moneyMarketPercent}%")
                            Text("• Obligasi Negara / SBN: ${alloc.bondsPercent}%")
                            Text("• Reksa Dana Pendapatan Tetap: ${alloc.mutualFundsPercent}%")
                            Text("• Saham / ETF Pasar Modal: ${alloc.stocksPercent}%")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            onSaveProfile(calculatedProfile!!)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Terapkan Profil Risiko")
                    }
                }
            }
        }
    }
}
