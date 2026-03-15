package dev.pranav.reconnect.ui.detail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.panpf.sketch.AsyncImage
import dev.pranav.reconnect.data.model.MomentCategory
import dev.pranav.reconnect.data.model.PastMoment
import dev.pranav.reconnect.ui.theme.*
import dev.pranav.reconnect.util.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonDetailScreen(
    contactId: String,
    onBack: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(),
    onEditDetails: (String) -> Unit = {},
    onOpenGallery: (title: String, uris: List<String>) -> Unit = { _, _ -> },
    viewModel: PersonDetailViewModel = viewModel()
) {
    LaunchedEffect(contactId) { viewModel.loadContact(contactId) }

    val state by viewModel.uiState.collectAsState()
    val contact = state.contact
    val persistedSeedColor = contact?.seedColorArgb?.let(::Color) ?: DefaultSeedColor
    val context = LocalContext.current
    var showLogSheet by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }

    val contactPhotoBitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        key1 = contact?.photoUri,
        key2 = contact?.seedColorArgb,
        key3 = context
    ) {
        if (contact?.seedColorArgb != null) {
            value = null
            return@produceState
        }
        value = withContext(Dispatchers.IO) {
            contact?.photoUri?.let { uri ->
                runCatching { uri.toUri().toBitmap(context) }.getOrNull()
            }
        }
    }

    val detailContent: @Composable () -> Unit = {
        Scaffold(
        modifier = Modifier,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            CenterAlignedTopAppBar(
                title = {  },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showMoreMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        if (contact != null) {
                            DropdownMenuItem(
                                text = {
                                    Text(if (contact.isImportant) "Unmark Important" else "Mark as Important")
                                },
                                leadingIcon = {
                                    Icon(
                                        if (contact.isImportant) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (contact.isImportant) MaterialTheme.colorScheme.primary else LocalContentColor.current
                                    )
                                },
                                onClick = {
                                    viewModel.toggleImportant()
                                    showMoreMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Edit Details") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = LocalContentColor.current
                                    )
                                },
                                onClick = {
                                    onEditDetails(contact.id)
                                    showMoreMenu = false
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Delete Contact") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = LocalContentColor.current
                                    )
                                },
                                onClick = {
                                    viewModel.deleteContact()
                                    showMoreMenu = false
                                    onBack()
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent.copy(alpha = 0.95f),
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showLogSheet = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                Icon(Icons.Default.EditNote, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Log Moment", fontFamily = PlusJakartaSansFamily)
            }
        },
        containerColor = Color.Transparent
    ) { scaffoldPadding ->
        if (contact == null) return@Scaffold

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = innerPadding.calculateBottomPadding() + 96.dp)
        ) {
            item {
            // Hero Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primaryContainer, Color.Transparent)
                        )
                    )
                    .padding(top = scaffoldPadding.calculateTopPadding(), bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(8.dp))

                    Box(contentAlignment = Alignment.BottomEnd) {
                        Surface(
                            modifier = Modifier
                                .size(120.dp)
                                .border(3.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                        ) {
                            if (contact.photoUri != null) {
                                AsyncImage(
                                    uri = contact.photoUri,
                                    contentDescription = contact.name,
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = contact.name.split(" ").take(2)
                                            .mapNotNull { it.firstOrNull() }.joinToString(""),
                                        style = MaterialTheme.typography.headlineLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        if (contact.isActive) {
                            Box(
                                modifier = Modifier
                                    .offset(x = (-4).dp, y = (-4).dp)
                                    .size(20.dp)
                                    .border(3.dp, MaterialTheme.colorScheme.surface, CircleShape)
                                    .clip(CircleShape)
                                    .background(ActiveGreen)
                            )
                        }
                    }

                    if (contact.isImportant) {
                        Spacer(Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "Important",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = contact.name,
                        style = MaterialTheme.typography.headlineLarge,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )

                    val subtitle = listOfNotNull(
                        contact.title.takeIf { it.isNotBlank() },
                        contact.relationship.takeIf { it.isNotBlank() }
                    ).joinToString(" • ")
                    if (subtitle.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )
                    }

                    if (contact.phoneNumber.isNotBlank()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = contact.phoneNumber,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Text(
                                "Reconnects ${contact.reconnectInterval.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        val healthColor = when (state.relationshipHealth) {
                            RelationshipHealth.STRONG -> ActiveGreen
                            RelationshipHealth.NEUTRAL -> MaterialTheme.colorScheme.primary
                            RelationshipHealth.FADING -> Color(0xFFE57373)
                        }
                        val healthLabel = when (state.relationshipHealth) {
                            RelationshipHealth.STRONG -> "Strong Bond"
                            RelationshipHealth.NEUTRAL -> "Nurturing"
                            RelationshipHealth.FADING -> "Fading"
                        }
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = healthColor.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(healthColor)
                                )
                                Text(
                                    healthLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = healthColor
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            } // end hero header item

            item {
                // Stats Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        value = state.pastMoments.size.toString(),
                        label = "moments",
                        icon = Icons.Default.BookmarkBorder
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        value = state.daysSinceLastContact?.let {
                            if (it == 0) "Today" else "${it}d ago"
                        } ?: "—",
                        label = "last contact",
                        icon = Icons.Default.AccessTime
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        value = state.daysUntilBirthday?.let { "${it}d" } ?: "—",
                        label = "till birthday",
                        icon = Icons.Default.Cake
                    )
                }
            }

            item {
                // Action Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            if (contact.phoneNumber.isNotBlank()) {
                                context.startActivity(
                                    Intent(Intent.ACTION_SENDTO).apply {
                                        data = "smsto:${contact.phoneNumber}".toUri()
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Message")
                    }
                    Button(
                        onClick = {
                            if (contact.phoneNumber.isNotBlank()) {
                                context.startActivity(
                                    Intent(Intent.ACTION_DIAL).apply {
                                        data = "tel:${contact.phoneNumber}".toUri()
                                    }
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Call")
                    }
                }
            }

            if (state.daysUntilBirthday != null && state.daysUntilBirthday!! <= 30) {
                item {
                    BirthdayReminderCard(
                        daysUntilBirthday = state.daysUntilBirthday!!,
                        name = contact.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                    )
                }
            }

            if (contact.notes.isNotBlank()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                    ) {
                        Text("Notes", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.height(12.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.StickyNote2,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp).padding(top = 2.dp)
                                )
                                Text(
                                    contact.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Next Talk Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 32.dp)
                ) {
                    Text("Next Talk", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    state.nextTalkDate,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                                Text("AI PREP", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onTertiaryContainer, letterSpacing = 1.sp)
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondary) {
                                    Text("PRO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSecondary, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            state.aiPrepBullets.forEach { bullet ->
                                Row(modifier = Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
                                    Box(modifier = Modifier.padding(top = 7.dp).size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                    Spacer(Modifier.width(10.dp))
                                    Text(bullet, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_INSERT).apply {
                                        data = android.provider.CalendarContract.Events.CONTENT_URI
                                        putExtra(android.provider.CalendarContract.Events.TITLE, "Reconnect with ${contact.name}")
                                        putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "ReConnect reminder — ${state.nextTalkDate}")
                                    }
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(28.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary, contentColor = MaterialTheme.colorScheme.onSecondary)
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Add to Calendar", fontFamily = PlusJakartaSansFamily)
                            }
                        }
                    }
                }
            }

            item {
                // Past Moments Header + Filter Chips
                Column(modifier = Modifier.fillMaxWidth().padding(top = 32.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Past Moments", style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.weight(1f))
                        if (state.pastMoments.isNotEmpty()) {
                            Text("${state.pastMoments.size} logged", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = state.selectedCategory == null,
                                onClick = { viewModel.setFilter(null) },
                                label = { Text("All", style = MaterialTheme.typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                            )
                        }
                        items(MomentCategory.entries.toList()) { cat ->
                            FilterChip(
                                selected = state.selectedCategory == cat,
                                onClick = { viewModel.setFilter(cat) },
                                label = { Text(cat.name.lowercase().replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelMedium) },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primary, selectedLabelColor = MaterialTheme.colorScheme.onPrimary)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }

            if (state.filteredMoments.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EditNote, contentDescription = null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                if (state.selectedCategory != null) "No ${state.selectedCategory!!.name.lowercase()} moments yet" else "No moments yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.selectedCategory == null) {
                                Spacer(Modifier.height(4.dp))
                                Text("Tap 'Log Moment' to record your first memory.", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(state.filteredMoments) { index, moment ->
                    PastMomentItem(
                        moment = moment,
                        isLast = index == state.filteredMoments.lastIndex,
                        onOpenGallery = onOpenGallery,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }
        }
    }

    if (showLogSheet) {
        LogMomentSheet(
            onDismiss = { showLogSheet = false },
            onSave = { title, description, category, imageUris ->
                viewModel.logMoment(contactId, title, description, category, imageUris)
                showLogSheet = false
            }
        )
    }
    }

    val dynamicScheme = remember(contactPhotoBitmap) {
        contactPhotoBitmap?.let { bitmap ->
            colorSchemeFromSeed(extractSeedColorOrDefault(bitmap, DefaultSeedColor))
        }
    }

    SeedColorTheme(
        colors = dynamicScheme ?: colorSchemeFromSeed(persistedSeedColor),
        content = detailContent
    )
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    icon: ImageVector
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BirthdayReminderCard(daysUntilBirthday: Int, name: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.20f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Cake,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (daysUntilBirthday == 0) "🎉 Birthday today!"
                    else "Birthday in $daysUntilBirthday days",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                Text(
                    "Don't forget to wish ${name.split(" ").first()} a happy birthday!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.82f)
                )
            }
        }
    }
}

@Composable
private fun PastMomentItem(
    moment: PastMoment,
    isLast: Boolean,
    onOpenGallery: (title: String, uris: List<String>) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    // ...existing code...
    val icon = when (moment.category) {
        MomentCategory.DINING -> Icons.Default.Restaurant
        MomentCategory.ART -> Icons.Default.Palette
        MomentCategory.OUTDOORS -> Icons.Default.Park
        MomentCategory.GENERAL -> Icons.Default.ChatBubbleOutline
    }
    val iconBg = when (moment.category) {
        MomentCategory.DINING -> Color(0xFFFFF3E0)
        MomentCategory.ART -> Color(0xFFF3E5F5)
        MomentCategory.OUTDOORS -> Color(0xFFE8F5E9)
        MomentCategory.GENERAL -> Color(0xFFE3F2FD)
    }
    val iconTint = when (moment.category) {
        MomentCategory.DINING -> Color(0xFFE65100)
        MomentCategory.ART -> Color(0xFF7B1FA2)
        MomentCategory.OUTDOORS -> Color(0xFF2E7D32)
        MomentCategory.GENERAL -> Color(0xFF1565C0)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = iconBg,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = iconTint
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    moment.dateLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    moment.title,
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFontFamily)
                )
                if (moment.imageUris.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    LazyRow(
                        modifier = Modifier.height(80.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(moment.imageUris) { uri ->
                            Box(
                                modifier = Modifier
                                    .size(width = 100.dp, height = 80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onOpenGallery(moment.title, moment.imageUris) }
                            ) {
                                ImageThumbnailPlaceholder(uri)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = { onOpenGallery(moment.title, moment.imageUris) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            if (moment.imageUris.size == 1) "View Photo" else "View ${moment.imageUris.size} Photos",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (moment.description.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        moment.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ImageThumbnailPlaceholder(uri: String) {
    val isRealUri = uri.startsWith("content://") || uri.startsWith("file://") || uri.startsWith("http")
    if (isRealUri) {
        AsyncImage(
            uri = uri,
            contentDescription = null,
            modifier = Modifier
                .size(width = 100.dp, height = 80.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
    } else {
        val bgColor = if (uri.contains("1")) Color(0xFFE8D5B7) else Color(0xFF2E7D6A)
        Box(
            modifier = Modifier
                .size(width = 100.dp, height = 80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Palette,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
