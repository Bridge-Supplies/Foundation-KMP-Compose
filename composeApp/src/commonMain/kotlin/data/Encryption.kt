package data

import bridge.supplies.foundation.BuildConfig
import korlibs.crypto.AES
import korlibs.crypto.Padding
import korlibs.crypto.encoding.Hex
import korlibs.io.compression.compress
import korlibs.io.compression.deflate.GZIP
import korlibs.io.compression.uncompress
import korlibs.io.lang.UTF8
import korlibs.io.lang.toString
import korlibs.io.lang.toByteArray

fun String.compressAndEncrypt(): String {
    val key = BuildConfig.ENCRYPTION_KEY.toByteArray()
    val compressedBytes = GZIP.compress(this.toByteArray())
    val encryptedBytes = AES.encryptAesCbc(compressedBytes, key, key, Padding.PKCS7Padding)
    return Hex.encode(encryptedBytes)
}

fun String.decryptAndUncompress(): String {
    val key = BuildConfig.ENCRYPTION_KEY.toByteArray()
    val decryptedBytes = AES.decryptAesCbc(Hex.decode(this), key, key, Padding.PKCS7Padding)
    val uncompressedBytes = GZIP.uncompress(decryptedBytes)
    return uncompressedBytes.toString(UTF8)
}