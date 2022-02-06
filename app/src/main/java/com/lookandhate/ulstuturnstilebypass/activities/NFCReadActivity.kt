package com.lookandhate.ulstuturnstilebypass.activities

import android.content.Intent
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lookandhate.ulstuturnstilebypass.BuildConfig
import com.lookandhate.ulstuturnstilebypass.R
import com.lookandhate.ulstuturnstilebypass.utils.AppMain
import com.lookandhate.ulstuturnstilebypass.ui.theme.ULSTUTurnstileBypassTheme
import com.lookandhate.ulstuturnstilebypass.utils.decodeHex
import com.lookandhate.ulstuturnstilebypass.utils.sum
import com.lookandhate.ulstuturnstilebypass.utils.toHex

class NFCReadActivity : ComponentActivity() {
    val Tag: String = "NFCReadActivity"
    private var nfcIntentTag: Tag? = null
    private val key = "FFFFFFFFFFFF".decodeHex()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ULSTUTurnstileBypassTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    CardUpdateScreen("Android")
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
        val nfcIntentTag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        this.nfcIntentTag = nfcIntentTag
        if (this.nfcIntentTag != null)
            updateCardInfo()

    }

    public fun updateCardInfo() {
        if (nfcIntentTag == null) {
            Log.e(Tag, "intent tag is null!")
            Toast.makeText(this, R.string.nfcread_toast_no_card_info, Toast.LENGTH_SHORT)
                .show()
            return
        }

        val mifareClassic = MifareClassic.get(nfcIntentTag)
        mifareClassic.connect()
        Log.d(Tag, mifareClassic.type.toString())
        var authStatus = mifareClassic.authenticateSectorWithKeyA(0, key)

        if (!authStatus) {
            Log.d(Tag, "Auth status is false")

            if (BuildConfig.DEBUG) {
                Toast.makeText(this, "Could not auth sector with key A", Toast.LENGTH_SHORT).show()
            }

            authStatus = mifareClassic.authenticateSectorWithKeyB(0, key)
            Log.d(Tag, "Auth status after keyB: $authStatus")
        }

        if (BuildConfig.DEBUG) {
            Toast.makeText(this, "Auth status $authStatus", Toast.LENGTH_SHORT).show()
        }
        if (!authStatus) {
            Log.e(Tag, "Auth status is $authStatus after authenticating with A and B key.")
            return
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

    public fun saveCardInfoInMemory() {
        val file = openFileOutput(AppMain.savedCardFileName, MODE_PRIVATE)

        file.write(AppMain.cardData.byteData.sum())
        file.close()
    }

    public fun readCardInfoFromMemory() {
        val file = openFileInput(AppMain.savedCardFileName)
        val byteArrayFromFile = file.readBytes()
        Log.d(
            Tag,
            "Does file contain same data as we read from card before?: ${
                byteArrayFromFile.contentEquals(
                    AppMain.cardData.byteData.sum()
                )
            }"
        )
        val newListOfByteArrays = mutableListOf<ByteArray>()
        var byteArrayFromFileIndexToReadFrom = 0

        for (i in 0 until 64) {
            val byteArrayChunk = ByteArray(16)
            for (j in 0 until 16) {
                byteArrayChunk[j] = byteArrayFromFile[byteArrayFromFileIndexToReadFrom]
                byteArrayFromFileIndexToReadFrom += 1
            }
            newListOfByteArrays.add(byteArrayChunk)
        }
        Log.d(
            Tag,
            "Does file contain same data as we read from card before?: ${newListOfByteArrays == AppMain.cardData.byteData}"
        )
        AppMain.cardData.byteData = newListOfByteArrays


    }

}

@Composable
fun CardUpdateScreen(name: String) {
    var cardSectors = remember { AppMain.cardData.sectors }

    Column(
        modifier = Modifier
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    )
    {
        Text(text = "Hello $name!")
        val context = LocalContext.current as NFCReadActivity
        cardSectors.forEach {
            Text(text = it)
        }

        Button(onClick = { context.updateCardInfo() }) {
            Text(text = "Update")
        }

        Button(onClick = {
            Log.d(
                context.Tag,
                "${
                    AppMain.cardData.byteData.sum().toString()
                }, ${AppMain.cardData.byteData.sum().size}"
            )
        }) {
            Text(text = "Log array state")
        }

        Button(onClick = { context.saveCardInfoInMemory() }) {
            Text(text = "Dump card info to internal storage")

        }
        Button(onClick = { context.readCardInfoFromMemory() }) {
            Text(text = "Read card info from memory")

        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview2() {
    ULSTUTurnstileBypassTheme {
        CardUpdateScreen("Android")
    }
}