package bridge.supplies.foundation.core.crypto

import korlibs.crypto.AES
import korlibs.crypto.Padding
import korlibs.encoding.Hex
import korlibs.io.compression.compress
import korlibs.io.compression.deflate.GZIP
import korlibs.io.compression.uncompress
import korlibs.io.lang.UTF8
import korlibs.io.lang.toByteArray
import korlibs.io.lang.toString

/**
 * Legacy (v1) share format: hex( AES-CBC/PKCS7( gzip(text) ) ) with the key also used as the IV.
 * [key] must be exactly 16 bytes. Returns null on failure.
 */
public fun compressAndEncryptV1(text: String, key: String): String? = try {
    val keyBytes = key.toByteArray()
    val compressedBytes = GZIP.compress(text.toByteArray())
    Hex.encode(AES.encryptAesCbc(compressedBytes, keyBytes, keyBytes, Padding.PKCS7Padding))
} catch (e: Exception) {
    null
}

/** Reverses [compressAndEncryptV1]. Returns null if [hex] isn't a valid v1 payload for [key]. */
public fun decryptAndDecompressV1(hex: String, key: String): String? = try {
    val keyBytes = key.toByteArray()
    val decryptedBytes = AES.decryptAesCbc(Hex.decode(hex), keyBytes, keyBytes, Padding.PKCS7Padding)
    GZIP.uncompress(decryptedBytes).toString(UTF8)
} catch (e: Exception) {
    null
}
