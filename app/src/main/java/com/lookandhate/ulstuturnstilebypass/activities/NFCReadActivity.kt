package com.lookandhate.ulstuturnstilebypass.activities

import android.app.PendingIntent
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareClassic
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lookandhate.ulstuturnstilebypass.utils.AppMain
import com.lookandhate.ulstuturnstilebypass.activities.ui.theme.ULSTUTurnstileBypassTheme
import com.lookandhate.ulstuturnstilebypass.utils.decodeHex
import com.lookandhate.ulstuturnstilebypass.utils.sum
import com.lookandhate.ulstuturnstilebypass.utils.toHex

class NFCReadActivity : ComponentActivity() {
    val Tag: String = "NFCReadActivity"
    private var intentTag: Tag? = null
    private val key = "FFFFFFFFFFFF".decodeHex()
    var pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

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
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        Log.d(
            Tag,
            "handleIntent called"
        )
        val tagFromIntent: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        intentTag = tagFromIntent
        if (intentTag != null)
            updateCardInfo()

    }

    public fun updateCardInfo() {
        if (intentTag == null) {
            Log.e(Tag, "intent tag is null!")
            return
        }
        val mifareClassic = MifareClassic.get(intentTag)
        mifareClassic.connect()
        Log.d(Tag, mifareClassic.type.toString())
        var authStatus = mifareClassic.authenticateSectorWithKeyA(0, key)

        if (!authStatus) {
            Log.d(Tag, "Auth status is false")

            authStatus = mifareClassic.authenticateSectorWithKeyB(0, key)
            Log.d(Tag, "Auth status after keyB: $authStatus")
        }

        Log.d(Tag, "Clearing cardData.sectors")
        AppMain.cardData.sectors.clear()

        val sectorsCount = mifareClassic.sectorCount

        for (sectorIndex in 0 until sectorsCount) {
            val firstBlockOfSector = mifareClassic.sectorToBlock(sectorIndex)
            val blockPerSectorCount = mifareClassic.getBlockCountInSector(sectorIndex)

            Log.d(
                Tag,
                "Sector number $sectorIndex, first block of sector $firstBlockOfSector, blocks in sector $blockPerSectorCount"
            )

            for (i in firstBlockOfSector until firstBlockOfSector + blockPerSectorCount) {
                mifareClassic.authenticateSectorWithKeyA(sectorIndex, key)
                val data = mifareClassic.readBlock(i)
                Log.d(Tag, "Data in block $i is $data. Hex is ${data.toHex()}")

                AppMain.cardData.sectors.add(data.toHex())
                AppMain.cardData.byteData.add(data)
            }
        }

        mifareClassic.close()
    }

}

@Composable
fun Greeting2(name: String) {
    Column(modifier = Modifier
        .padding(20.dp)
        .verticalScroll(rememberScrollState())) {

        Text(text = "Hello $name!")
        val context = LocalContext.current as NFCReadActivity
        AppMain.cardData.sectors.forEach {
            Text(text = it)
        }
        Button(onClick = { context.updateCardInfo() }) {
            Text(text = "Update")
        }
        Button(onClick = {
            Log.d(
                context.Tag,
                "${AppMain.cardData.byteData.sum().toString()}, ${AppMain.cardData.byteData.sum().size}"
            )
        }) {
            Text(text = "Log array state")
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