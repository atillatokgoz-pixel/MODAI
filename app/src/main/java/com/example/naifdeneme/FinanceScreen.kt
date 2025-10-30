package com.example.naifdeneme.ui.screens.finance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naifdeneme.database.AppDatabase
import com.example.naifdeneme.database.TransactionEntity
import com.example.naifdeneme.database.TransactionType
import com.example.naifdeneme.ui.theme.ModaiTheme
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * FinanceScreen - Gelir/Gider takip ekranı
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val transactionDao = remember { database.transactionDao() }
    val scope = rememberCoroutineScope()

    // Flow'dan işlemleri al
    val transactions by transactionDao.getAllTransactions().collectAsState(initial = emptyList())

    // Toplam hesaplamalar (transactions değiştiğinde otomatik güncelle)
    val totalIncome = remember(transactions) {
        transactions.filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }

    val totalExpense = remember(transactions) {
        transactions.filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
    }

    val balance = totalIncome - totalExpense

    // Dialog gösterimi
    var showAddDialog by remember { mutableStateOf(false) }
    var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Finans") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yeni İşlem")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Özet Kartları
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Bakiye
                SummaryCard(
                    title = "Bakiye",
                    amount = balance,
                    color = if (balance >= 0) Color(0xFF10B981) else Color(0xFFEF4444),
                    icon = "💰"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Gelir
                    SummaryCard(
                        title = "Gelir",
                        amount = totalIncome,
                        color = Color(0xFF10B981),
                        icon = "📈",
                        modifier = Modifier.weight(1f)
                    )

                    // Gider
                    SummaryCard(
                        title = "Gider",
                        amount = totalExpense,
                        color = Color(0xFFEF4444),
                        icon = "📉",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // İşlemler Listesi
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Henüz işlem yok",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(transactions, key = { it.id }) { transaction ->
                        TransactionCard(
                            transaction = transaction,
                            onEdit = {
                                editingTransaction = transaction
                                showAddDialog = true
                            },
                            onDelete = {
                                scope.launch {
                                    transactionDao.deleteTransaction(transaction)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // İşlem Ekleme/Düzenleme Dialog
    if (showAddDialog) {
        TransactionDialog(
            existingTransaction = editingTransaction,
            onDismiss = {
                showAddDialog = false
                editingTransaction = null
            },
            onSave = { type, amount, category, description ->
                scope.launch {
                    if (editingTransaction != null) {
                        transactionDao.updateTransaction(
                            editingTransaction!!.copy(
                                type = type,
                                amount = amount,
                                category = category,
                                description = description
                            )
                        )
                    } else {
                        transactionDao.insertTransaction(
                            TransactionEntity(
                                type = type,
                                amount = amount,
                                category = category,
                                description = description
                            )
                        )
                    }
                    showAddDialog = false
                    editingTransaction = null
                }
            }
        )
    }
}

/**
 * Özet Kartı
 */
@Composable
fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(icon, fontSize = 20.sp)
                Text(
                    title,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                formatCurrency(amount),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * İşlem Kartı
 */
@Composable
fun TransactionCard(
    transaction: TransactionEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kategori emoji
            val categoryEmoji = when (transaction.category) {
                "Yemek" -> "🍽️"
                "Ulaşım" -> "🚗"
                "Alışveriş" -> "🛍️"
                "Fatura" -> "🧾"
                "Eğlence" -> "🎮"
                "Sağlık" -> "🏥"
                "Diğer" -> "📦"
                "Maaş" -> "💰"
                "Yatırım" -> "📈"
                "Hediye" -> "🎁"
                else -> "❓"
            }

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (transaction.type == TransactionType.INCOME)
                            Color(0xFF10B981).copy(alpha = 0.1f)
                        else
                            Color(0xFFEF4444).copy(alpha = 0.1f),
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    categoryEmoji,
                    fontSize = 24.sp
                )
            }

            Spacer(Modifier.width(12.dp))

            // Bilgiler
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    transaction.category,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (transaction.description.isNotBlank()) {
                    Text(
                        transaction.description,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    formatDate(transaction.date),
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Tutar
            Text(
                "${if (transaction.type == TransactionType.INCOME) "+" else "-"}${formatCurrency(transaction.amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (transaction.type == TransactionType.INCOME)
                    Color(0xFF10B981)
                else
                    Color(0xFFEF4444)
            )

            // Menü
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menü", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Düzenle") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sil", color = Color(0xFFEF4444)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

/**
 * İşlem Ekleme/Düzenleme Dialog
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDialog(
    existingTransaction: TransactionEntity?,
    onDismiss: () -> Unit,
    onSave: (TransactionType, Double, String, String) -> Unit
) {
    var selectedType by remember {
        mutableStateOf(existingTransaction?.type ?: TransactionType.EXPENSE)
    }
    var amount by remember { mutableStateOf(existingTransaction?.amount?.toString() ?: "") }
    var selectedCategory by remember {
        mutableStateOf(existingTransaction?.category ?: "Yemek")
    }
    var description by remember { mutableStateOf(existingTransaction?.description ?: "") }

    val categories = if (selectedType == TransactionType.INCOME) {
        listOf("Maaş", "Yatırım", "Hediye", "Diğer")
    } else {
        listOf("Yemek", "Ulaşım", "Alışveriş", "Fatura", "Eğlence", "Sağlık", "Diğer")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (existingTransaction != null)
                    "İşlemi Düzenle"
                else
                    "Yeni İşlem"
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tip seçimi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == TransactionType.INCOME,
                        onClick = { selectedType = TransactionType.INCOME },
                        label = { Text("Gelir") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == TransactionType.EXPENSE,
                        onClick = { selectedType = TransactionType.EXPENSE },
                        label = { Text("Gider") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Tutar
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Tutar") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Kategori
                Text(
                    "Kategori",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                LazyColumn(
                    modifier = Modifier.height(200.dp)
                ) {
                    items(categories) { category ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedCategory = category }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                when (category) {
                                    "Yemek" -> "🍽️"
                                    "Ulaşım" -> "🚗"
                                    "Alışveriş" -> "🛍️"
                                    "Fatura" -> "🧾"
                                    "Eğlence" -> "🎮"
                                    "Sağlık" -> "🏥"
                                    "Diğer" -> "📦"
                                    "Maaş" -> "💰"
                                    "Yatırım" -> "📈"
                                    "Hediye" -> "🎁"
                                    else -> "❓"
                                },
                                fontSize = 20.sp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(category)
                        }
                    }
                }

                // Açıklama
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama (opsiyonel)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0 && selectedCategory.isNotBlank()) {
                        onSave(selectedType, amountValue, selectedCategory, description)
                    }
                },
                enabled = amount.toDoubleOrNull() != null &&
                        amount.toDoubleOrNull()!! > 0 &&
                        selectedCategory.isNotBlank()
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

/**
 * Para birimi formatı
 */
private fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("tr", "TR"))
    return format.format(amount)
}

/**
 * Tarih formatı
 */
private fun formatDate(date: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr", "TR"))
    return sdf.format(Date(date))
}

@Preview(showBackground = true)
@Composable
fun FinanceScreenPreview() {
    ModaiTheme {
        FinanceScreen(onBack = {})
    }
}