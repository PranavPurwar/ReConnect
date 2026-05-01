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
import androidx.compose.ui.unit.dp
import dev.pranav.reconnect.ui.home.RecentMoment
import dev.pranav.reconnect.ui.theme.CharcoalText
import dev.pranav.reconnect.ui.theme.MediumGray
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.map.OrnamentOptions
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
    onMapClick: () -> Unit = {}
) {
    val camera = rememberCameraState()
    val styleState = rememberStyleState()

    Box(Modifier.fillMaxSize()) {
        MaplibreMap(
            baseStyle = BaseStyle.Uri("https://maps.geo.eu-west-1.amazonaws.com/v2/styles/Hybrid/descriptor?key=v1.public.eyJqdGkiOiJiOTNkYjBlZi04OWUzLTQxMGUtODFhMC0zYjZjZjVmZWZmMDgifYtukap0NBaJpcrS6Vit9j03GJgK9Bn-RSu5UCe3jkdSql2kKp3IEgLPtyLssbmKUdVO11sXddjK3ZOZy8V6QG0olv0K_1tOxyMIe4DAO3IV6H4VzHWiaXlbSakGiEgFLuHBdcfLDeMotye7N6rSRxuZb0CN9ytH9VjLly6-NEBRZezO_qPQyvdTFdeZsARIpL0f9YVpxPxPVvUcAWYCk5LpaPseRCDPrY5SlCdA1ZKqUA4F9RzxSTxB73Fel_SoNDkCNaux1VposBu791-uUpDzUpr7leKckrPXrpZ2hwnFbafVxFV9vq4fLTpB5KoBksuLfGNIwAx1RLLxWuMhE4c.ZGQzZDY2OGQtMWQxMy00ZTEwLWIyZGUtOGVjYzUzMjU3OGE4&color-scheme=Light"),
            cameraState = camera,
            styleState = styleState,
            options = MapOptions(ornamentOptions = OrnamentOptions.OnlyLogo),
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
