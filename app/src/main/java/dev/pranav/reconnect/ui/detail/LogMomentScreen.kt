package dev.pranav.reconnect.ui.detail

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.github.panpf.sketch.AsyncImage
import dev.pranav.reconnect.core.model.MomentCategory
import dev.pranav.reconnect.core.model.MomentImage
import dev.pranav.reconnect.data.port.AppContainer
import dev.pranav.reconnect.ui.theme.GoldPrimary
import dev.pranav.reconnect.ui.theme.MediumGray
import dev.pranav.reconnect.util.takePersistableReadPermissionIfPossible
import kotlinx.coroutines.launch
import java.util.UUID

private const val MAX_IMAGES = 100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMomentScreen(
    initialContactId: String? = null,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        description: String,
        category: MomentCategory,
        images: List<MomentImage>,
        isCoreMemory: Boolean,
        wasPresent: Boolean,
        groupName: String?,
        locationMood: String?,
        momentId: String,
        contactIds: List<String>
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MomentCategory.GENERAL) }
    var selectedImages by remember { mutableStateOf(emptyList<MomentImage>()) }
    var isCoreMemory by remember { mutableStateOf(false) }
    var wasPresent by remember { mutableStateOf(true) }
    var groupName by remember { mutableStateOf("") }
    var locationMood by remember { mutableStateOf("") }
    var selectedContactIds by remember(initialContactId) {
        mutableStateOf(initialContactId?.let { setOf(it) } ?: emptySet())
    }

    var isUploading by remember { mutableStateOf(false) }
    var uploadErrors by remember { mutableStateOf<List<MomentImage>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val attachmentStore = remember { AppContainer.attachmentStore }
    val contactStore = remember { AppContainer.contactStore }
    val photoResolver = remember { AppContainer.photoResolver }

    val allContacts by contactStore.contacts.collectAsState(initial = emptyList())
    var showContactSheet by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(MAX_IMAGES)
    ) { uris ->
        uris.forEach { context.takePersistableReadPermissionIfPossible(it) }
        val newImages = uris.map {
            MomentImage(
                id = UUID.randomUUID().toString(),
                uri = it.toString(),
                caption = ""
            )
        }
        selectedImages = (selectedImages + newImages).take(MAX_IMAGES)
    }

    BackHandler(onBack = { if (!isUploading) onDismiss() })

    Scaffold(
        modifier = Modifier.fillMaxSize().zIndex(10f),
        topBar = {
            TopAppBar(
                title = { Text("Log a Moment") },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Mark as Core Memory", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = isCoreMemory,
                    onCheckedChange = { isCoreMemory = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = GoldPrimary)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("I was present", style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = wasPresent,
                    onCheckedChange = { wasPresent = it },
                    colors = SwitchDefaults.colors(checkedTrackColor = GoldPrimary)
                )
            }

            Text("People involved", style = MaterialTheme.typography.labelLarge)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(selectedContactIds.toList(), key = { it }) { id ->
                    val contact = allContacts.find { it.id == id }
                    if (contact != null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.width(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.TopEnd) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = contact.name.firstOrNull()?.toString()?.uppercase()
                                            ?: "?",
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    AsyncImage(
                                        uri = photoResolver.resolveContactPhoto(contact.id),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .offset(x = 4.dp, y = (-4).dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.error)
                                        .clickable { selectedContactIds -= contact.id },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.onError,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = contact.name.split(" ").first(),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clickable { showContactSheet = true }
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add Person",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Add", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Project / Group Name (optional)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = GoldPrimary),
                singleLine = true
            )

            OutlinedTextField(
                value = locationMood,
                onValueChange = { locationMood = it },
                label = { Text("Location Mood (optional)") },
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Photos", style = MaterialTheme.typography.labelLarge)
                if (selectedImages.isNotEmpty()) {
                    Text(
                        "${selectedImages.size}/$MAX_IMAGES",
                        style = MaterialTheme.typography.labelMedium,
                        color = MediumGray
                    )
                }
            }

            if (selectedImages.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(140.dp)
                ) {
                    items(selectedImages, key = { it.id }) { image ->
                        Column(modifier = Modifier.width(120.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            ) {
                                val finalUri = if (image.uri.startsWith("content://")) image.uri else photoResolver.resolveMomentPhoto(image.uri)
                                AsyncImage(
                                    uri = finalUri,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                IconButton(
                                    onClick = {
                                        selectedImages = selectedImages.filter { it.id != image.id }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.5f))
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(
                                value = image.caption ?: "",
                                onValueChange = { newCaption ->
                                    if (newCaption.length <= 50) {
                                        selectedImages = selectedImages.map {
                                            if (it.id == image.id) it.copy(caption = newCaption) else it
                                        }
                                    }
                                },
                                placeholder = {
                                    Text(
                                        "Caption...",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                },
                                modifier = Modifier.fillMaxWidth().height(40.dp),
                                textStyle = MaterialTheme.typography.bodySmall,
                                maxLines = 1,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldPrimary,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }

            OutlinedButton(
                onClick = { imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)) },
                enabled = selectedImages.size < MAX_IMAGES,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = GoldPrimary)
            ) {
                Icon(
                    Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (selectedImages.isEmpty()) "Add Photos" else "Add More Photos")
            }

            Spacer(Modifier.height(4.dp))

            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        scope.launch {
                            isUploading = true
                            uploadErrors = emptyList()

                            val momentId =
                                UUID.randomUUID().toString() // Generate upfront for AttachmentStore
                            val successfulImages = mutableListOf<MomentImage>()
                            val failedImages = mutableListOf<MomentImage>()

                            for (img in selectedImages) {
                                try {
                                    val up = attachmentStore.persistMomentAttachments(
                                        contactId = "N/A", // Actually unused by SupabaseAttachmentStore
                                        momentId = momentId,
                                        sourceUris = listOf(img)
                                    )
                                    if (up.isNotEmpty()) {
                                        successfulImages.add(up.first())
                                    } else {
                                        failedImages.add(img)
                                    }
                                } catch (e: Exception) {
                                    failedImages.add(img)
                                }
                            }

                            if (failedImages.isNotEmpty()) {
                                isUploading = false
                                uploadErrors = failedImages
                            } else {
                                onSave(
                                    title,
                                    description,
                                    category,
                                    successfulImages,
                                    isCoreMemory,
                                    wasPresent,
                                    groupName.takeIf { it.isNotBlank() },
                                    locationMood.takeIf { it.isNotBlank() },
                                    momentId,
                                    selectedContactIds.toList()
                                )
                            }
                        }
                    }
                },
                enabled = title.isNotBlank() && !isUploading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = Color.White
                )
            ) {
                if (isUploading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Log It", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        if (showContactSheet) {
            ModalBottomSheet(
                onDismissRequest = { showContactSheet = false },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)) {
                    Text(
                        text = "Select Contacts",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(allContacts, key = { it.id }) { contact ->
                            val isSelected = selectedContactIds.contains(contact.id)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedContactIds = if (isSelected) {
                                            selectedContactIds - contact.id
                                        } else {
                                            selectedContactIds + contact.id
                                        }
                                    }
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        contact.name.firstOrNull()?.toString()?.uppercase() ?: "?",
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    AsyncImage(
                                        uri = photoResolver.resolveContactPhoto(contact.id),
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Spacer(Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(contact.name, style = MaterialTheme.typography.titleMedium)
                                    if (contact.phoneNumber.isNotBlank()) {
                                        Text(
                                            contact.phoneNumber,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(checkedColor = GoldPrimary)
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uploadErrors.isNotEmpty()) {
            AlertDialog(
                onDismissRequest = { uploadErrors = emptyList() },
                title = { Text("Uploads Failed") },
                text = { Text("${uploadErrors.size} photos failed to upload. Log anyway without them?") },
                confirmButton = {
                    TextButton(onClick = {
                        // Re-generate since we didn't store the current one in outer scope, or just create new
                        val successfulImages = selectedImages.filter { it !in uploadErrors }
                        onSave(
                            title,
                            description,
                            category,
                            successfulImages,
                            isCoreMemory,
                            wasPresent,
                            groupName.takeIf { it.isNotBlank() },
                            locationMood.takeIf { it.isNotBlank() },
                            UUID.randomUUID().toString(), // New ID for skipping
                            selectedContactIds.toList()
                        )
                    }) {
                        Text("Log Anyway")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { uploadErrors = emptyList() }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
