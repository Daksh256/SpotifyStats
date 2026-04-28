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
import com.example.spotifystats.api.SpotifyApiService
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
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

                val response = service.fetchNewAccessToken(
                    authorization = headerMap,
                    grantType = "authorization_code",
                    code = authCode,
                    redirectUri = "dakshstats://callback"
                )

                if (response.isSuccessful && response.body() != null) {
                    val tokens = response.body()!!
                    Log.d("SPOTIFY_AUTH", "SUCCESS! Access Token: ${tokens.accessToken}")
                    Log.d("SPOTIFY_AUTH", "SUCCESS! Refresh Token: ${tokens.refreshToken}")

                    sharedPreferences.edit {
                        putString("ACCESS_TOKEN", tokens.accessToken)
                        putString("REFRESH_TOKEN", tokens.refreshToken)
                        apply()
                    }
                    Log.d("SPOTIFY_AUTH", "TOKENS SAVED SUCCESSFULLY!")
                    saveUserToSupabase(tokens.accessToken, tokens.refreshToken ?: "")
                    onSuccess()
                } else {
                    Log.e("SPOTIFY_AUTH", "Server rejected login: ${response.errorBody()?.string()}")
                }

            } catch (e: Exception) {
                Log.e("SPOTIFY_AUTH", "Network Error: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}

private suspend fun saveUserToSupabase(accessToken: String, refreshToken: String) {
    try {
        val supabase = createSupabaseClient(
            supabaseUrl = BuildConfig.SUPABASE_URL,
            supabaseKey = BuildConfig.SUPABASE_ANON_KEY
        ) {
            install(Postgrest)
        }

        val spotifyRetrofit = Retrofit.Builder()
            .baseUrl("https://api.spotify.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val spotifyService = spotifyRetrofit.create(SpotifyApiService::class.java)
        val userResponse = spotifyService.getCurrentUser("Bearer $accessToken")

        if (userResponse.isSuccessful && userResponse.body() != null) {
            val userId = userResponse.body()!!.id

            supabase.from("users").upsert(
                mapOf(
                    "user_id" to userId,
                    "refresh_token" to refreshToken
                )
            )
            Log.d("SUPABASE", "User $userId saved to Supabase!")
        }
    } catch (e: Exception) {
        Log.e("SUPABASE", "Failed to save user: ${e.message}")
    }
}