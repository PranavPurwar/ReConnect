package dev.pranav.reconnect.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.pranav.reconnect.data.model.ReconnectInterval
import dev.pranav.reconnect.ui.theme.GoldPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactSheet(
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, title: String, relationship: String, interval: ReconnectInterval) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }
    var interval by remember { mutableStateOf(ReconnectInterval.MONTHLY) }

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
            Text("Add Contact", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                singleLine = true
            )
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone number") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                singleLine = true
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title / Occupation") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                singleLine = true
            )
            OutlinedTextField(
                value = relationship,
                onValueChange = { relationship = it },
                label = { Text("Relationship (e.g. Close Friend)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                singleLine = true
            )

            Text("Reconnect every", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ReconnectInterval.entries.forEach { option ->
                    FilterChip(
                        selected = interval == option,
                        onClick = { interval = option },
                        label = { Text(option.label, style = MaterialTheme.typography.labelSmall) },
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
                    if (name.isNotBlank()) {
                        onSave(name, phone, title, relationship, interval)
                    }
                },
                enabled = name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.White
                )
            ) {
                Text("Save Contact", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

