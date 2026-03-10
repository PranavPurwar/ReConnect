package dev.pranav.reconnect.ui.navigation

sealed interface AppRoute {
    data object Onboarding : AppRoute
    data object ContactPicker : AppRoute
    data object Home : AppRoute
    data class PersonDetail(val contactId: String) : AppRoute
}

