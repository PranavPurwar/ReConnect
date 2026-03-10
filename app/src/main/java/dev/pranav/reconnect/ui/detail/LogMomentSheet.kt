package dev.pranav.reconnect.ui.detail

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.pranav.reconnect.data.model.MomentCategory
import dev.pranav.reconnect.ui.theme.GoldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMomentSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, description: String, category: MomentCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MomentCategory.GENERAL) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Log a Moment", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                singleLine = true
            )
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                maxLines = 5
            )

            Text("Category", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MomentCategory.entries.forEach { option ->
                    FilterChip(
                        selected = category == option,
                        onClick = { category = option },
                        label = {
                            Text(
                                option.name.lowercase().replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldPrimary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title, description, category)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Log It", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

