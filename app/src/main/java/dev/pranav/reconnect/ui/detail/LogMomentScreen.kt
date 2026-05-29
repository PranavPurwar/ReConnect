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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.github.panpf.sketch.AsyncImage
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.pranav.reconnect.core.model.MomentCategory
import dev.pranav.reconnect.core.model.MomentImage
import dev.pranav.reconnect.core.model.PastMoment
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.di.AppContainer
import dev.pranav.reconnect.ui.components.AppTopBar
import dev.pranav.reconnect.ui.theme.GoldPrimary
import dev.pranav.reconnect.ui.theme.PlayfairFamily
import dev.pranav.reconnect.ui.theme.PlusJakartaSansFamily
import dev.pranav.reconnect.util.takePersistableReadPermissionIfPossible
import kotlinx.coroutines.launch
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.compose.util.ClickResult
import java.util.UUID

private const val MAX_IMAGES = 100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogMomentScreen(
    initialContactId: String? = null,
    initialMoment: PastMoment? = null,
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
        locationLatitude: Double?,
        locationLongitude: Double?,
        momentId: String,
        contactIds: List<String>,
        dateEpochMs: Long
    ) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(initialMoment?.title ?: "") }
    var description by remember { mutableStateOf(initialMoment?.description ?: "") }
    var category by remember { mutableStateOf(initialMoment?.category ?: MomentCategory.GENERAL) }
    var selectedImages by remember {
        mutableStateOf(
            initialMoment?.images ?: emptyList<MomentImage>()
        )
    }
    var isCoreMemory by remember { mutableStateOf(initialMoment?.isCoreMemory ?: false) }
    var wasPresent by remember { mutableStateOf(initialMoment?.wasPresent ?: true) }
    var groupName by remember { mutableStateOf(initialMoment?.groupName ?: "") }
    var locationMood by remember { mutableStateOf(initialMoment?.locationMood ?: "") }
    var locationLatitude by remember { mutableStateOf(initialMoment?.locationLatitude) }
    var locationLongitude by remember { mutableStateOf(initialMoment?.locationLongitude) }
    var selectedContactIds by remember(initialContactId, initialMoment) {
        mutableStateOf(
            if (initialMoment != null) {
                initialMoment.contactIds.toSet()
            } else {
                initialContactId?.let { setOf(it) } ?: emptySet()
            }
        )
    }

    var isUploading by remember { mutableStateOf(false) }
    var uploadErrors by remember { mutableStateOf<List<MomentImage>>(emptyList()) }
    var successfulUploads by remember { mutableStateOf<List<MomentImage>>(emptyList()) }
    var currentMomentId by remember { mutableStateOf("") }
    var selectedDateMs by remember {
        mutableStateOf(
            initialMoment?.dateEpochMs ?: System.currentTimeMillis()
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var showLocationPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val attachmentStore = remember { AppContainer.attachmentStore }
    val contactStore = remember { AppContainer.contactStore }
    val photoResolver = remember { AppContainer.photoResolver }

    val allContacts by contactStore.contacts.collectAsState(initial = emptyList())
    var showContactSheet by remember { mutableStateOf(false) }
    var imageForCaption by remember { mutableStateOf<MomentImage?>(null) }

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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMs)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMs = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val hazeState = remember { HazeState() }
    val expressiveScheme = MaterialTheme.colorScheme

    val baseBackgroundBrush = remember(expressiveScheme) {
        Brush.linearGradient(
            colors = listOf(
                expressiveScheme.primaryContainer.copy(alpha = 0.50f),
                expressiveScheme.secondaryContainer.copy(alpha = 0.24f),
                expressiveScheme.tertiaryContainer.copy(alpha = 0.34f)
            ),
            start = Offset(0f, 0f),
            end = Offset(1500f, 2400f)
        )
    }

    val topBloomBrush = remember(expressiveScheme) {
        Brush.radialGradient(
            colors = listOf(expressiveScheme.primary.copy(alpha = 0.42f), Color.Transparent),
            center = Offset(1100f, 120f),
            radius = 760f
        )
    }

    val bottomBloomBrush = remember(expressiveScheme) {
        Brush.radialGradient(
            colors = listOf(expressiveScheme.tertiary.copy(alpha = 0.32f), Color.Transparent),
            center = Offset(140f, 1950f),
            radius = 920f
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(10f)
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            AppTopBar(
                title = "Log a Moment",
                scrollBehavior = scrollBehavior,
                hazeState = hazeState,
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            )
        },
        containerColor = Color.Transparent,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .hazeSource(hazeState)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(baseBackgroundBrush)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(topBloomBrush)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bottomBloomBrush)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding() + 32.dp
                    )
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                Spacer(Modifier.height(18.dp))

                FormSection(label = "Title *") {
                    GlassInputField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = "What happened?"
                    )
                }

                FormSection(label = "Date") {
                    Surface(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.44f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val formattedDate = remember(selectedDateMs) {
                                java.text.SimpleDateFormat("MMM d, yyyy", java.util.Locale.US)
                                    .format(java.util.Date(selectedDateMs))
                            }
                            Text(
                                formattedDate,
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "Select Date",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Settings",
                        fontFamily = PlayfairFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.44f)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Mark as Core Memory",
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 16.sp
                            )
                            Switch(
                                checked = isCoreMemory,
                                onCheckedChange = { isCoreMemory = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(
                                alpha = 0.5f
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "I was present",
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 16.sp
                            )
                            Switch(
                                checked = wasPresent,
                                onCheckedChange = { wasPresent = it },
                                colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                            )
                        }
                    }
                }

                FormSection(label = "People involved") {
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
                                                text = contact.name.firstOrNull()?.toString()
                                                    ?.uppercase()
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
                                        .border(
                                            1.dp,
                                            MaterialTheme.colorScheme.outline,
                                            CircleShape
                                        ),
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
                }

                FormSection(label = "Project / Group Name") {
                    GlassInputField(
                        value = groupName,
                        onValueChange = { groupName = it },
                        placeholder = "Optional"
                    )
                }

                FormSection(label = "Location") {
                    Surface(
                        onClick = { showLocationPicker = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.44f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (locationLatitude != null && locationLongitude != null) {
                                    "${"%.5f".format(locationLatitude)} , ${
                                        "%.5f".format(
                                            locationLongitude
                                        )
                                    }"
                                } else {
                                    "Select on map"
                                },
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 15.sp,
                                color = if (locationLatitude != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = "Select location",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                FormSection(label = "Location Mood") {
                    GlassInputField(
                        value = locationMood,
                        onValueChange = { locationMood = it },
                        placeholder = "Optional"
                    )
                }

                FormSection(label = "Notes") {
                    GlassInputField(
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Optional",
                        singleLine = false,
                        minLines = 3,
                        maxLines = 10
                    )
                }

                FormSection(label = "Category") {
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
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }

                FormSection(label = "Photos") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (selectedImages.isNotEmpty()) {
                                Text(
                                    "${selectedImages.size}/$MAX_IMAGES",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (selectedImages.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(selectedImages, key = { it.id }) { image ->
                                    Box(
                                        modifier = Modifier
                                            .width(130.dp)
                                            .aspectRatio(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { imageForCaption = image }
                                    ) {
                                        val finalUri =
                                            if (image.uri.startsWith("content://")) image.uri else photoResolver.resolveMomentPhoto(
                                                image.uri
                                            )
                                        AsyncImage(
                                            uri = finalUri,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize()
                                        )

                                        if (!image.caption.isNullOrBlank()) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomStart)
                                                    .fillMaxWidth()
                                                    .background(Color.Black.copy(alpha = 0.4f))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                            ) {
                                                Text(
                                                    text = image.caption!!,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                selectedImages =
                                                    selectedImages.filter { it.id != image.id }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(24.dp)
                                        ) {
                                            // Design modification: Using light shadow and no background instead of a grey background square
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                tint = Color.White,
                                                modifier = Modifier.size(20.dp) // Removed clipping and black background
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                imagePicker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                    )
                                )
                            },
                            enabled = selectedImages.size < MAX_IMAGES,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (selectedImages.isEmpty()) "Add Photos" else "Add More Photos",
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            scope.launch {
                                isUploading = true
                                uploadErrors = emptyList()
                                successfulUploads = emptyList()

                                val momentId = initialMoment?.id ?: UUID.randomUUID().toString()
                                currentMomentId = momentId
                                val successfulImages = mutableListOf<MomentImage>()
                                val failedImages = mutableListOf<MomentImage>()

                                for (img in selectedImages) {
                                    if (!img.uri.startsWith("content://")) {
                                        successfulImages.add(img)
                                        continue
                                    }
                                    try {
                                        val up = attachmentStore.persistMomentAttachments(
                                            contactId = "N/A",
                                            momentId = momentId,
                                            sourceUris = listOf(img)
                                        )
                                        if (up.isNotEmpty()) {
                                            successfulImages.add(up.first())
                                        } else {
                                            failedImages.add(img)
                                        }
                                    } catch (_: Exception) {
                                        failedImages.add(img)
                                    }
                                }

                                if (failedImages.isNotEmpty()) {
                                    isUploading = false
                                    uploadErrors = failedImages
                                    successfulUploads = successfulImages
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
                                        locationLatitude,
                                        locationLongitude,
                                        momentId,
                                        selectedContactIds.toList(),
                                        selectedDateMs
                                    )
                                }
                            }
                        }
                    },
                    enabled = title.isNotBlank() && !isUploading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    if (isUploading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            "Save Moment",
                            fontFamily = dev.pranav.reconnect.ui.theme.UltraFamily,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }

        if (showLocationPicker) {
            val locationSheetState = rememberBottomSheetState(
                SheetValue.Hidden,
                setOf(SheetValue.Expanded, SheetValue.Hidden)
            )
            ModalBottomSheet(
                onDismissRequest = { showLocationPicker = false },
                sheetState = locationSheetState,
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() },
                sheetGesturesEnabled = false
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, top = 8.dp)
                ) {
                    Text(
                        text = "Select location",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.6f)
                    ) {
                        val mapStyle = AppSessionStore(LocalContext.current).getMapStyle()
                        val cameraState = rememberCameraState()
                        val styleState = rememberStyleState()

                        MaplibreMap(
                            baseStyle = BaseStyle.Uri(mapStyle.styleUri),
                            cameraState = cameraState,
                            styleState = styleState,
                            options = MapOptions(ornamentOptions = OrnamentOptions.OnlyLogo),
                            onMapClick = { pos, _ ->
                                locationLatitude = pos.latitude
                                locationLongitude = pos.longitude
                                ClickResult.Pass
                            }
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .size(28.dp)
                                .background(Color.White.copy(alpha = 0.9f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(onClick = {
                            locationLatitude = null
                            locationLongitude = null
                            showLocationPicker = false
                        }) {
                            Text("Clear")
                        }
                        Spacer(Modifier.weight(1f))
                        Button(onClick = { showLocationPicker = false }) {
                            Text("Save")
                        }
                    }
                }
            }
        }

        if (showContactSheet) {
            ModalBottomSheet(
                onDismissRequest = { showContactSheet = false },
                sheetState = rememberBottomSheetState(
                    SheetValue.Hidden,
                    setOf(SheetValue.Expanded, SheetValue.Hidden)
                ),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
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

        if (imageForCaption != null) {
            val currentImage = imageForCaption!!
            ModalBottomSheet(
                onDismissRequest = { imageForCaption = null },
                sheetState = rememberBottomSheetState(
                    SheetValue.Hidden,
                    setOf(SheetValue.Expanded, SheetValue.Hidden)
                ),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp, top = 8.dp)
                ) {
                    Text(
                        text = "Photo Caption",
                        fontFamily = PlayfairFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    GlassInputField(
                        value = currentImage.caption ?: "",
                        onValueChange = { newCaption ->
                            if (newCaption.length <= 150) {
                                selectedImages = selectedImages.map {
                                    if (it.id == currentImage.id) it.copy(caption = newCaption) else it
                                }
                                imageForCaption = imageForCaption?.copy(caption = newCaption)
                            }
                        },
                        placeholder = "Write something about this photo...",
                        singleLine = false,
                        minLines = 3,
                        maxLines = 6
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { imageForCaption = null },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            "Done",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold
                        )
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
                        onSave(
                            title,
                            description,
                            category,
                            successfulUploads,
                            isCoreMemory,
                            wasPresent,
                            groupName.takeIf { it.isNotBlank() },
                            locationMood.takeIf { it.isNotBlank() },
                            locationLatitude,
                            locationLongitude,
                            currentMomentId,
                            selectedContactIds.toList(),
                            selectedDateMs
                        )
                        uploadErrors = emptyList()
                        successfulUploads = emptyList()
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

@Composable
private fun FormSection(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = label,
            fontFamily = PlayfairFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 8.dp)
        )
        content()
    }
}

@Composable
private fun GlassInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = {
            Text(
                placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = PlusJakartaSansFamily,
                fontSize = 14.sp
            )
        },
        textStyle = LocalTextStyle.current.copy(
            fontFamily = PlusJakartaSansFamily,
            fontSize = 16.sp
        ),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.42f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.42f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(20.dp),
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines
    )
}
