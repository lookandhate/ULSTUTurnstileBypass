package com.lookandhate.ulstuturnstilebypass.utils

class CardInformation(sectors: MutableList<String>, byteData: MutableList<ByteArray>) {
    public var sectors = sectors
    public var byteData = byteData
        set(value) {
            sectors.clear()
            field = value
            field.forEach { it -> sectors.add(it.toHex()) }
        }

    fun updateSector() {
        byteData.forEach { it -> sectors.add(it.toHex()) }
    }

}
