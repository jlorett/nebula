package com.joshualorett.nebula.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshualorett.nebula.apod.ApodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch

/**
 * [ViewModel] of Settings.
 * Created by Joshua on 5/31/2020.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(private val apodRepository: ApodRepository) : ViewModel() {
    fun clearData() {
        viewModelScope.launch {
            apodRepository.clearCache()
        }
    }
}
