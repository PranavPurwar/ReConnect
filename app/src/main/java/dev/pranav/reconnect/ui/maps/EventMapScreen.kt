package dev.pranav.reconnect.ui.maps;

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.core.session.MapStyle
import dev.pranav.reconnect.ui.home.RecentMoment
import dev.pranav.reconnect.ui.theme.CharcoalText
import dev.pranav.reconnect.ui.theme.MediumGray
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.material3.CompassButton
import org.maplibre.compose.material3.ExpandingAttributionButton
import org.maplibre.compose.material3.ScaleBar
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.toJson

@Composable
fun MomentsMap(
    moments: List<RecentMoment>,
    onMomentClick: (String?) -> Unit,
    onMapClick: () -> Unit = {},
    mapStyle: MapStyle = AppSessionStore(LocalContext.current).getMapStyle()
) {
    val camera = rememberCameraState()
    val styleState = rememberStyleState()

    Box(Modifier.fillMaxSize()) {
        MaplibreMap(
            baseStyle = BaseStyle.Uri(mapStyle.styleUri),
            cameraState = camera,
            styleState = styleState,
            options = MapOptions(
                gestureOptions = GestureOptions.Standard
            ),
            onMapClick = { pos, offset ->
                onMapClick()
                val features = camera.projection?.queryRenderedFeatures(offset)
                if (!features.isNullOrEmpty()) {
                    println("Clicked on ${features[0].toJson()}")
                    onMomentClick(null)
                    ClickResult.Consume
                } else {
                    ClickResult.Pass
                }
            },
            onMapLongClick = { pos, offset ->
                println("Long click at $pos")
                ClickResult.Pass
            },
        )

        Box(modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)) {
            ScaleBar(camera.metersPerDpAtTarget, modifier = Modifier.align(Alignment.TopStart))
            CompassButton(camera, modifier = Modifier.align(Alignment.TopEnd))
            ExpandingAttributionButton(
                cameraState = camera,
                styleState = styleState,
                modifier = Modifier.align(Alignment.BottomEnd),
                contentAlignment = Alignment.BottomEnd,
            )
        }
    }
}

@Composable
fun MapScreen(
    onBack: () -> Unit,
    moments: List<RecentMoment> = emptyList()
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Your Map",
                            style = MaterialTheme.typography.titleLarge,
                            color = CharcoalText
                        )
                        Text(
                            "${moments.size} ${if (moments.size == 1) "location" else "locations"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MediumGray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CharcoalText
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
                )
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            Color.White
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            MomentsMap(moments, onMomentClick = {})
        }
    }
}
