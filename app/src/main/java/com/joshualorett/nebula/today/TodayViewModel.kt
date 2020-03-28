package com.joshualorett.nebula.today

import androidx.lifecycle.*
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.OneShotEvent
import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * [ViewModel] show today's Astronomy Picture of the Day.
 * Created by Joshua on 1/11/2020.
 */
class TodayViewModel(private val apodRepository: ApodRepository,
                     private val todaysDate: LocalDate): ViewModel() {
    private val _apod: MutableLiveData<Apod> = MutableLiveData()
    val apod: LiveData<Apod> = _apod

    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error

    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading: LiveData<Boolean> = _loading

    private val _navigateVideoLink: MutableLiveData<OneShotEvent<String?>> = MutableLiveData()
    val navigateVideoLink: LiveData<OneShotEvent<String?>> = _navigateVideoLink

    private val _navigateFullPicture: MutableLiveData<OneShotEvent<Long>> = MutableLiveData()
    val navigateFullPicture: LiveData<OneShotEvent<Long>> = _navigateFullPicture

    init {
        viewModelScope.launch {
            _loading.value = true
            when(val resource = apodRepository.getApod(todaysDate)) {
                is Resource.Success -> {
                    _apod.value = resource.data
                    _loading.value = false
                }
                is Resource.Error<String> -> {
                    _error.value = resource.data
                    _loading.value = false
                }
            }
        }
    }

    fun videoLinkClicked() {
        val currentApod = _apod.value
        if(currentApod?.mediaType == "video") {
            _navigateVideoLink.value = OneShotEvent(currentApod.url)
        }
    }

    fun onPhotoClicked() {
        val apod = _apod.value
        if(apod != null) {
            _navigateFullPicture.value = OneShotEvent(apod.id)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _loading.value = true
            when(val resource = apodRepository.getFreshApod(todaysDate)) {
                is Resource.Success -> {
                    _apod.value = resource.data
                    _loading.value = false
                }
                is Resource.Error<String> -> {
                    _error.value = resource.data
                    _loading.value = false
                }
            }
        }
    }

    class TodayViewModelFactory(private val apodRepository: ApodRepository,
                                private val todaysDate: LocalDate = LocalDate.now()): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TodayViewModel(apodRepository, todaysDate) as T
        }
    }
}