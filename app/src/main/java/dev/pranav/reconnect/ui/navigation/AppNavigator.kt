package dev.pranav.reconnect.ui.navigation

import androidx.navigation.NavController

object NavPayloadKeys {
    const val GALLERY_TITLE = "gallery_title"
    const val GALLERY_URIS = "gallery_uris"
    const val PREVIEW_URIS = "preview_uris"
}

fun NavController.openGallery(title: String, uris: List<String>) {
    currentBackStackEntry?.savedStateHandle?.set(NavPayloadKeys.GALLERY_TITLE, title)
    currentBackStackEntry?.savedStateHandle?.set(NavPayloadKeys.GALLERY_URIS, ArrayList(uris))
    navigate(AppRoute.GALLERY)
}

fun NavController.galleryPayload(): Pair<String, ArrayList<String>> {
    val handle = previousBackStackEntry?.savedStateHandle
    val title = handle?.get<String>(NavPayloadKeys.GALLERY_TITLE) ?: ""
    val uris = handle?.get<ArrayList<String>>(NavPayloadKeys.GALLERY_URIS) ?: arrayListOf()
    return title to uris
}

fun NavController.openImagePreview(index: Int, uris: ArrayList<String>) {
    currentBackStackEntry?.savedStateHandle?.set(NavPayloadKeys.PREVIEW_URIS, uris)
    navigate(AppRoute.imagePreview(index))
}

fun NavController.previewPayload(): ArrayList<String> {
    return previousBackStackEntry?.savedStateHandle
        ?.get<ArrayList<String>>(NavPayloadKeys.PREVIEW_URIS) ?: arrayListOf()
}

