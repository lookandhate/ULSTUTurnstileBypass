package com.lookandhate.ulstuturnstilebypass.utils


object AppMain {
    var cardData: CardInformation =
        CardInformation(mutableListOf<String>(), mutableListOf<ByteArray>())

    val savedCardFileName: String = "card.dmp"
}

fun String.decodeHex(): ByteArray {
    check(length % 2 == 0) { "Must have an even length" }

    return chunked(2)
        .map { it.toInt(16).toByte() }
        .toByteArray()
}

fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }


fun MutableList<ByteArray>.sum(): ByteArray {
    var newArr: ByteArray = byteArrayOf()
    this.forEach { newArr += it }
    return newArr
}

