package com.example.spotifystats.ui.login


import android.app.Activity
import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.spotifystats.BuildConfig
import com.example.spotifystats.R
import com.example.spotifystats.ui.theme.SpotifyStatsTheme
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse


@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = viewModel()
){
    val context = LocalContext.current

    val loginState by viewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {

            val authCode = (loginState as LoginState.Success).authCode

            val sharedPreferences =
                context.getSharedPreferences("SpotifyStatsPrefs", Context.MODE_PRIVATE)

            sharedPreferences.edit().putString("AUTH_CODE", authCode).apply()

            viewModel.exchangeCodeForToken(authCode, sharedPreferences)

            navController.navigate("HomeScreen") {
                popUpTo("LoginScreen") { inclusive = true }
            }
        }
    }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            viewModel.handleSpotifyLogin(result.resultCode, result.data)
        }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .size(100.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {

        Spacer(Modifier.weight(1f))

        Text(
            text = "Welcome!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Connect to see your top tracks, artists, and listening history.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                val request = AuthorizationRequest.Builder(
                    BuildConfig.SPOTIFY_CLIENT_ID,
                    AuthorizationResponse.Type.CODE,
                    "dakshstats://callback"
                ).setScopes(arrayOf("user-top-read", "user-read-recently-played")).build()

                val intent = AuthorizationClient.createLoginActivityIntent(context as Activity, request)
                launcher.launch(intent)
            },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Image(
                painter = painterResource(R.drawable.spotify_music_download_playlist_streaming_media_black_and_white_812877a098e0e84df9c8e20c435e581b),
                contentDescription = null
            )

            Text(
                text = "Connect With Spotify",
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.weight(0.5f))
    }
}



@Preview(
    //name = "Dark Mode",
    showBackground = true,
    //uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun LoginScreenPreview() {
    SpotifyStatsTheme() {
        LoginScreen(
            navController = androidx.navigation.compose.rememberNavController()
        )
    }
}