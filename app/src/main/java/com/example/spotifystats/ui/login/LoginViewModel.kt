package com.example.spotifystats.ui.login

import android.content.Intent
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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
}