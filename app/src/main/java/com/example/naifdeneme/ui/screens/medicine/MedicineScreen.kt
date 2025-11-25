package com.example.naifdeneme.ui.screens.medicine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.naifdeneme.database.AppDatabase
import com.example.naifdeneme.database.MedicineEntity
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }

    // Flow ile verileri dinle
    val medicines by database.medicineDao().getAllMedicines().collectAsState(initial = emptyList())

    // UI State
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("💊 İlaç Takibi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Geri")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFFEBEE) // Light Pink bg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFE91E63),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "İlaç Ekle")
            }
        }
    ) { padding ->

        if (medicines.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("💊", fontSize = 64.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Henüz ilaç eklemediniz",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Text(
                        "Düzenli ilaçlarınızı buradan takip edin",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // İstatistik Kartı (Basit)
                item {
                    val takenCount = medicines.count { it.isTakenToday }
                    val totalCount = medicines.size

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                progress = if (totalCount > 0) takenCount.toFloat() / totalCount else 0f,
                                color = Color(0xFFE91E63),
                                trackColor = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Bugünkü Durum",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$takenCount / $totalCount alındı",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFC2185B)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // İlaç Listesi
                items(medicines) { medicine ->
                    MedicineCard(
                        medicine = medicine,
                        onToggle = {
                            scope.launch {
                                val updated = medicine.copy(isTakenToday = !medicine.isTakenToday)
                                database.medicineDao().updateMedicine(updated)
                            }
                        },
                        onDelete = {
                            scope.launch {
                                database.medicineDao().deleteMedicine(medicine)
                            }
                        }
                    )
                }

                // FAB için boşluk
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddMedicineDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, dosage, time ->
                scope.launch {
                    val newMedicine = MedicineEntity(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        dosage = dosage,
                        time = time,
                        isTakenToday = false,
                        lastTakenDate = null
                    )
                    database.medicineDao().insertMedicine(newMedicine)
                }
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MedicineCard(
    medicine: MedicineEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val backgroundColor = if (medicine.isTakenToday) Color(0xFFE8F5E9) else Color.White
    val textColor = if (medicine.isTakenToday) Color.Gray else Color.Black
    val textDecoration = if (medicine.isTakenToday) TextDecoration.LineThrough else TextDecoration.None

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox / Icon
            IconButton(onClick = onToggle) {
                if (medicine.isTakenToday) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Icon(
                        Icons.Outlined.Circle,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    textDecoration = textDecoration
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "⏰ ${medicine.time}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "💊 ${medicine.dosage}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFFF5252))
            }
        }
    }
}

@Composable
fun AddMedicineDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("1 adet") }
    var time by remember { mutableStateOf("09:00") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni İlaç Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("İlaç Adı") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Doz (örn: 1 hap)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = time,
                    onValueChange = { time = it },
                    label = { Text("Saat (örn: 09:00)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onAdd(name, dosage, time)
                    }
                }
            ) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}