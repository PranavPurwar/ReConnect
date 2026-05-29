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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.PainterState
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.core.model.ContactFormData
import dev.pranav.reconnect.core.model.ReconnectInterval
import dev.pranav.reconnect.core.storage.DeviceContactsDataSource
import dev.pranav.reconnect.di.AppContainer
import dev.pranav.reconnect.ui.components.AppTopBar
import dev.pranav.reconnect.ui.home.HomeViewModel
import dev.pranav.reconnect.ui.theme.*
import dev.pranav.reconnect.util.decodePhotoBitmap
import dev.pranav.reconnect.util.loadRemoteBitmap
import dev.pranav.reconnect.util.provisionalSeedColorFromPhotoUri
import dev.pranav.reconnect.util.takePersistableReadPermissionIfPossible
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

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


@Composable
fun AddConnectionScreen(
    contactIdToEdit: String? = null,
    onAdded: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = dev.pranav.reconnect.di.AppViewModelProvider.Factory)
) {
    val context = LocalContext.current
    val state = remember { AddConnectionState() }
    var isSaving by remember { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val existingContact = remember(contactIdToEdit, uiState.quickCatchUps) {
        contactIdToEdit?.let { id -> uiState.quickCatchUps.firstOrNull { it.first.id == id }?.first }
    }
    val isEditMode = existingContact != null
    val birthdayFormatter = remember { java.text.SimpleDateFormat("MMMM d", Locale.US) }

    LaunchedEffect(existingContact?.id) {
        val contact = existingContact ?: return@LaunchedEffect
        if (state.didPrefillForContactId == contact.id) return@LaunchedEffect

        state.name = contact.name
        state.title = contact.title
        state.phone = contact.phoneNumber
        state.selectedRelationship = contact.relationship.takeIf { it.isNotBlank() }
        state.notes = contact.notes
        state.birthdayYear = contact.birthdayYear
        state.birthdayMonth = contact.birthdayMonth
        state.birthdayDay = contact.birthdayDay
        state.didPrefillForContactId = contact.id
    }

    LaunchedEffect(state.photoUri, existingContact?.id) {
        val fallbackSeedColor = existingContact?.seedColorArgb?.let(::Color)
            ?: provisionalSeedColorFromPhotoUri(state.photoUri)

        val decodedBitmap = withContext(Dispatchers.IO) {
            decodePhotoBitmap(context, state.photoUri) ?: existingContact?.id?.let {
                loadRemoteBitmap(context, it)
            }
        }

        state.photoBitmap = decodedBitmap
        if (!state.isSeedColorCustom) {
            state.seedColor = fallbackSeedColor
            if (decodedBitmap != null) {
                state.seedColor = extractSeedColorOrDefault(decodedBitmap, fallbackSeedColor)
            }
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            context.takePersistableReadPermissionIfPossible(it)
            state.photoUri = it.toString()
        }
    }

    val expressiveScheme = remember(state.seedColor) {
        colorSchemeFromSeed(state.seedColor)
    }
    val selectableSeedColors = remember(state.photoBitmap) {
        extractVibrantSeedColors(
            bitmap = state.photoBitmap,
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

    @OptIn(ExperimentalMaterial3Api::class)
    val hazeState = remember { HazeState() }

    @OptIn(ExperimentalMaterial3Api::class)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var isAdvancedOptionsExpanded by remember { mutableStateOf(false) }

    @OptIn(ExperimentalMaterial3Api::class)
    val screenContent: @Composable () -> Unit = {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0.dp),
            topBar = {
                AppTopBar(
                    title = if (isEditMode) "Edit Connection" else "New Connection",
                    scrollBehavior = scrollBehavior,
                    hazeState = hazeState
                )
            },
            bottomBar = {
                AddConnectionFooter(
                    onAdd = {
                        isSaving = true
                        if (isEditMode) {
                            existingContact.let { contact ->
                                viewModel.updateContact(
                                    contact.copy(
                                        name = state.name.trim(),
                                        title = state.title.trim(),
                                        phoneNumber = state.phone.trim(),
                                        relationship = state.selectedRelationship.orEmpty().trim(),
                                        notes = state.notes.trim(),
                                        birthdayYear = state.birthdayYear,
                                        birthdayMonth = state.birthdayMonth,
                                        birthdayDay = state.birthdayDay,
                                        seedColorArgb = state.seedColor.toArgb()
                                    ),
                                    photoUri = state.photoUri,
                                    onComplete = {
                                        isSaving = false
                                        onAdded()
                                    }
                                )
                            }
                        } else {
                            viewModel.addContact(
                                form = ContactFormData(
                                    name = state.name,
                                    phone = state.phone,
                                    title = state.title,
                                    relationship = state.selectedRelationship ?: "",
                                    notes = state.notes,
                                    interval = ReconnectInterval.MONTHLY,
                                    birthdayMonth = state.birthdayMonth,
                                    birthdayDay = state.birthdayDay,
                                    birthdayYear = state.birthdayYear,
                                    seedColorArgb = state.seedColor.toArgb()
                                ),
                                photoUri = state.photoUri,
                                onComplete = {
                                    isSaving = false
                                    onAdded()
                                }
                            )
                        }
                    },
                    canAdd = state.name.isNotBlank() && !isSaving,
                    isEditMode = isEditMode,
                    isSaving = isSaving
                )
            }
        ) { padding ->
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
                            top = padding.calculateTopPadding(),
                            bottom = padding.calculateBottomPadding()
                        )
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Spacer(Modifier.height(24.dp))

                    Box(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Surface(
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                            border = BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                            ),
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(150.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (!state.photoUri.isNullOrBlank()) {
                                    AsyncImage(
                                        uri = state.photoUri,
                                        contentDescription = "Profile photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(48.dp))
                                    )
                                } else if (existingContact != null) {
                                    val state = com.github.panpf.sketch.rememberAsyncImageState()
                                    val shape = RoundedCornerShape(48.dp)
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(shape)
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                    ) {
                                        if (state.painterState !is PainterState.Success) {
                                            val initials =
                                                existingContact.name.split(" ").take(2)
                                                    .mapNotNull {
                                                        it.firstOrNull()?.uppercaseChar()
                                                    }
                                                    .joinToString("")
                                            Text(initials, color = CharcoalText)
                                        }

                                        val resolvedUri =
                                            AppContainer.photoResolver.resolveContactPhoto(
                                                existingContact.id
                                            )

                                        AsyncImage(
                                            uri = resolvedUri,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
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

                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = if (state.photoUri.isNullOrBlank()) "Add Profile Picture" else "Update Profile Picture",
                        fontFamily = PlayfairFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(Modifier.height(24.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 560.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            onClick = { state.showContactSearch = true },
                            shape = CircleShape,
                            color = expressiveColors.syncChipContainer,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 22.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    Icons.Default.ImportContacts,
                                    contentDescription = null,
                                    tint = expressiveColors.syncChipIcon
                                )
                                Text(
                                    "Sync from Contacts",
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = expressiveColors.syncChipText
                                )
                            }
                        }
                        TextField(
                            value = state.name,
                            onValueChange = { state.name = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Who are you connecting with?",
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
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.9f
                                ),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.8f
                                ),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                    }

                    FormSection(label = "Phone") {
                        TextField(
                            value = state.phone,
                            onValueChange = { state.phone = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "Optional - for quick reminders",
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
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.9f
                                ),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                    alpha = 0.8f
                                ),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true
                        )
                    }

                    FormSection(label = "Birthday") {
                        val birthdayLabel =
                            if (state.birthdayYear != null && state.birthdayMonth != null && state.birthdayDay != null) {
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, state.birthdayYear!!)
                                    set(Calendar.MONTH, state.birthdayMonth!! - 1)
                                    set(Calendar.DAY_OF_MONTH, state.birthdayDay!!)
                                }
                                birthdayFormatter.format(cal.time)
                            } else null

                        Surface(
                            onClick = { state.showBirthdayPicker = true },
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
                                                state.birthdayYear = null; state.birthdayMonth =
                                                null; state.birthdayDay = null
                                            }
                                    )
                                }
                            }
                        }
                    }

                    FormSection(label = "Relationship Circle") {
                        RelationshipChips(
                            selectedRelationship = state.selectedRelationship,
                            expressiveColors = expressiveColors,
                            expressiveScheme = expressiveScheme,
                            onRelationshipSelect = { rel ->
                                state.selectedRelationship =
                                    if (state.selectedRelationship == rel) null else rel
                            }
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { isAdvancedOptionsExpanded = !isAdvancedOptionsExpanded }
                            .padding(vertical = 12.dp, horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "More details",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            if (isAdvancedOptionsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (isAdvancedOptionsExpanded) {
                        FormSection(label = "Title") {
                            TextField(
                                value = state.title,
                                onValueChange = { state.title = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        "Optional - e.g. Designer, Manager",
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
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.9f
                                    ),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = 0.8f
                                    ),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true
                            )
                        }

                        FormSection(label = "Theme Color") {
                            SeedColorSelector(
                                colors = selectableSeedColors,
                                selectedColor = state.seedColor,
                                onSelect = {
                                    state.seedColor = it
                                    state.isSeedColorCustom = true
                                },
                                onOpenCustomPicker = { state.showColorPicker = true },
                                onUsePhotoColor = {
                                    val bitmap = state.photoBitmap ?: return@SeedColorSelector
                                    state.seedColor =
                                        extractSeedColorOrDefault(bitmap, state.seedColor)
                                    state.isSeedColorCustom = false
                                },
                                canUsePhotoColor = state.photoBitmap != null
                            )
                        }

                        FormSection(label = "Notes") {
                            TextField(
                                value = state.notes,
                                onValueChange = { state.notes = it },
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
                                shape = RoundedCornerShape(
                                    topStart = 22.dp,
                                    topEnd = 28.dp,
                                    bottomEnd = 26.dp,
                                    bottomStart = 24.dp
                                ),
                                maxLines = 5
                            )
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }

    if (state.showContactSearch) {
        ContactSearchSheet(
            onDismiss = { state.showContactSearch = false },
            onContactPicked = { contact ->
                state.name = contact.name
                state.phone = contact.phoneNumber
                state.photoUri = contact.photoUri
                state.showContactSearch = false
            }
        )
    }

    if (state.showBirthdayPicker) {
        BirthdayPickerDialog(
            initialYear = state.birthdayYear,
            initialMonth = state.birthdayMonth,
            initialDay = state.birthdayDay,
            onDismiss = { state.showBirthdayPicker = false },
            onConfirm = { year, month, day ->
                state.birthdayYear = year
                state.birthdayMonth = month
                state.birthdayDay = day
                state.showBirthdayPicker = false
            }
        )
    }

    if (state.showColorPicker) {
        CustomSeedColorDialog(
            initialColor = state.seedColor,
            onDismiss = { state.showColorPicker = false },
            onConfirm = {
                state.seedColor = it
                state.isSeedColorCustom = true
                state.showColorPicker = false
            }
        )
    }

    val bitmap = state.photoBitmap
    if (bitmap != null) {
        SeedColorTheme(bitmap = bitmap, content = screenContent)
    } else {
        SeedColorTheme(colors = expressiveScheme, content = screenContent)
    }

    if (isSaving) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.35f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
        }
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
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
    var hue by remember(initialColor) { mutableFloatStateOf(hsv[0]) }
    var saturation by remember(initialColor) { mutableFloatStateOf(hsv[1]) }
    var value by remember(initialColor) { mutableFloatStateOf(hsv[2]) }
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
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        shape = RoundedCornerShape(16.dp),
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
    initialYear: Int?,
    initialMonth: Int?,
    initialDay: Int?,
    onDismiss: () -> Unit,
    onConfirm: (year: Int, month: Int, day: Int) -> Unit
) {
    val initialMillis = remember(initialYear, initialMonth, initialDay) {
        if (initialYear != null && initialMonth != null && initialDay != null) {
            val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.YEAR, initialYear)
                set(Calendar.MONTH, initialMonth - 1)
                set(Calendar.DAY_OF_MONTH, initialDay)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            cal.timeInMillis
        } else null
    }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                            .apply { timeInMillis = millis }
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
            DeviceContactsDataSource().getSystemContacts(context.contentResolver)
        }
        contacts = loaded
        isLoading = false
    }

    val filtered by remember {
        derivedStateOf {
            if (query.isBlank()) contacts
            else contacts.filter { it.name.contains(query, ignoreCase = true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(
            SheetValue.Hidden,
            setOf(SheetValue.Expanded, SheetValue.Hidden)
        ),
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
            modifier = Modifier.padding(start = 12.dp)
        )
        content()
    }
}

@Composable
private fun AddConnectionFooter(
    onAdd: () -> Unit,
    canAdd: Boolean,
    isEditMode: Boolean,
    isSaving: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        tonalElevation = 4.dp,
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
                enabled = canAdd && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        if (isEditMode) Icons.Default.Save else Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isEditMode) "Save details" else "Add to Circle"
                    )
                }
            }
        }
    }
}
