package com.joshualorett.nebula.today

import androidx.lifecycle.*
import com.joshualorett.nebula.apod.Apod
import com.joshualorett.nebula.apod.ApodRepository
import com.joshualorett.nebula.shared.Resource
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

/**
 * [ViewModel] show today's Astronomy Picture of the Day.
 * Created by Joshua on 1/11/2020.
 */
class TodayViewModel(private val apodRepository: ApodRepository, private val todaysDate: Date): ViewModel() {
    private val _apod: MutableLiveData<Apod> = MutableLiveData()
    val apod: LiveData<Apod> = _apod

    private val _error: MutableLiveData<String> = MutableLiveData()
    val error: LiveData<String> = _error

    private val _loading: MutableLiveData<Boolean> = MutableLiveData()
    val loading: LiveData<Boolean> = _loading

    init {
        viewModelScope.launch {
            _loading.value = true
            apodRepository.getApod(todaysDate).collect { resource ->
                when(resource) {
                    is Resource.Success -> _apod.value = resource.data
                    is Resource.Error -> _error.value = resource.message ?: "Error fetching the picture of the day."
                    is Resource.Loading -> _loading.value = true
                }
            }
        }
    }

    class TodayViewModelFactory(private val apodRepository: ApodRepository, private val todaysDate: Date = Date(System.currentTimeMillis())): ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return TodayViewModel(apodRepository, todaysDate) as T
        }
    }
}