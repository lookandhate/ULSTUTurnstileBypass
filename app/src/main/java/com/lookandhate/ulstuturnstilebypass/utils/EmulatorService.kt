package com.lookandhate.ulstuturnstilebypass.utils

import android.nfc.cardemulation.HostApduService
import android.os.Bundle

class EmulatorService : HostApduService() {
    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        val byteArrayToSend: ByteArray = AppMain.cardData.byteData.sum()
        return byteArrayToSend
    }

    override fun onDeactivated(reason: Int) {
        //TODO Complete
    }
}