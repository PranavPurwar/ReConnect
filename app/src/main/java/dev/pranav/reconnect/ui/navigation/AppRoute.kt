package dev.pranav.reconnect.ui.navigation

import kotlinx.serialization.Serializable

sealed class AppRoute {
    @Serializable
    data object Login
    @Serializable
    data object SignUp
    @Serializable
    data object VerifyEmail
    @Serializable
    data object Onboarding
    @Serializable
    data object Picker
    @Serializable
    data object Main
    @Serializable
    data object Settings
    @Serializable
    data object NotificationSettings

    @Serializable
    data object PrivacyPolicy
    @Serializable
    data object EditProfile
    @Serializable
    data class ConnectionDetail(val contactId: String)
    @Serializable
    data class AddConnection(val contactId: String? = null)
    @Serializable
    data object Gallery
    @Serializable
    data class ImagePreview(val index: Int)
}
