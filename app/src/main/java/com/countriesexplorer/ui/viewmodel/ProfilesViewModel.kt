package com.countriesexplorer.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.countriesexplorer.data.local.ProfileEntity
import com.countriesexplorer.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    val profiles: StateFlow<List<ProfileEntity>> = profileRepository.profiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfileId: StateFlow<Long> = profileRepository.activeProfileId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileRepository.DEFAULT_PROFILE_ID)

    fun createProfile(name: String) {
        viewModelScope.launch {
            profileRepository.createProfile(name)
        }
    }

    fun renameProfile(id: Long, name: String) {
        viewModelScope.launch {
            profileRepository.renameProfile(id, name)
        }
    }

    fun setActiveProfile(id: Long) {
        viewModelScope.launch {
            profileRepository.setActiveProfile(id)
        }
    }

    fun deleteProfile(id: Long) {
        viewModelScope.launch {
            profileRepository.deleteProfile(id)
        }
    }
}
