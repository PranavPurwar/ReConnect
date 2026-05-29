package dev.pranav.reconnect.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import dev.pranav.reconnect.ui.circle.SocialCircleViewModel
import dev.pranav.reconnect.ui.detail.ConnectionDetailViewModel
import dev.pranav.reconnect.ui.detail.MomentDetailViewModel
import dev.pranav.reconnect.ui.home.HomeViewModel
import dev.pranav.reconnect.ui.journey.JourneyViewModel
import dev.pranav.reconnect.ui.maps.MomentsViewModel
import dev.pranav.reconnect.ui.picker.ContactPickerViewModel
import dev.pranav.reconnect.ui.settings.EditProfileViewModel

object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            val contactStore = AppContainer.contactStore
            HomeViewModel(contactStore, AppContainer.momentStore)
        }
        initializer {
            SocialCircleViewModel(AppContainer.contactStore)
        }
        initializer {
            JourneyViewModel(AppContainer.contactStore, AppContainer.momentStore)
        }
        initializer {
            ConnectionDetailViewModel(AppContainer.contactStore, AppContainer.momentStore)
        }
        initializer {
            ContactPickerViewModel(AppContainer.contactStore)
        }
        initializer {
            EditProfileViewModel()
        }
        initializer {
            MomentsViewModel(AppContainer.momentStore)
        }
        initializer {
            val savedStateHandle = this.createSavedStateHandle()
            val momentId = savedStateHandle.get<String>("momentId") ?: ""
            MomentDetailViewModel(momentId, AppContainer.contactStore, AppContainer.momentStore)
        }
    }
}
