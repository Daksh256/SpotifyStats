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
    private val _recapMonth = MutableStateFlow(currentMonthName())
    val recapMonth = _recapMonth.asStateFlow()

    private val _recapYear = MutableStateFlow(currentYear())
    val recapYear = _recapYear.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _totalMinutes = MutableStateFlow(0)
    val totalMinutes = _totalMinutes.asStateFlow()

    private val _totalStreams = MutableStateFlow(0)
    val totalStreams = _totalStreams.asStateFlow()

    private val _totalHours = MutableStateFlow(0f)
    val totalHours = _totalHours.asStateFlow()

    private val _topDay = MutableStateFlow("--")
    val topDay = _topDay.asStateFlow()

    private val _totalArtists = MutableStateFlow(0)
    val totalArtists = _totalArtists.asStateFlow()


    private fun currentMonthName(): String {
        val cal = java.util.Calendar.getInstance()
        return java.text.SimpleDateFormat("MMMM", java.util.Locale.getDefault()).format(cal.time).uppercase()
    }
    private fun currentMonth(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
    }

    private fun currentYear(): Int {
        return java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
    }


    fun fetchMinutesThisMonth(userId: String) {
        viewModelScope.launch {
            try {
                val url = "${BuildConfig.SUPABASE_URL}/rest/v1/streams" +
                        "?user_id=eq.$userId" +
                        "&select=duration_ms,played_at"

                val retrofit = Retrofit.Builder()
                    .baseUrl(BuildConfig.SUPABASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(SupabaseApiService::class.java)

                val rows = retrofit.getStreams(
                    url = url,
                    apiKey = BuildConfig.SUPABASE_ANON_KEY,
                    auth = "Bearer ${BuildConfig.SUPABASE_ANON_KEY}"
                )

                Log.d("SUPABASE", "Total rows fetched: ${rows.size}")

                val now = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                val currentMonth = now.get(java.util.Calendar.MONTH) + 1
                val currentYear = now.get(java.util.Calendar.YEAR)

                Log.d("SUPABASE", "Filtering for month=$currentMonth year=$currentYear")

                val filtered = rows.filter { row ->
                    try {
                        val cleaned = row.played_at
                            .replace("Z", "+00:00")
                            .take(19)
                        val sdf = java.text.SimpleDateFormat(
                            "yyyy-MM-dd'T'HH:mm:ss",
                            java.util.Locale.getDefault()
                        )
                        val range = "7d"

                        val result: Map<String, Int> = when (range) {
                            "7d" -> {
                                val days = (6 downTo 0).map { daysAgo ->
                                    val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                                    cal.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
                                    val dayKey = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                                        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                                        .format(cal.time)
                                    val dateKey = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                                        .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                                        .format(cal.time)
                                    dayKey to dateKey
                                }
                                days.associate { (dayKey, dateKey) ->
                                    val mins = rows.filter { row ->
                                        row.played_at.take(10) == dateKey
                                    }.sumOf { it.duration_ms ?: 0L }
                                    dayKey to (mins / 60000).toInt()
                                }
                            }
                            "30d" -> {
                                val weeks = listOf("W1", "W2", "W3", "W4")
                                val now = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                                val currentMonth = now.get(java.util.Calendar.MONTH) + 1
                                val currentYear = now.get(java.util.Calendar.YEAR)
                                weeks.associateWith { week ->
                                    val weekNum = week.removePrefix("W").toInt()
                                    val mins = rows.filter { row ->
                                        try {
                                            val date = sdf.parse(row.played_at.take(19)) ?: return@filter false
                                            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                                            cal.time = date
                                            val rowMonth = cal.get(java.util.Calendar.MONTH) + 1
                                            val rowYear = cal.get(java.util.Calendar.YEAR)
                                            val dayOfMonth = cal.get(java.util.Calendar.DAY_OF_MONTH)
                                            val rowWeek = ((dayOfMonth - 1) / 7) + 1
                                            rowMonth == currentMonth && rowYear == currentYear && rowWeek == weekNum
                                        } catch (e: Exception) { false }
                                    }.sumOf { it.duration_ms ?: 0L }
                                    (mins / 60000).toInt()
                                }
                            }
                            "dow" -> {
                                val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                                dayNames.associateWith { dayName ->
                                    val mins = rows.filter { row ->
                                        try {
                                            val date = sdf.parse(row.played_at.take(19)) ?: return@filter false
                                            val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                                            cal.time = date
                                            val rowDay = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
                                                .apply { timeZone = java.util.TimeZone.getTimeZone("UTC") }
                                                .format(cal.time)
                                            rowDay == dayName
                                        } catch (e: Exception) { false }
                                    }.sumOf { it.duration_ms ?: 0L }
                                    (mins / 60000).toInt()
                                }
                            }
                            else -> emptyMap()
                        }
                        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                        val date = sdf.parse(cleaned) ?: return@filter false

                        val topDay = result.maxByOrNull { it.value }?.key ?: "--"

                        val cal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                        cal.time = date

                        val rowMonth = cal.get(java.util.Calendar.MONTH) + 1
                        val rowYear = cal.get(java.util.Calendar.YEAR)

                        Log.d("SUPABASE", "Row: ${row.played_at} → month=$rowMonth year=$rowYear")

                        rowMonth == currentMonth && rowYear == currentYear
                    } catch (e: Exception) {
                        Log.e("SUPABASE", "Date parse failed for: ${row.played_at}")
                        false
                    }
                }

                Log.d("SUPABASE", "Filtered rows this month: ${filtered.size}")

                val totalMs = filtered.sumOf { it.duration_ms ?: 0L }
                _totalMinutes.value = (totalMs / 60000).toInt()

                _totalStreams.value = filtered.size

                Log.d("SUPABASE", "Minutes this month: ${_totalMinutes.value}")

            } catch (e: Exception) {
                Log.e("SUPABASE", "Failed to fetch minutes: ${e.message}")
            }
        }
    }
}
