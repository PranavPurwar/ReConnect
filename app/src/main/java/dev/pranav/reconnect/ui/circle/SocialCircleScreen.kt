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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.panpf.sketch.AsyncImage
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialCircleScreen(
    onContactClick: (String) -> Unit,
    onAddClick: () -> Unit,
    innerPadding: PaddingValues = PaddingValues(),
    viewModel: SocialCircleViewModel = viewModel()
) {
    val contacts by viewModel.filteredContacts.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 120.dp),
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
                            .padding(vertical = 64.dp),
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
                    val cardColor = when (category) {
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
                        modifier = Modifier.padding(horizontal = 24.dp)
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
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Your Circle",
            style = MaterialTheme.typography.displayLarge.copy(
                fontFamily = UltraFamily,
                fontWeight = FontWeight.Black,
                fontSize = 44.sp,
                letterSpacing = (-1).sp
            ),
            color = CharcoalText
        )

        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            border = BorderStroke(2.dp, GoldPrimary)
        ) {
            AsyncImage(
                uri = "https://lh3.googleusercontent.com/aida-public/AB6AXuAVIAW1MXyPH0lbiJSkVqCmrcUIjgB6FhHPLV4LUIGpUtDo0_Xcl_F79XMqd5l7Rgc7libSBX82F_9kKWvNfE5VSiHAqBRMNAJ-l7mL_JBxOj6SpHJ2aVxruUiJB-voIaiCFerz4DeyWMGyI7RR3I6aVVl9sb_8UnlNAMY688sDCX3pnaYW1JuiSJY3a1gEV5M_iWcMAK4xIH-7R8ZS6uOCaugX9OaRpNkbOcq8w1qrwApqIdq6klUSsVC7eG0McegEh2U8wRFj__bx",
                contentDescription = "Profile",
                modifier = Modifier.clip(CircleShape),
                contentScale = ContentScale.Crop
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
private fun ContactAvatar(photoUri: String?, name: String, modifier: Modifier = Modifier) {
    if (!photoUri.isNullOrBlank()) {
        AsyncImage(
            uri = photoUri,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.background(GoldLight)
        ) {
            Text(
                text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GoldDark
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CircleContactCard(
    contact: Contact,
    status: String,
    tag: String,
    backgroundColor: Color,
    actionLabel: String = "Message",
    actionIcon: ImageVector = Icons.Default.ChatBubble,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
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
                        photoUri = contact.photoUri,
                        name = contact.name,
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
