package dev.pranav.reconnect.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import dev.pranav.reconnect.R


val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

val PlusJakartaSansFamily = FontFamily(
    Font(googleFont = GoogleFont("Plus Jakarta Sans"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Plus Jakarta Sans"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Plus Jakarta Sans"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Plus Jakarta Sans"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Plus Jakarta Sans"), fontProvider = provider, weight = FontWeight.ExtraBold),
)

val UltraFamily = FontFamily(
    Font(googleFont = GoogleFont("Ultra"), fontProvider = provider, weight = FontWeight.Normal)
)

val PlayfairFamily = FontFamily(
    Font(googleFont = GoogleFont("Playfair Display"), fontProvider = provider, weight = FontWeight.Bold),
    Font(googleFont = GoogleFont("Playfair Display"), fontProvider = provider, weight = FontWeight.ExtraBold),
    Font(googleFont = GoogleFont("Playfair Display"), fontProvider = provider, weight = FontWeight.Black),
)

val RobotoFlexFamily = FontFamily(
    Font(googleFont = GoogleFont("Roboto Flex"), fontProvider = provider, weight = FontWeight.Light),
    Font(googleFont = GoogleFont("Roboto Flex"), fontProvider = provider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Roboto Flex"), fontProvider = provider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Roboto Flex"), fontProvider = provider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Roboto Flex"), fontProvider = provider, weight = FontWeight.Bold),
)

val SerifFontFamily = PlayfairFamily
val SansFontFamily = PlusJakartaSansFamily

val Typography = Typography(
    displayLarge  = TextStyle(fontFamily = PlayfairFamily,      fontWeight = FontWeight.Bold,      fontSize = 48.sp, lineHeight = 56.sp),
    displayMedium = TextStyle(fontFamily = UltraFamily,         fontWeight = FontWeight.Normal,    fontSize = 36.sp, lineHeight = 42.sp),
    displaySmall  = TextStyle(fontFamily = UltraFamily,         fontWeight = FontWeight.Normal,    fontSize = 28.sp, lineHeight = 34.sp),
    headlineLarge  = TextStyle(fontFamily = PlayfairFamily,     fontWeight = FontWeight.Bold,      fontSize = 28.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = PlayfairFamily,     fontWeight = FontWeight.Bold,      fontSize = 24.sp, lineHeight = 30.sp),
    headlineSmall  = TextStyle(fontFamily = PlayfairFamily,     fontWeight = FontWeight.Bold,      fontSize = 20.sp, lineHeight = 26.sp),
    titleLarge  = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.Bold,      fontSize = 20.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall  = TextStyle(fontFamily = PlusJakartaSansFamily, fontWeight = FontWeight.SemiBold,  fontSize = 14.sp, lineHeight = 20.sp),
    bodyLarge  = TextStyle(fontFamily = PlusJakartaSansFamily,  fontWeight = FontWeight.Normal,    fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = PlusJakartaSansFamily,  fontWeight = FontWeight.Normal,    fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall  = TextStyle(fontFamily = PlusJakartaSansFamily,       fontWeight = FontWeight.Normal,    fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge  = TextStyle(fontFamily = PlusJakartaSansFamily,      fontWeight = FontWeight.Medium,    fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.8.sp),
    labelMedium = TextStyle(fontFamily = PlusJakartaSansFamily,      fontWeight = FontWeight.Medium,    fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.6.sp),
    labelSmall  = TextStyle(fontFamily = PlusJakartaSansFamily,      fontWeight = FontWeight.Medium,    fontSize = 10.sp, lineHeight = 14.sp, letterSpacing = 0.5.sp),
)
