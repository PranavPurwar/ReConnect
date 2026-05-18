package dev.pranav.reconnect.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.material3.carousel.HorizontalUncontainedCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.PainterState
import com.github.panpf.sketch.rememberAsyncImageState
import dev.pranav.reconnect.di.AppContainer
import dev.pranav.reconnect.ui.theme.CharcoalText
import dev.pranav.reconnect.ui.theme.CreamBackground
import dev.pranav.reconnect.ui.theme.SerifFontFamily
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomentDetailScreen(
    viewModel: MomentDetailViewModel,
    onBack: () -> Unit,
    onContactClick: (String) -> Unit,
    onImageClick: (Int, List<String>, List<String>) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val moment = state.moment
    val clipboardManager = LocalClipboard.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(moment?.title ?: "Moment") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (moment == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Moment not found")
            }
        } else {
            val dateLabel =
                SimpleDateFormat("MMMM d, yyyy", LocalLocale.current.platformLocale).format(
                    Date(moment.dateEpochMs)
                )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(24.dp)
            ) {
                item {
                    Text(
                        text = moment.title,
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontFamily = SerifFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = CharcoalText
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (moment.images.isNotEmpty()) {
                        val uris =
                            moment.images.map { AppContainer.photoResolver.resolveMomentPhoto(it.uri) }
                        val captions = moment.images.map { it.caption ?: "" }
                        val carouselState = rememberCarouselState(itemCount = { uris.size })
                        HorizontalUncontainedCarousel(
                            state = carouselState,
                            itemWidth = 200.dp,
                            itemSpacing = 8.dp,
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) { index ->
                            val uri = uris[index]
                            AsyncImage(
                                uri = uri,
                                contentDescription = captions.getOrNull(index) ?: "Moment Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f)
                                    .maskClip(MaterialTheme.shapes.extraLarge)
                                    .clickable { onImageClick(index, uris, captions) }
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (state.contacts.isNotEmpty()) {
                        Text(
                            text = "With",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(state.contacts) { contact ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable { onContactClick(contact.id) }
                                        .padding(4.dp)
                                ) {
                                    val state = rememberAsyncImageState()
                                    val initials =
                                        contact.name.split(" ").filter { it.isNotBlank() }.take(2)
                                            .joinToString("") { it.take(1) }.uppercase()
                                    val seedColor = contact.seedColorArgb
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (seedColor != null) Color(seedColor).copy(
                                                    0.15f
                                                ) else CreamBackground
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (state.painterState !is PainterState.Success) {
                                            Text(
                                                initials,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = CharcoalText
                                                )
                                            )
                                        }
                                        AsyncImage(
                                            uri = AppContainer.photoResolver.resolveContactPhoto(
                                                contact.id
                                            ),
                                            state = state,
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = contact.name.split(" ").first(),
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (moment.description.isNotBlank()) {
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        SelectionContainer {
                            Text(
                                text = moment.description,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}
