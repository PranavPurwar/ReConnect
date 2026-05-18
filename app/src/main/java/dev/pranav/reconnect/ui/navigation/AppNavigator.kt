package dev.pranav.reconnect.ui.navigation

import androidx.navigation.NavController

object NavPayloadKeys {
    const val GALLERY_TITLE = "gallery_title"
    const val GALLERY_URIS = "gallery_uris"
    const val GALLERY_CAPTIONS = "gallery_captions"
    const val PREVIEW_URIS = "preview_uris"
    const val PREVIEW_CAPTIONS = "preview_captions"
}

fun NavController.openGallery(
    title: String,
    uris: List<String>,
    captions: List<String> = emptyList()
) {
    currentBackStackEntry?.savedStateHandle?.set(NavPayloadKeys.GALLERY_TITLE, title)
    currentBackStackEntry?.savedStateHandle?.set(NavPayloadKeys.GALLERY_URIS, ArrayList(uris))
    currentBackStackEntry?.savedStateHandle?.set(
        NavPayloadKeys.GALLERY_CAPTIONS,
        ArrayList(captions)
    )
    navigate(AppRoute.Gallery)
}

fun NavController.galleryPayload(): Triple<String, ArrayList<String>, ArrayList<String>> {
    val handle = previousBackStackEntry?.savedStateHandle
    val title = handle?.get<String>(NavPayloadKeys.GALLERY_TITLE) ?: ""
    val uris = handle?.get<ArrayList<String>>(NavPayloadKeys.GALLERY_URIS) ?: arrayListOf()
    val captions = handle?.get<ArrayList<String>>(NavPayloadKeys.GALLERY_CAPTIONS) ?: arrayListOf()
    return Triple(title, uris, captions)
}

fun NavController.openImagePreview(
    index: Int,
    uris: ArrayList<String>,
    captions: ArrayList<String> = arrayListOf()
) {
    currentBackStackEntry?.savedStateHandle?.set(NavPayloadKeys.PREVIEW_URIS, uris)
    currentBackStackEntry?.savedStateHandle?.set(NavPayloadKeys.PREVIEW_CAPTIONS, captions)
    navigate(AppRoute.ImagePreview(index))
}

fun NavController.previewPayload(): Pair<ArrayList<String>, ArrayList<String>> {
    val handle = previousBackStackEntry?.savedStateHandle
    val uris = handle?.get<ArrayList<String>>(NavPayloadKeys.PREVIEW_URIS) ?: arrayListOf()
    val captions = handle?.get<ArrayList<String>>(NavPayloadKeys.PREVIEW_CAPTIONS) ?: arrayListOf()
    return uris to captions
}
