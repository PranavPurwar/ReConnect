package dev.pranav.reconnect.ui.add

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.pranav.reconnect.ui.theme.DefaultSeedColor

class AddConnectionState {
    var name by mutableStateOf("")
    var title by mutableStateOf("")
    var phone by mutableStateOf("")
    var selectedRelationship by mutableStateOf<String?>(null)
    var notes by mutableStateOf("")
    var photoUri by mutableStateOf<String?>(null)
    var birthdayYear by mutableStateOf<Int?>(null)
    var birthdayMonth by mutableStateOf<Int?>(null)
    var birthdayDay by mutableStateOf<Int?>(null)
    var showContactSearch by mutableStateOf(false)
    var showBirthdayPicker by mutableStateOf(false)
    var showColorPicker by mutableStateOf(false)
    var didPrefillForContactId by mutableStateOf<String?>(null)
    var photoBitmap by mutableStateOf<android.graphics.Bitmap?>(null)
    var seedColor by mutableStateOf(DefaultSeedColor)
    var isSeedColorCustom by mutableStateOf(false)
}
