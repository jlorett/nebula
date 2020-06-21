package com.joshualorett.nebula.settings

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshualorett.nebula.apod.ApodRepository
import kotlinx.coroutines.launch

/**
 * [ViewModel] of Settings.
 * Created by Joshua on 5/31/2020.
 */
class SettingsViewModel @ViewModelInject constructor(private val apodRepository: ApodRepository): ViewModel() {
    fun clearData() {
        viewModelScope.launch {
            apodRepository.clearResources()
        }
    }
}