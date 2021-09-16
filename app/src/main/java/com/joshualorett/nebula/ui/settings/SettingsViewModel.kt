package com.joshualorett.nebula.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshualorett.nebula.apod.ApodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [ViewModel] of Settings.
 * Created by Joshua on 5/31/2020.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(private val apodRepository: ApodRepository) :
    ViewModel() {
    fun clearData() {
        viewModelScope.launch {
            apodRepository.clearCache()
        }
    }
}
