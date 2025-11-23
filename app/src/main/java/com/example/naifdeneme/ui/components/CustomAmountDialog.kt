package com.example.naifdeneme.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.naifdeneme.R

/**
 * Özel miktar girişi dialog
 * 100-2000 ml arası değer kabul eder
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAmountDialog(
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    // Dialog açıldığında keyboard'u göster
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Validation function
    fun validateAmount(): Boolean {
        val amount = amountText.toIntOrNull()
        return when {
            amount == null -> {
                isError = true
                errorMessage = "Geçerli bir sayı girin"
                false
            }
            amount < 100 -> {
                isError = true
                errorMessage = "Minimum 100 ml girebilirsiniz"
                false
            }
            amount > 2000 -> {
                isError = true
                errorMessage = "Maksimum 2000 ml girebilirsiniz"
                false
            }
            else -> {
                isError = false
                errorMessage = ""
                true
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.custom_amount_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { newValue ->
                        // Sadece sayı kabul et
                        if (newValue.all { it.isDigit() } && newValue.length <= 4) {
                            amountText = newValue
                            isError = false
                            errorMessage = ""
                        }
                    },
                    label = { Text(stringResource(R.string.custom_amount_hint)) },
                    suffix = { Text("ml") },
                    isError = isError,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            if (validateAmount()) {
                                keyboardController?.hide()
                                onConfirm(amountText.toInt())
                            }
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF06F9F9),
                        focusedLabelColor = Color(0xFF06F9F9)
                    )
                )

                if (isError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick amount buttons
                QuickAmountButtons(
                    onAmountSelected = { amount ->
                        amountText = amount.toString()
                        isError = false
                        errorMessage = ""
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Info text
                Text(
                    text = "100 - 2000 ml arası değer girebilirsiniz",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (validateAmount()) {
                        keyboardController?.hide()
                        onConfirm(amountText.toInt())
                    }
                },
                enabled = amountText.isNotEmpty()
            ) {
                Text(stringResource(R.string.dialog_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_cancel))
            }
        }
    )
}

/**
 * Hızlı miktar seçim butonları
 */
@Composable
private fun QuickAmountButtons(
    onAmountSelected: (Int) -> Unit
) {
    val quickAmounts = listOf(100, 200, 250, 330, 500, 750, 1000)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickAmounts.take(4).forEach { amount ->
                QuickAmountChip(
                    amount = amount,
                    onClick = { onAmountSelected(amount) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickAmounts.drop(4).forEach { amount ->
                QuickAmountChip(
                    amount = amount,
                    onClick = { onAmountSelected(amount) },
                    modifier = Modifier.weight(1f)
                )
            }
            // Boş alan için spacer
            if (quickAmounts.size > 4 && (quickAmounts.size - 4) < 4) {
                Spacer(modifier = Modifier.weight((4 - (quickAmounts.size - 4)).toFloat()))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAmountChip(
    amount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SuggestionChip(
        onClick = onClick,
        label = {
            Text(
                text = "${amount}ml",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        modifier = modifier,
        colors = SuggestionChipDefaults.suggestionChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )
}

// ============================================
// PREVIEW
// ============================================

@Preview
@Composable
fun CustomAmountDialogPreview() {
    com.example.naifdeneme.ui.theme.ModaiTheme {
        CustomAmountDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}