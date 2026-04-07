package dev.pranav.reconnect.ui.circle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.PainterState
import com.github.panpf.sketch.rememberAsyncImageState
import dev.pranav.reconnect.core.model.Contact
import dev.pranav.reconnect.data.port.AppContainer
import dev.pranav.reconnect.ui.components.CurrentUserAvatar
import dev.pranav.reconnect.ui.components.ScreenTitle
import dev.pranav.reconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialCircleScreen(
    onContactClick: (String) -> Unit,
    onAddClick: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: SocialCircleViewModel = viewModel()
) {
    val contacts by viewModel.filteredContacts.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CreamBackground,
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = GoldPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding())
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "New Connection",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        },
        contentWindowInsets = WindowInsets.statusBars
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item { CircleHeader() }

            item {
                CircleSearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.updateSearch(it) }
                )
            }

            item {
                CategoryFilterRow(
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.selectCategory(it) }
                )
            }

            if (contacts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 64.dp)
                            .animateItem(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank() || selectedCategory != "All")
                                "No contacts match your search."
                            else
                                "Add someone to your circle!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MediumGray
                        )
                    }
                }
            } else {
                items(contacts, key = { it.id }) { contact ->
                    val category = contact.relationship.toCircleCategory()
                    val cardColor = contact.seedColorArgb
                        ?.let(::Color)
                        ?.let { seeded -> colorSchemeFromSeed(seeded).primaryContainer.copy(alpha = 0.95f) }
                        ?: when (category) {
                        "Family" -> AmberCardStart
                        "Friends" -> BlueCard
                        "Work" -> PurpleCard
                        else -> CardYellowLight
                    }
                    val actionLabel = if (category == "Work") "Schedule" else "Message"
                    val actionIcon: ImageVector =
                        if (category == "Work") Icons.Default.CalendarToday
                        else Icons.Default.ChatBubble
                    val status = when {
                        contact.isActive -> "Active Now"
                        contact.title.isNotBlank() -> contact.title
                        else -> "Reconnect · ${contact.reconnectInterval.label}"
                    }

                    CircleContactCard(
                        contact = contact,
                        status = status,
                        tag = contact.relationship.toTagLabel(),
                        backgroundColor = cardColor,
                        actionLabel = actionLabel,
                        actionIcon = actionIcon,
                        onCardClick = { onContactClick(contact.id) },
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CircleHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 24.dp,
                vertical = 20.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ScreenTitle(
            text = "Your Circle",
            modifier = Modifier.weight(1f)
        )

        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            border = BorderStroke(2.dp, GoldPrimary)
        ) {
            CurrentUserAvatar(
                modifier = Modifier.size(48.dp),
                showBorder = false
            )
        }
    }
}

@Composable
private fun CircleSearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        placeholder = { Text("Find someone...", color = MediumGray) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldPrimary) },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search", tint = MediumGray)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun CategoryFilterRow(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("All", "Family", "Friends", "Work", "Other")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) GoldPrimary else Color.White,
                onClick = { onCategorySelected(category) }
            ) {
                Text(
                    text = category,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else CharcoalText
                    )
                )
            }
        }
    }
}

@Composable
private fun ContactAvatar(
    contactId: String,
    name: String,
    seedColorArgb: Int?,
    modifier: Modifier = Modifier
) {
    val state = rememberAsyncImageState()
    val seedColor =
        seedColorArgb?.let { Color(it) } ?: DefaultSeedColor
    val scheme = colorSchemeFromSeed(seedColor)

    SeedColorTheme(colors = scheme) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceContainer)
        ) {
            if (state.painterState !is PainterState.Success) {
                val initials =
                    name.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .joinToString("").takeIf { it.isNotEmpty() } ?: "?"
                Text(
                    text = initials,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            AsyncImage(
                uri = AppContainer.photoResolver.resolveContactPhoto(contactId),
                state = state,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CircleContactCard(
    modifier: Modifier = Modifier,
    contact: Contact,
    status: String,
    tag: String,
    backgroundColor: Color,
    actionLabel: String = "Message",
    actionIcon: ImageVector = Icons.Default.ChatBubble,
    onCardClick: () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(48.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = onCardClick
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    modifier = Modifier.size(90.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    ContactAvatar(
                        contactId = contact.id,
                        name = contact.name,
                        seedColorArgb = contact.seedColorArgb,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Surface(
                    color = Color.White.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = tag,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp,
                            color = CharcoalText
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = contact.name,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontFamily = PlayfairFamily,
                    fontWeight = FontWeight.Black
                ),
                color = CharcoalText
            )
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = CharcoalText.copy(alpha = 0.7f)
            )

            Spacer(Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {},
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shapes = ButtonDefaults.shapes(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.8f),
                        contentColor = CharcoalText
                    )
                ) {
                    Icon(actionIcon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(actionLabel, fontWeight = FontWeight.Bold)
                }

                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.3f),
                    onClick = {}
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Call, contentDescription = "Call", tint = CharcoalText)
                    }
                }
            }
        }
    }
}
