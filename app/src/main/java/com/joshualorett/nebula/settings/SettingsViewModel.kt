package com.joshualorett.nebula.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.joshualorett.nebula.apod.ApodRepository
import kotlinx.coroutines.launch

/**
 * [ViewModel] of Settings.
 * Created by Joshua on 5/31/2020.
 */
class SettingsViewModel(private val apodRepository: ApodRepository): ViewModel() {
    fun clearData() {
        viewModelScope.launch {
            apodRepository.clearResources()
        }
    }

    class SettingsViewModelFactory(private val apodRepository: ApodRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return SettingsViewModel(apodRepository) as T
        }
    }
}