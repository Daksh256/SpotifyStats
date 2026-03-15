package com.example.spotifystats.ui.login

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.spotifystats.ui.theme.SpotifyStatsTheme
@Composable
fun LoginScreen(){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().size(100.dp).background(MaterialTheme.colorScheme.background)
    ) {

        Button(
            onClick = {},
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(100.dp).padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Connect With Spotify",
                fontSize = 23.sp
            )
        }
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
        LoginScreen()
    }
}