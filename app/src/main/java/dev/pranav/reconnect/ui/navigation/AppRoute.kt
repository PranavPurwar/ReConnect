package dev.pranav.reconnect.ui.navigation

import android.net.Uri

object AppRoute {
    const val ONBOARDING = "onboarding"
    const val PICKER = "picker"
    const val MAIN = "main"
    const val PERSON_DETAIL = "detail/{contactId}"
    const val ADD_CONNECTION = "add?contactId={contactId}"
    const val GALLERY = "gallery"
    const val IMAGE_PREVIEW = "imagePreview/{index}"

    fun personDetail(contactId: String) = "detail/${Uri.encode(contactId)}"
    fun addConnection(contactId: String?) =
        if (contactId.isNullOrBlank()) "add" else "add?contactId=${Uri.encode(contactId)}"
    fun imagePreview(index: Int) = "imagePreview/$index"
}
