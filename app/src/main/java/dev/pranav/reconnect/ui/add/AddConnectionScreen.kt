package dev.pranav.reconnect.ui.add

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.pranav.reconnect.data.model.Contact
import dev.pranav.reconnect.data.model.ReconnectInterval
import dev.pranav.reconnect.data.repository.ContactRepository
import dev.pranav.reconnect.ui.home.HomeViewModel
import dev.pranav.reconnect.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val SageColor = Color(0xFFE2E8DA)
private val AmberSoftColor = Color(0xFFFDF2D5)
private val MorphedFabShape = RoundedCornerShape(topStart = 48.dp, topEnd = 16.dp, bottomEnd = 48.dp, bottomStart = 40.dp)
private val SquircleShape = RoundedCornerShape(28.dp)
private val relationships = listOf("Family", "Friend", "Colleague", "Other")

@Composable
fun AddConnectionScreen(
    onBack: () -> Unit,
    onAdded: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var selectedRelationship by remember { mutableStateOf<String?>(null) }
    var notes by remember { mutableStateOf("") }
    var showContactSearch by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            AddConnectionFooter(
                onHomeClick = onBack,
                onAdd = {
                    viewModel.addContact(
                        name = name,
                        phone = phone,
                        title = "",
                        relationship = selectedRelationship ?: "",
                        interval = ReconnectInterval.MONTHLY,
                        notes = notes
                    )
                    onAdded()
                },
                canAdd = name.isNotBlank()
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = padding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CircleIconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = CharcoalText)
                }
                CircleIconButton(onClick = {}) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = CharcoalText)
                }
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "New Connection",
                fontFamily = UltraFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 36.sp,
                color = GoldPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Grow your circle with intention",
                fontFamily = RobotoFlexFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = CharcoalText.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Surface(
                    onClick = { showContactSearch = true },
                    shape = CircleShape,
                    color = SageColor,
                    shadowElevation = 2.dp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.ImportContacts, contentDescription = null, tint = Color(0xFF2E7D32))
                        Text(
                            "Sync from Contacts",
                            fontFamily = PlusJakartaSansFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }

                FormSection(label = "Name") {
                    TextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Who are you reconnecting with?",
                                color = CharcoalText.copy(alpha = 0.4f),
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 14.sp
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(fontFamily = PlusJakartaSansFamily, fontSize = 16.sp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AmberSoftColor,
                            unfocusedContainerColor = AmberSoftColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = CharcoalText,
                            unfocusedTextColor = CharcoalText
                        ),
                        shape = SquircleShape,
                        singleLine = true
                    )
                    Text(
                        "Full name or a nickname you use",
                        fontFamily = RobotoFlexFamily,
                        fontSize = 12.sp,
                        color = CharcoalText.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                FormSection(label = "Phone") {
                    TextField(
                        value = phone,
                        onValueChange = { phone = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                "Optional — for quick-dial reminders",
                                color = CharcoalText.copy(alpha = 0.4f),
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 14.sp
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(fontFamily = PlusJakartaSansFamily, fontSize = 16.sp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = AmberSoftColor,
                            unfocusedContainerColor = AmberSoftColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = CharcoalText,
                            unfocusedTextColor = CharcoalText
                        ),
                        shape = SquircleShape,
                        singleLine = true
                    )
                }

                FormSection(label = "Relationship") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        relationships.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                row.forEach { rel ->
                                    val isSelected = selectedRelationship == rel
                                    Surface(
                                        onClick = { selectedRelationship = if (isSelected) null else rel },
                                        modifier = Modifier.weight(1f),
                                        shape = SquircleShape,
                                        color = if (isSelected) GoldPrimary.copy(alpha = 0.15f) else Color.White,
                                        border = BorderStroke(2.dp, if (isSelected) GoldPrimary else GoldPrimary.copy(alpha = 0.25f))
                                    ) {
                                        Text(
                                            text = rel,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 18.dp),
                                            textAlign = TextAlign.Center,
                                            fontFamily = PlusJakartaSansFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 15.sp,
                                            color = if (isSelected) GoldPrimary else CharcoalText
                                        )
                                    }
                                }
                                if (row.size == 1) Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                    Text(
                        "Helps us suggest the right tone for reminders",
                        fontFamily = RobotoFlexFamily,
                        fontSize = 12.sp,
                        color = CharcoalText.copy(alpha = 0.5f),
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                FormSection(label = "Memory Jogger") {
                    TextField(
                        value = notes,
                        onValueChange = { notes = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        placeholder = {
                            Text(
                                "Where did you meet? What do they love?",
                                color = CharcoalText.copy(alpha = 0.4f),
                                fontFamily = PlusJakartaSansFamily,
                                fontSize = 14.sp
                            )
                        },
                        textStyle = LocalTextStyle.current.copy(fontFamily = PlusJakartaSansFamily, fontSize = 16.sp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SageColor.copy(alpha = 0.5f),
                            unfocusedContainerColor = SageColor.copy(alpha = 0.5f),
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = CharcoalText,
                            unfocusedTextColor = CharcoalText
                        ),
                        shape = SquircleShape,
                        maxLines = 5
                    )
                }

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .width(64.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(GoldPrimary.copy(alpha = 0.2f))
                    )
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (showContactSearch) {
        ContactSearchSheet(
            onDismiss = { showContactSearch = false },
            onContactPicked = { contact ->
                name = contact.name
                phone = contact.phoneNumber
                showContactSearch = false
            }
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
            ContactRepository().getDeviceContacts(context.contentResolver)
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
                color = CharcoalText
            )

            TextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text("Search by name…", fontFamily = PlusJakartaSansFamily, fontSize = 14.sp, color = CharcoalText.copy(alpha = 0.4f))
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = CharcoalText.copy(alpha = 0.5f)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
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
                    CircularProgressIndicator(color = GoldPrimary)
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
                        color = CharcoalText.copy(alpha = 0.5f),
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
                                color = GoldPrimary.copy(alpha = 0.15f),
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = contact.name.take(1).uppercase(),
                                        fontFamily = PlusJakartaSansFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = GoldPrimary
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    contact.name,
                                    fontFamily = PlusJakartaSansFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = CharcoalText
                                )
                                if (contact.phoneNumber.isNotBlank()) {
                                    Text(
                                        contact.phoneNumber,
                                        fontFamily = RobotoFlexFamily,
                                        fontSize = 12.sp,
                                        color = CharcoalText.copy(alpha = 0.5f)
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
            color = CharcoalText,
            modifier = Modifier.padding(start = 8.dp)
        )
        content()
    }
}

@Composable
private fun CircleIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.6f),
        shadowElevation = 2.dp,
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) { content() }
    }
}

@Composable
private fun AddConnectionFooter(
    onHomeClick: () -> Unit,
    onAdd: () -> Unit,
    canAdd: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.92f),
        shadowElevation = 4.dp,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Home,
                    contentDescription = "Home",
                    tint = GoldPrimary,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable(onClick = onHomeClick)
                )
                Icon(
                    Icons.Default.People,
                    contentDescription = "Circle",
                    tint = CharcoalText.copy(alpha = 0.35f),
                    modifier = Modifier.size(28.dp)
                )
            }

            Button(
                onClick = onAdd,
                enabled = canAdd,
                modifier = Modifier
                    .weight(1f)
                    .height(68.dp),
                shape = MorphedFabShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GoldPrimary,
                    contentColor = CharcoalText,
                    disabledContainerColor = GoldPrimary.copy(alpha = 0.35f),
                    disabledContentColor = CharcoalText.copy(alpha = 0.4f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(22.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "ADD TO CIRCLE",
                    fontFamily = UltraFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}
