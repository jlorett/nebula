package com.joshualorett.nebula.today

import androidx.lifecycle.*
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.OneShotEvent
import com.joshualorett.nebula.shared.Resource
import com.joshualorett.nebula.shared.data
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * [ViewModel] show today's Astronomy Picture of the Day.
 * Created by Joshua on 1/11/2020.
 */
class TodayViewModel(private val apodRepository: ApodRepository): ViewModel() {
    private val _apod: MutableLiveData<Resource<Apod, String>> = MutableLiveData()
    val apod: LiveData<Resource<Apod, String>> = _apod

    private val _navigateVideoLink: MutableLiveData<OneShotEvent<String?>> = MutableLiveData()
    val navigateVideoLink: LiveData<OneShotEvent<String?>> = _navigateVideoLink

    private val _navigateFullPicture: MutableLiveData<OneShotEvent<Long>> = MutableLiveData()
    val navigateFullPicture: LiveData<OneShotEvent<Long>> = _navigateFullPicture

    private val today = LocalDate.now()

    private val _date: MutableLiveData<LocalDate> = MutableLiveData()

    init {
        updateDate(_date.value ?: today)
    }

    fun currentDate(): LocalDate? {
        return _date.value
    }

    fun videoLinkClicked() {
        val currentApod = _apod.value?.data
        if(currentApod?.mediaType == "video") {
            _navigateVideoLink.value = OneShotEvent(currentApod.url)
        }
    }

    fun onPhotoClicked() {
        val apod = _apod.value?.data
        if(apod != null) {
            _navigateFullPicture.value = OneShotEvent(apod.id)
        }
    }

    fun updateDate(date: LocalDate) {
        _date.value = date
        viewModelScope.launch {
            _apod.value = Resource.Loading
            _apod.value = apodRepository.getApod(date)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _apod.value = Resource.Loading
            _apod.value = apodRepository.getFreshApod(_date.value ?: today)
        }
    }

    class TodayViewModelFactory(private val apodRepository: ApodRepository): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TodayViewModel(apodRepository) as T
        }
    }
}