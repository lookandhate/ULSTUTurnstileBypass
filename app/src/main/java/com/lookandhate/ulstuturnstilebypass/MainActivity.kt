package com.lookandhate.ulstuturnstilebypass

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.lookandhate.ulstuturnstilebypass.activities.NFCReadActivity
import com.lookandhate.ulstuturnstilebypass.ui.theme.ULSTUTurnstileBypassTheme
import com.lookandhate.ulstuturnstilebypass.utils.AppMain
import com.lookandhate.ulstuturnstilebypass.utils.sum


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ULSTUTurnstileBypassTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting("Android")
                }
            }
        }
    }

}

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
    MainScreen()
}

@Composable
fun MainScreen() {
    val localContext = LocalContext.current
    var cardDataLength by remember {
        mutableStateOf(
            AppMain.cardData.byteData.sum().size
        )
    }
    Column() {
        Text(text = "Card info", modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(text = "Card byte array Length $cardDataLength")
        Button(onClick = {
            val intentToLaucnhCardReadActivity = Intent(localContext, NFCReadActivity::class.java)
            localContext.startActivity(intentToLaucnhCardReadActivity)
        }) {
            Text(text = "Launch Card adding activity")

        }

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ULSTUTurnstileBypassTheme {
        Greeting("Android")
    }
}