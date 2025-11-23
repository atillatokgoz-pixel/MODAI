package com.example.naifdeneme.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.naifdeneme.R

/**
 * Not ekleme dialog
 * Su kaydına opsiyonel not eklemek için
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteInputDialog(
    initialNote: String = "",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var noteText by remember { mutableStateOf(initialNote) }
    val maxLength = 100
    val focusRequester = remember { FocusRequester() }

    // Dialog açıldığında focus
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.note_dialog_title),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { newValue ->
                        if (newValue.length <= maxLength) {
                            noteText = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.note_hint)) },
                    placeholder = {
                        Text(
                            "Örn: Spor sonrası, yemekle birlikte...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    singleLine = false,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .focusRequester(focusRequester),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF06F9F9),
                        focusedLabelColor = Color(0xFF06F9F9)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Character counter
                Text(
                    text = "${noteText.length} / $maxLength",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (noteText.length > maxLength * 0.8)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Quick note suggestions
                QuickNoteSuggestions(
                    onSuggestionSelected = { suggestion ->
                        noteText = suggestion
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(noteText.trim())
                }
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
 * Hızlı not önerileri
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickNoteSuggestions(
    onSuggestionSelected: (String) -> Unit
) {
    val suggestions = listOf(
        "🏃 Spor sonrası",
        "🍽️ Yemekle birlikte",
        "🌅 Sabah",
        "🌙 Akşam",
        "💊 İlaçla birlikte",
        "🏢 İş yerinde"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Hızlı Notlar:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.take(3).forEach { suggestion ->
                SuggestionChip(
                    onClick = { onSuggestionSelected(suggestion) },
                    label = {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.drop(3).forEach { suggestion ->
                SuggestionChip(
                    onClick = { onSuggestionSelected(suggestion) },
                    label = {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}

// ============================================
// PREVIEW
// ============================================

@Preview
@Composable
fun NoteInputDialogPreview() {
    com.example.naifdeneme.ui.theme.ModaiTheme {
        NoteInputDialog(
            onDismiss = {},
            onConfirm = {}
        )
    }
}

@Preview
@Composable
fun NoteInputDialogWithTextPreview() {
    com.example.naifdeneme.ui.theme.ModaiTheme {
        NoteInputDialog(
            initialNote = "Spor sonrası içildi",
            onDismiss = {},
            onConfirm = {}
        )
    }
}