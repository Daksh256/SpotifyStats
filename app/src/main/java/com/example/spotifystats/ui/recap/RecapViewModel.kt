package com.example.spotifystats.ui.recap

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifystats.BuildConfig
import com.example.spotifystats.api.SupabaseApiService
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class RecapViewModel : ViewModel(){
    private val _recapMonth = mutableStateFlow(currentMonth)
    val recapMonth = _recapMonth.asStateFlow()

    private val _recapYear = mutableStateFlow(currentYear)
    val recapYear = _recapYear.asStateFlow()

    private val _isLoading = mutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _totalMinutes = MutableStateFlow(0)
    val totalMinutes = _totalMinutes.asStateFlow()

    private val _totalStreams = MutableStateFlow(0)
    val totalStreams = _totalStreams.asStateFlow()

    private fun currentMonth(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
    }

    private fun currentYear(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    }

}
