package com.example.spotifystats.ui.login

import android.content.Intent
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifystats.BuildConfig
import com.example.spotifystats.api.SpotifyAuthService
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.content.SharedPreferences
import androidx.core.content.edit

sealed class LoginState{
    object Idle : LoginState()
    object Loading : LoginState()
    data class Success (val authCode : String) : LoginState()
    data class Error(val message : String) : LoginState()
}

class LoginViewModel : ViewModel(){
    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    fun handleSpotifyLogin(resultCode : Int , intent: Intent?){
        _loginState.value = LoginState.Loading

        val response = AuthorizationClient.getResponse(resultCode, intent)

        when (response.type) {
            AuthorizationResponse.Type.CODE -> {
                val authCode = response.code
                println("VIEWMODEL: SPOTIFY LOGIN SUCCESS! Token: $authCode")
                _loginState.value = LoginState.Success(authCode)
            }
            AuthorizationResponse.Type.ERROR -> {
                println("VIEWMODEL: SPOTIFY LOGIN ERROR: ${response.error}")
                _loginState.value = LoginState.Error(response.error ?: "Unknown Error")
            }
            else -> {
                println("VIEWMODEL: SPOTIFY LOGIN CANCELLED")
                _loginState.value = LoginState.Idle
            }
        }
    }

    fun exchangeCodeForToken(
        authCode: String,
        sharedPreferences: SharedPreferences,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val retrofit = Retrofit.Builder()
                    .baseUrl("https://accounts.spotify.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()

                val service = retrofit.create(SpotifyAuthService::class.java)

                val authString = "${BuildConfig.SPOTIFY_CLIENT_ID}:${BuildConfig.SPOTIFY_CLIENT_SECRET}"
                val base64Auth = Base64.encodeToString(authString.toByteArray(), Base64.NO_WRAP)
                val headerMap = "Basic $base64Auth"

                val response = service.getAccessToken(
                    authorization = headerMap,
                    code = authCode,
                    redirectUri = "dakshstats://callback"
                )

                Log.d("SPOTIFY_AUTH", "SUCCESS! Access Token: ${response.accessToken}")
                Log.d("SPOTIFY_AUTH", "SUCCESS! Refresh Token: ${response.refreshToken}")

                sharedPreferences.edit {
                    putString("ACCESS_TOKEN", response.accessToken)
                        .putString("REFRESH_TOKEN", response.refreshToken)
                        .apply()
                }
                Log.d("SPOTIFY_AUTH", "TOKENS SAVED SUCCESSFULLY!")
                onSuccess()

            } catch (e: Exception) {
                Log.e("SPOTIFY_AUTH", "Network Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}