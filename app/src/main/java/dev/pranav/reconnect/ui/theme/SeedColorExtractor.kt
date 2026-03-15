package dev.pranav.reconnect.ui.theme

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.palette.graphics.Palette
import kotlin.math.abs

fun extractSeedColor(bitmap: Bitmap): Color? {
    val palette = Palette.from(bitmap)
        .clearFilters()
        .maximumColorCount(24)
        .generate()

    val candidates = listOfNotNull(
        palette.vibrantSwatch?.toCandidate(sourceWeight = 1.00f),
        palette.dominantSwatch?.toCandidate(sourceWeight = 0.94f),
        palette.mutedSwatch?.toCandidate(sourceWeight = 0.86f),
        palette.lightVibrantSwatch?.toCandidate(sourceWeight = 0.88f),
        palette.darkVibrantSwatch?.toCandidate(sourceWeight = 0.84f)
    )

    if (candidates.isEmpty()) return null

    val maxPopulation = candidates.maxOf { it.population }.coerceAtLeast(1)
    val best = candidates.maxByOrNull { it.score(maxPopulation) } ?: return null
    return Color(best.rgb)
}

fun extractSeedColorOrDefault(
    bitmap: Bitmap?,
    defaultSeedColor: Color = DefaultSeedColor
): Color {
    if (bitmap == null) return defaultSeedColor
    return extractSeedColor(bitmap) ?: defaultSeedColor
}

private data class SeedColorCandidate(
    val rgb: Int,
    val saturation: Float,
    val lightness: Float,
    val population: Int,
    val sourceWeight: Float
) {
    fun score(maxPopulation: Int): Float {
        val populationScore = (population.toFloat() / maxPopulation).coerceIn(0f, 1f)
        val saturationScore = ((saturation - 0.12f) / 0.70f).coerceIn(0f, 1f)
        val lightnessScore = (1f - abs(lightness - 0.52f) / 0.52f).coerceIn(0f, 1f)

        return sourceWeight * 0.22f +
            populationScore * 0.40f +
            saturationScore * 0.26f +
            lightnessScore * 0.12f
    }
}

private fun Palette.Swatch.toCandidate(sourceWeight: Float): SeedColorCandidate {
    return SeedColorCandidate(
        rgb = rgb,
        saturation = hsl[1],
        lightness = hsl[2],
        population = population,
        sourceWeight = sourceWeight
    )
}

