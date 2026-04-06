package dev.pranav.reconnect.ui.navigation

import android.net.Uri

object AppRoute {
    const val LOGIN = "login"
    const val SIGNUP = "signup"
    const val ONBOARDING = "onboarding"
    const val PICKER = "picker"
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val EDIT_PROFILE = "edit_profile"
    const val PERSON_DETAIL = "detail/{contactId}"
    const val ADD_CONNECTION = "add?contactId={contactId}"
    const val GALLERY = "gallery"
    const val IMAGE_PREVIEW = "imagePreview/{index}"
    const val VERIFY_EMAIL = "verify-email"

    fun personDetail(contactId: String) = "detail/${Uri.encode(contactId)}"
    fun addConnection(contactId: String?) =
        if (contactId.isNullOrBlank()) "add" else "add?contactId=${Uri.encode(contactId)}"
    fun imagePreview(index: Int) = "imagePreview/$index"
}
