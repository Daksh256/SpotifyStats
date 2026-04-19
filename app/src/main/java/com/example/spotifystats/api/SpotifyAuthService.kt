package com.example.spotifystats.api



import com.example.spotifystats.data.TokenResponse
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

interface SpotifyAuthService {

    @FormUrlEncoded
    @POST("api/token")
    suspend fun fetchNewAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String
    ): Response<TokenResponse>

    @FormUrlEncoded
    @POST("api/token")
    suspend fun refreshAccessToken(
        @Header("Authorization") authorization: String,
        @Field("grant_type") grantType: String,
        @Field("refresh_token") refreshToken: String
    ): Response<TokenResponse>
}