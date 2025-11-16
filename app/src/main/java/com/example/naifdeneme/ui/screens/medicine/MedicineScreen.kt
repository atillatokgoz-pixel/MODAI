package com.example.naifdeneme.ui.screens.medicine

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.naifdeneme.R
import com.example.naifdeneme.ui.theme.ModaiTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.isSystemInDarkTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineScreen(
    onAddMedicine: () -> Unit,
    onMedicineClick: (String) -> Unit,
    onViewAllRecords: () -> Unit
) {
    val medicines = remember { getSampleMedicines() }
    val selectedDateIndex = remember { mutableStateOf(2) } // Bugün

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.medicine_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { /* Analytics */ }) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = stringResource(R.string.analytics)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicine,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add))
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Date Chips
            DateChipsSection(selectedDateIndex = selectedDateIndex.value)

            Spacer(modifier = Modifier.height(16.dp))

            // Section Header
            Text(
                text = getTodayFormatted(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Medicine List
            if (medicines.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(medicines) { medicine ->
                        MedicineCard(
                            medicine = medicine,
                            onCheckedChange = { /* Update in database */ },
                            onClick = { onMedicineClick(medicine.id) }
                        )
                    }
                }
            } else {
                EmptyStateSection()
            }

            // View All Records
            Text(
                text = stringResource(R.string.medicine_view_all),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .clickable { onViewAllRecords() }
            )
        }
    }
}

@Composable
private fun DateChipsSection(selectedDateIndex: Int) {
    val dates = remember { getNext7Days() }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                dates.forEachIndexed { index, date ->
                    DateChip(
                        date = date,
                        isSelected = index == selectedDateIndex,
                        onClick = { /* Update selected date */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun DateChip(
    date: Pair<String, String>,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = CircleShape,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp)
    ) {
        Text(
            text = date.first,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimary
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun MedicineCard(
    medicine: MedicineUiModel,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color Bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(getSideBarColor(medicine.colorIndex))
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Medicine Info
            Column(
                modifier = Modifier.fillMaxWidth(0.8f) // WEIGHT YERİNE FILLMAXWIDTH
            ) {
                Text(
                    text = medicine.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = medicine.time,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Checkbox
            Checkbox(
                checked = medicine.isTaken,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
private fun EmptyStateSection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.NotificationsOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.medicine_empty_state),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.medicine_empty_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// Helper functions
@Composable
private fun getSideBarColor(index: Int): Color = when (index % 4) {
    0 -> if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color(0xFFA2E4B8)
    1 -> if (isSystemInDarkTheme()) MaterialTheme.colorScheme.primary else Color(0xFFA7D7F9)
    2 -> if (isSystemInDarkTheme()) Color(0xFFF906F9) else Color(0xFFF5B8B8)
    3 -> if (isSystemInDarkTheme()) Color(0xFF06F906) else Color(0xFFF0E2B6)
    else -> MaterialTheme.colorScheme.outline
}

private fun getNext7Days(): List<Pair<String, String>> {
    val calendar = Calendar.getInstance()
    val dateFormatDay = SimpleDateFormat("EEE", Locale.getDefault())
    val dateFormatNumber = SimpleDateFormat("d", Locale.getDefault())

    return (0..6).map { offset ->
        calendar.time = Date(System.currentTimeMillis() + offset * 24 * 60 * 60 * 1000)
        val day = dateFormatDay.format(calendar.time)
        val number = dateFormatNumber.format(calendar.time)
        Pair("$day, $number", calendar.timeInMillis.toString())
    }
}

private fun getTodayFormatted(): String {
    val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
    return "Bugün, ${dateFormat.format(Date())}"
}

private fun getSampleMedicines(): List<MedicineUiModel> = listOf(
    MedicineUiModel("1", "Vitamin D", "09:00", false, 0),
    MedicineUiModel("2", "Probiyotik", "09:00", true, 1),
    MedicineUiModel("3", "Magnezyum", "21:00", true, 2),
    MedicineUiModel("4", "Demir Takviyesi", "21:30", false, 3)
)

data class MedicineUiModel(
    val id: String,
    val name: String,
    val time: String,
    val isTaken: Boolean,
    val colorIndex: Int
)

@Preview(showBackground = true)
@Composable
fun MedicineScreenPreview() {
    ModaiTheme {
        MedicineScreen(
            onAddMedicine = {},
            onMedicineClick = {},
            onViewAllRecords = {}
        )
    }
}

@Preview(showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MedicineScreenDarkPreview() {
    ModaiTheme {
        MedicineScreen(
            onAddMedicine = {},
            onMedicineClick = {},
            onViewAllRecords = {}
        )
    }
}