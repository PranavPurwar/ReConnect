package dev.pranav.reconnect.ui.maps

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.pranav.reconnect.core.model.PastMoment
import dev.pranav.reconnect.core.session.AppSessionStore
import dev.pranav.reconnect.core.session.MapStyle
import dev.pranav.reconnect.ui.theme.CharcoalText
import dev.pranav.reconnect.ui.theme.MediumGray
import io.github.jan.supabase.annotations.SupabaseInternal
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.maplibre.compose.camera.rememberCameraState
import org.maplibre.compose.expressions.dsl.asString
import org.maplibre.compose.expressions.dsl.const
import org.maplibre.compose.expressions.dsl.feature
import org.maplibre.compose.expressions.dsl.offset
import org.maplibre.compose.expressions.value.SymbolAnchor
import org.maplibre.compose.layers.CircleLayer
import org.maplibre.compose.layers.SymbolLayer
import org.maplibre.compose.map.GestureOptions
import org.maplibre.compose.map.MapOptions
import org.maplibre.compose.map.MaplibreMap
import org.maplibre.compose.material3.CompassButton
import org.maplibre.compose.material3.ExpandingAttributionButton
import org.maplibre.compose.material3.ScaleBar
import org.maplibre.compose.sources.GeoJsonData
import org.maplibre.compose.sources.rememberGeoJsonSource
import org.maplibre.compose.style.BaseStyle
import org.maplibre.compose.style.rememberStyleState
import org.maplibre.compose.util.ClickResult
import org.maplibre.spatialk.geojson.Feature
import org.maplibre.spatialk.geojson.FeatureCollection
import org.maplibre.spatialk.geojson.Point
import org.maplibre.spatialk.geojson.toJson

@OptIn(SupabaseInternal::class, ExperimentalMaterial3Api::class)
@Composable
fun MomentsMap(
    moments: List<PastMoment>,
    onMomentClick: (String?) -> Unit,
    modifier: Modifier = Modifier,
    onMapClick: () -> Unit = {},
    mapStyle: MapStyle = AppSessionStore(LocalContext.current).getMapStyle()
) {
    val camera = rememberCameraState()
    val styleState = rememberStyleState()

    Box(modifier = modifier) {
        MaplibreMap(
            modifier = Modifier.fillMaxSize(),
            baseStyle = BaseStyle.Uri(mapStyle.styleUri),
            cameraState = camera,
            styleState = styleState,
            options = MapOptions(
                gestureOptions = GestureOptions.Standard
            ),
            onMapClick = { _, offset ->
                onMapClick()
                val features = camera.projection?.queryRenderedFeatures(offset)
                if (!features.isNullOrEmpty()) {
                    println("Clicked on ${features[0].toJson()}")
                    onMomentClick(null)
                    ClickResult.Consume
                } else {
                    ClickResult.Pass
                }
            }
        ) {
            val momentPoints = moments.mapNotNull { moment ->
                val lat = moment.locationLatitude
                val lng = moment.locationLongitude
                if (lat != null && lng != null) {
                    Feature(
                        id = JsonPrimitive(moment.id),
                        geometry = Point(lng, lat),
                        properties = buildJsonObject {
                            put("id", moment.id)
                            put("title", moment.title)
                        }
                    )
                } else {
                    null
                }
            }

            if (momentPoints.isNotEmpty()) {
                val featureCollection = FeatureCollection(momentPoints)
                val source =
                    rememberGeoJsonSource(GeoJsonData.JsonString(featureCollection.toJson()))

                CircleLayer(
                    id = "moment-pins",
                    source = source,
                    color = const(Color.Yellow)
                )

                SymbolLayer(
                    id = "moment-labels",
                    source = source,
                    textField = feature.get("title").asString(),
                    textAnchor = const(SymbolAnchor.Top),
                    textOffset = offset(
                        0f.em,
                        1f.em
                    ),
                    //textSize = const((1.2).em),
                    textColor = const(Color.White),
                    textHaloColor = const(Color.Black),
                    textHaloWidth = const(1.dp),
                    onClick = { features ->
                        //val clickedId = features.firstOrNull()?.id?.toString()?.replace("\"", "")
                        //selectedMoment = moments.find { it.id == clickedId }
                        ClickResult.Consume
                    }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    viewModel: MomentsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = dev.pranav.reconnect.di.AppViewModelProvider.Factory)
) {
    val moments by viewModel.moments.collectAsStateWithLifecycle()

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
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.background,
                            Color.White
                        )
                    )
                )
        ) {
            MomentsMap(
                moments = moments,
                onMomentClick = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
