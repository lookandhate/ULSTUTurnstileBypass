package com.lookandhate.ulstuturnstilebypass.activities

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lookandhate.ulstuturnstilebypass.CardInformation
import com.lookandhate.ulstuturnstilebypass.activities.ui.theme.ULSTUTurnstileBypassTheme
import com.lookandhate.ulstuturnstilebypass.decodeHex
import com.lookandhate.ulstuturnstilebypass.toHex

class NFCReadActivity : ComponentActivity() {
    val Tag: String = "NFCReadActivity"
    var intentTag: Tag? = null
    var cardData: CardInformation = CardInformation(mutableListOf<String>())
    private val key = "FFFFFFFFFFFF".decodeHex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(
            Tag,
            "On create called, has feauture ${getPackageManager().hasSystemFeature("com.nxp.mifare")}"
        )

        setContent {
            ULSTUTurnstileBypassTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    Greeting2("Android")
                }
            }
        }
        Log.d(
            Tag,
            intent.action.toString()
        )
        handleIntent(intent)


    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
    }


    private fun handleIntent(intent: Intent) {
        Log.d(
            Tag,
            "handleIntent called"
        )
        val tagFromIntent: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        intentTag = tagFromIntent
        val mClassic = MifareClassic.get(intentTag)
        Log.d(
            Tag,
            "mclassic $mClassic"
        )

        mClassic.use { mifareClassic ->
            mifareClassic.connect()
            Log.d(Tag, "authenticating Sector With Key A")
            val authStatus = mifareClassic.authenticateSectorWithKeyA(0, key)
            Log.d(Tag, "Auth status $authStatus")

            Log.d(Tag, "Connecting to mifareClassic")
            Log.d(Tag, "isConnected ${mifareClassic.isConnected}")

            if (!mifareClassic.isConnected) {
                Toast.makeText(this, "MifareClassic is not connected!", Toast.LENGTH_SHORT).show()
                return
            }

            Log.d(
                Tag,
                "Block count: ${mifareClassic.blockCount}. Sector count ${mifareClassic.sectorCount}. Blocks in sector ${
                    mifareClassic.getBlockCountInSector(0)
                }"
            )

            for (i in 0 until mifareClassic.getBlockCountInSector(0)) {
                val data = mifareClassic.readBlock(i)
                Log.d(Tag, "Data in block $i is $data. Hex is ${data.toHex()}")
                cardData.sectors.add(data.toHex())
            }
            mifareClassic.close()
        }
    }

    public fun authMifare() {
        val mfc = MifareClassic.get(intentTag)
        mfc.connect()
        Log.d(
            Tag,
            mfc.type.toString()
        )
        var authStatus = mfc.authenticateSectorWithKeyA(0, key)
        if (!authStatus) {
            Log.d(
                Tag,
                "Auth status is false"
            )
            authStatus = mfc.authenticateSectorWithKeyB(0, key)
            Log.d(
                Tag,
                "Auth status after keyB: $authStatus"
            )
        }
        mfc.close()
    }

}

@Composable
fun Greeting2(name: String) {
    Column(modifier = Modifier.padding(20.dp)) {

        Text(text = "Hello $name!")
        val context = LocalContext.current as NFCReadActivity
        context.cardData.sectors.forEach {
            Text(text = it)
        }
        Button(onClick = { context.authMifare() }) {
            Text(text = "Update")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    ULSTUTurnstileBypassTheme {
        Greeting2("Android")
    }
}