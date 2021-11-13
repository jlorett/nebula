package com.joshualorett.nebula.ui.today

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import javax.inject.Inject

/**
 * [ViewModel] show today's Astronomy Picture of the Day.
 * Created by Joshua on 1/11/2020.
 */
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val apodRepository: ApodRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val dateKey: String = "date"
    private var refresh: Boolean = false
    private val _navigateVideoLink: Channel<String?> = Channel()
    val navigateVideoLink: Flow<String?> = _navigateVideoLink.receiveAsFlow()
    private val _navigateFullPicture: Channel<Long> = Channel()
    val navigateFullPicture: Flow<Long> = _navigateFullPicture.receiveAsFlow()
    private val _showDatePicker: Channel<LocalDate> = Channel()
    val showDatePicker: Flow<LocalDate> = _showDatePicker.receiveAsFlow()
    private val date: MutableStateFlow<LocalDate?> = MutableStateFlow(
        savedStateHandle.get(dateKey) ?: LocalDate.now()
    )
    val apod: StateFlow<Resource<Apod, String>> = date
        .asStateFlow()
        .filter { date -> date != null }
        .map { date ->
            val apodDate = date ?: throw NullPointerException("Date can't be null.")
            if (refresh) {
                refresh = false
                apodRepository.getFreshApod(apodDate)
            } else {
                apodRepository.getApod(apodDate)
            }
        }
        .catch { throwable -> emit(Resource.Error("Couldn't fetch apod.")) }
        .stateIn(
            initialValue = Resource.Loading,
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000)
        )

    fun videoLinkClicked() {
        val resource = apod.value
        if (resource.successful()) {
            val apod = (resource as Resource.Success).data
            if (apod.mediaType == "video") {
                _navigateVideoLink.offer(apod.url)
            }
        }
    }

    fun onPhotoClicked() {
        val resource = apod.value
        if (resource.successful()) {
            val apod = (resource as Resource.Success).data
            _navigateFullPicture.offer(apod.id)
        }
    }

    fun onChooseDate() {
        _showDatePicker.offer(date.value ?: LocalDate.now())
    }

    fun updateDate(date: LocalDate) {
        this.date.value = null
        this.date.value = date
        savedStateHandle.set(dateKey, date)
    }

    fun refresh() {
        refresh = true
        updateDate(date.value ?: LocalDate.now())
    }
}
