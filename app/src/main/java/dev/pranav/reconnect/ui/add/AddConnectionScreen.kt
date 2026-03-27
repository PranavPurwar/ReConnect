package dev.pranav.reconnect.ui.add

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.panpf.sketch.AsyncImage
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.ContactFormData
import dev.pranav.reconnect.data.repository.SystemContactsDataSource
import dev.pranav.reconnect.ui.home.HomeViewModel
import dev.pranav.reconnect.ui.theme.*
import dev.pranav.reconnect.util.takePersistableReadPermissionIfPossible
import dev.pranav.reconnect.util.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val MorphedFabShape = RoundedCornerShape(topStart = 48.dp, topEnd = 16.dp, bottomEnd = 48.dp, bottomStart = 40.dp)
private val relationships = listOf("Family", "Friend", "Colleague", "Other")
private val fallbackSeedColors = listOf(
    Color(0xFFE53935),
    Color(0xFFD81B60),
    Color(0xFF5E35B1),
    Color(0xFF3949AB),
    Color(0xFF1E88E5),
    Color(0xFF00897B),
    Color(0xFF43A047),
)

private fun decodePhotoBitmap(context: android.content.Context, photoUri: String?): android.graphics.Bitmap? {
    if (photoUri.isNullOrBlank()) return null
    return runCatching { photoUri.toUri().toBitmap(context) }.getOrNull()
}

private fun provisionalSeedColorFromPhotoUri(photoUri: String?): Color {
    if (photoUri.isNullOrBlank()) return DefaultSeedColor
    val hue = ((photoUri.hashCode().toLong() and 0x7FFFFFFF) % 360L).toFloat()
    return Color.hsv(hue = hue, saturation = 0.42f, value = 0.82f)
}

@Composable
fun AddConnectionScreen(
    contactIdToEdit: String? = null,
    onBack: () -> Unit,
    onAdded: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRelationship by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    var birthdayYear by remember { mutableStateOf<Int?>(null) }
    var birthdayMonth by remember { mutableStateOf<Int?>(null) }
    var birthdayDay by remember { mutableStateOf<Int?>(null) }
    var showContactSearch by remember { mutableStateOf(false) }
    var showBirthdayPicker by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var didPrefillForContactId by remember(contactIdToEdit) { mutableStateOf<String?>(null) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val existingContact = remember(contactIdToEdit, uiState.quickCatchUps) {
        contactIdToEdit?.let { id -> uiState.quickCatchUps.firstOrNull { it.first.id == id }?.first }
    }
    val isEditMode = existingContact != null
    val birthdayFormatter = remember { SimpleDateFormat("MMMM d", Locale.US) }

    LaunchedEffect(existingContact?.id) {
        val contact = existingContact ?: return@LaunchedEffect
        if (didPrefillForContactId == contact.id) return@LaunchedEffect

        name = contact.name
        title = contact.title
        phone = contact.phoneNumber
        selectedRelationship = contact.relationship.takeIf { it.isNotBlank() }
        notes = contact.notes
        photoUri = contact.photoUri
        birthdayYear = contact.birthdayYear
        birthdayMonth = contact.birthdayMonth
        birthdayDay = contact.birthdayDay
        didPrefillForContactId = contact.id
    }

    var photoBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    var seedColor by remember(existingContact?.id, photoUri) {
        mutableStateOf(existingContact?.seedColorArgb?.let(::Color) ?: provisionalSeedColorFromPhotoUri(photoUri))
    }
    var isSeedColorCustom by remember(existingContact?.id) {
        mutableStateOf(existingContact?.seedColorArgb != null)
    }

    LaunchedEffect(photoUri) {
        val fallbackSeedColor = existingContact?.seedColorArgb?.let(::Color)
            ?: provisionalSeedColorFromPhotoUri(photoUri)
        val decodedBitmap = withContext(Dispatchers.IO) {
            decodePhotoBitmap(context, photoUri)
        }
        photoBitmap = decodedBitmap
        if (!isSeedColorCustom) {
            seedColor = fallbackSeedColor
            if (decodedBitmap != null) {
                seedColor = extractSeedColorOrDefault(decodedBitmap, fallbackSeedColor)
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            context.takePersistableReadPermissionIfPossible(it)
            photoUri = it.toString()
        }
    }

    val expressiveScheme = remember(seedColor) {
        colorSchemeFromSeed(seedColor)
    }
    val selectableSeedColors = remember(photoBitmap) {
        extractVibrantSeedColors(
            bitmap = photoBitmap,
            fallbackColors = fallbackSeedColors
        )
    }
    val expressiveColors = remember(expressiveScheme) { addConnectionExpressiveColors(expressiveScheme) }

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

    val screenContent: @Composable () -> Unit = {
        Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            AddConnectionFooter(
                onAdd = {
                    if (isEditMode) {
                        existingContact.let { contact ->
                            viewModel.updateContact(
                                contact.copy(
                                    name = name.trim(),
                                    title = title.trim(),
                                    phoneNumber = phone.trim(),
                                    relationship = selectedRelationship.orEmpty().trim(),
                                    notes = notes.trim(),
                                    birthdayYear = birthdayYear,
                                    birthdayMonth = birthdayMonth,
                                    birthdayDay = birthdayDay,
                                    photoUri = photoUri,
                                    seedColorArgb = seedColor.toArgb()
                                )
                            )
                        }
                    } else {
                        viewModel.addContact(
                            ContactFormData(
                                name = name,
                                phone = phone,
                                title = title,
                                relationship = selectedRelationship ?: "",
                                notes = notes,
                                birthdayYear = birthdayYear,
                                birthdayMonth = birthdayMonth,
                                birthdayDay = birthdayDay,
                                photoUri = photoUri,
                                seedColorArgb = seedColor.toArgb()
                            )
                        )
                    }
                    onAdded()
                },
                canAdd = name.isNotBlank(),
                isEditMode = isEditMode,
                expressiveScheme = expressiveScheme
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
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
                    .padding(bottom = padding.calculateBottomPadding())
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        onClick = onBack,
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.78f),
                        shadowElevation = 2.dp,
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Text(
                        text = if (isEditMode) "Edit Connection" else "New Connection",
                        fontFamily = UltraFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )

                    Spacer(Modifier.size(46.dp))
                }

                Spacer(Modifier.height(18.dp))

                Box(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Surface(
                        onClick = { photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        shape = RoundedCornerShape(topStart = 52.dp, topEnd = 34.dp, bottomEnd = 54.dp, bottomStart = 44.dp),
                        color = expressiveColors.avatarContainer,
                        border = BorderStroke(2.dp, Color.White.copy(alpha = 0.75f)),
                        shadowElevation = 5.dp,
                        modifier = Modifier.size(150.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!photoUri.isNullOrBlank()) {
                                AsyncImage(
                                    uri = photoUri,
                                    contentDescription = "Profile photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 52.dp,
                                                topEnd = 34.dp,
                                                bottomEnd = 54.dp,
                                                bottomStart = 44.dp
                                            )
                                        )
                                )
                            } else {
                                Icon(
                                    Icons.Default.AddAPhoto,
                                    contentDescription = "Add photo",
                                    tint = expressiveColors.avatarIcon,
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = expressiveScheme.primary,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit photo",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text(
                    text = if (photoUri.isNullOrBlank()) "Add Profile Picture" else "Update Profile Picture",
                    fontFamily = PlayfairFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Capture a moment to remember",
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                )

                Spacer(Modifier.height(24.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 560.dp),
                    verticalArrangement = Arrangement.spacedBy(22.dp)
                ) {
                    Surface(
                        onClick = { showContactSearch = true },
                        shape = CircleShape,
                        color = expressiveColors.syncChipContainer,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.ImportContacts, contentDescription = null, tint = expressiveColors.syncChipIcon)
                            Text(
                                "Sync from Contacts",
                                fontFamily = PlusJakartaSansFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = expressiveColors.syncChipText
                            )
                        }
                    }

                    FormSection(label = "Full Name") {
                        GlassInputField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "Who are you connecting with?"
                        )
                    }

                    FormSection(label = "Title") {
                        GlassInputField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = "Optional - e.g. Designer, Manager"
                        )
                    }

                    FormSection(label = "Phone") {
                        GlassInputField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = "Optional - for quick reminders"
                        )
                    }

                    FormSection(label = "Birthday") {
                        val birthdayLabel =
                            if (birthdayYear != null && birthdayMonth != null && birthdayDay != null) {
                            val cal = Calendar.getInstance().apply {
                                set(Calendar.YEAR, birthdayYear!!)
                                set(Calendar.MONTH, birthdayMonth!! - 1)
                                set(Calendar.DAY_OF_MONTH, birthdayDay!!)
                            }
                            birthdayFormatter.format(cal.time)
                        } else null

                        Surface(
                            onClick = { showBirthdayPicker = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.44f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = expressiveScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    birthdayLabel ?: "Tap to add birthday",
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 15.sp,
                                    color = if (birthdayLabel != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                if (birthdayLabel != null) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear birthday",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .clickable {
                                                birthdayYear = null; birthdayMonth =
                                                null; birthdayDay = null
                                            }
                                    )
                                }
                            }
                        }
                    }

                    FormSection(label = "Relationship Circle") {
                        RelationshipChips(
                            selectedRelationship = selectedRelationship,
                            expressiveColors = expressiveColors,
                            expressiveScheme = expressiveScheme,
                            onRelationshipSelect = { rel ->
                                selectedRelationship = if (selectedRelationship == rel) null else rel
                            }
                        )
                    }

                    FormSection(label = "Theme Color") {
                        SeedColorSelector(
                            colors = selectableSeedColors,
                            selectedColor = seedColor,
                            onSelect = {
                                seedColor = it
                                isSeedColorCustom = true
                            },
                            onOpenCustomPicker = { showColorPicker = true },
                            onUsePhotoColor = {
                                val bitmap = photoBitmap ?: return@SeedColorSelector
                                seedColor = extractSeedColorOrDefault(bitmap, seedColor)
                                isSeedColorCustom = false
                            },
                            canUsePhotoColor = photoBitmap != null
                        )
                    }

                    FormSection(label = "Memory Jogger") {
                        TextField(
                            value = notes,
                            onValueChange = { notes = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(132.dp),
                            placeholder = {
                                Text(
                                    "Write a note about how you met or what they love...",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontSize = 14.sp
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(fontFamily = PlusJakartaSansFamily, fontSize = 16.sp),
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
                            shape = RoundedCornerShape(topStart = 22.dp, topEnd = 28.dp, bottomEnd = 26.dp, bottomStart = 24.dp),
                            maxLines = 5
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
    }

    if (showContactSearch) {
        ContactSearchSheet(
            onDismiss = { showContactSearch = false },
            onContactPicked = { contact ->
                name = contact.name
                phone = contact.phoneNumber
                photoUri = contact.photoUri
                showContactSearch = false
            }
        )
    }

    if (showBirthdayPicker) {
        BirthdayPickerDialog(
            onDismiss = { showBirthdayPicker = false },
            onConfirm = { year, month, day ->
                birthdayYear = year
                birthdayMonth = month
                birthdayDay = day
                showBirthdayPicker = false
            }
        )
    }

    if (showColorPicker) {
        CustomSeedColorDialog(
            initialColor = seedColor,
            onDismiss = { showColorPicker = false },
            onConfirm = {
                seedColor = it
                isSeedColorCustom = true
                showColorPicker = false
            }
        )
    }

    val bitmap = photoBitmap
    if (bitmap != null) {
        SeedColorTheme(bitmap = bitmap, content = screenContent)
    } else {
        SeedColorTheme(colors = expressiveScheme, content = screenContent)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SeedColorSelector(
    colors: List<Color>,
    selectedColor: Color,
    onSelect: (Color) -> Unit,
    onOpenCustomPicker: () -> Unit,
    onUsePhotoColor: () -> Unit,
    canUsePhotoColor: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            colors.forEach { color ->
                val isSelected = selectedColor.toArgb() == color.toArgb()
                Surface(
                    onClick = { onSelect(color) },
                    shape = CircleShape,
                    color = color,
                    border = BorderStroke(
                        width = if (isSelected) 3.dp else 1.dp,
                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.White.copy(
                            alpha = 0.55f
                        )
                    ),
                    modifier = Modifier.size(34.dp)
                ) {}
            }

            Surface(
                onClick = onOpenCustomPicker,
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.75f)),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = "Custom color",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        TextButton(
            onClick = onUsePhotoColor,
            enabled = canUsePhotoColor,
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
        ) {
            Text(
                text = "Use photo color",
                fontFamily = PlusJakartaSansFamily,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CustomSeedColorDialog(
    initialColor: Color,
    onDismiss: () -> Unit,
    onConfirm: (Color) -> Unit
) {
    val hsv = remember(initialColor) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor.toArgb(), it) }
    }
    var hue by remember(initialColor) { mutableStateOf(hsv[0]) }
    var saturation by remember(initialColor) { mutableStateOf(hsv[1]) }
    var value by remember(initialColor) { mutableStateOf(hsv[2]) }
    val selected = remember(hue, saturation, value) { Color.hsv(hue, saturation, value) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        title = { Text("Pick a Color") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = selected,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {}

                Text("Hue", style = MaterialTheme.typography.labelMedium)
                Slider(value = hue, onValueChange = { hue = it }, valueRange = 0f..360f)

                Text("Saturation", style = MaterialTheme.typography.labelMedium)
                Slider(value = saturation, onValueChange = { saturation = it }, valueRange = 0f..1f)

                Text("Brightness", style = MaterialTheme.typography.labelMedium)
                Slider(value = value, onValueChange = { value = it }, valueRange = 0f..1f)
            }
        }
    )
}

@Composable
private fun GlassInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = {
            Text(
                placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontFamily = PlusJakartaSansFamily,
                fontSize = 14.sp
            )
        },
        textStyle = LocalTextStyle.current.copy(fontFamily = PlusJakartaSansFamily, fontSize = 16.sp),
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
        singleLine = singleLine
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RelationshipChips(
    selectedRelationship: String?,
    expressiveColors: AddConnectionExpressiveColors,
    expressiveScheme: ColorScheme,
    onRelationshipSelect: (String) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        relationships.forEach { rel ->
            val isSelected = selectedRelationship == rel
            Surface(
                onClick = { onRelationshipSelect(rel) },
                shape = CircleShape,
                color = when {
                    isSelected -> expressiveScheme.primary
                    rel == "Friend" -> expressiveColors.relationshipFriendContainer
                    rel == "Colleague" -> expressiveColors.relationshipColleagueContainer
                    else -> Color.White.copy(alpha = 0.65f)
                },
                border = BorderStroke(1.dp, if (isSelected) expressiveScheme.primary else Color.White.copy(alpha = 0.65f))
            ) {
                Text(
                    text = rel,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp),
                    fontFamily = PlusJakartaSansFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (isSelected) expressiveScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthdayPickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int, day: Int) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        onConfirm(
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH) + 1,
                            cal.get(Calendar.DAY_OF_MONTH)
                        )
                    }
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text("Set", color = MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    ) {
        DatePicker(
            state = datePickerState,
            headline = {
                Text(
                    "Select Birthday",
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp)
                )
            },
            showModeToggle = true
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContactSearchSheet(
    onDismiss: () -> Unit,
    onContactPicked: (Contact) -> Unit
) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var query by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val loaded = withContext(Dispatchers.IO) {
            SystemContactsDataSource().getSystemContacts(context.contentResolver)
        }
        contacts = loaded
        isLoading = false
    }

    val filtered = remember(query, contacts) {
        if (query.isBlank()) contacts
        else contacts.filter { it.name.contains(query, ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Pick a Contact",
                fontFamily = PlayfairFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search by name…", fontFamily = PlusJakartaSansFamily, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp),
                singleLine = true
            )

            when {
                isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }

                filtered.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (query.isBlank()) "No contacts found on this device."
                        else "No contacts match \"$query\".",
                        fontFamily = RobotoFlexFamily,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                else -> LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filtered, key = { it.id }) { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { onContactPicked(contact) }
                                .padding(horizontal = 12.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(
                                    modifier = Modifier.size(44.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!contact.photoUri.isNullOrBlank()) {
                                        AsyncImage(
                                            uri = contact.photoUri,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = contact.name.take(1).uppercase(),
                                                    fontFamily = PlusJakartaSansFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 16.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    contact.name,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (contact.phoneNumber.isNotBlank()) {
                                    Text(
                                        contact.phoneNumber,
                                        fontFamily = RobotoFlexFamily,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
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
private fun AddConnectionFooter(
    onAdd: () -> Unit,
    canAdd: Boolean,
    isEditMode: Boolean,
    expressiveScheme: ColorScheme
) {
    Surface(
        color = Color.Transparent,
        shadowElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Button(
                onClick = onAdd,
                enabled = canAdd,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp),
                shape = MorphedFabShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = expressiveScheme.primary,
                    contentColor = expressiveScheme.onPrimary,
                    disabledContainerColor = expressiveScheme.surfaceVariant,
                    disabledContentColor = expressiveScheme.onSurfaceVariant
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(
                    if (isEditMode) Icons.Default.Save else Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (isEditMode) "SAVE DETAILS" else "ADD TO CIRCLE",
                    fontFamily = UltraFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
