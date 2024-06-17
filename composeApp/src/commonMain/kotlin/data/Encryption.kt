package data

import bridge.supplies.foundation.BuildConfig
import korlibs.crypto.AES
import korlibs.crypto.Padding
import korlibs.crypto.encoding.Hex
import korlibs.io.compression.compress
import korlibs.io.compression.deflate.GZIP
import korlibs.io.compression.uncompress
import korlibs.io.lang.UTF8
import korlibs.io.lang.toByteArray
import korlibs.io.lang.toString
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class SharedData(
    @SerialName("m") val message: String
)

fun serializeData(data: SharedData): String =
    Json.encodeToString(data)

fun deserializeData(json: String): SharedData? = try {
    Json.decodeFromString<SharedData>(json)
} catch (e: Exception) {
    null
}

fun SharedData.compressAndEncrypt(): String? {
    val key = BuildConfig.ENCRYPTION_KEY.toByteArray()
    
    val encrypted = try {
        val jsonBytes = serializeData(this).toByteArray()
        val compressedBytes = GZIP.compress(jsonBytes)
        val encryptedBytes = AES.encryptAesCbc(compressedBytes, key, key, Padding.PKCS7Padding)
        val encodedHex = Hex.encode(encryptedBytes)
        encodedHex
    } catch (e: Exception) {
        null
    }
    
    return encrypted
}

fun String.decryptAndUncompress(): SharedData? {
    val key = BuildConfig.ENCRYPTION_KEY.toByteArray()
    
    val decrypted = try {
        val decryptedBytes = AES.decryptAesCbc(Hex.decode(this), key, key, Padding.PKCS7Padding)
        val uncompressedBytes = GZIP.uncompress(decryptedBytes)
        val jsonString = uncompressedBytes.toString(UTF8)
        val data = deserializeData(jsonString)
        data
    } catch (e: Exception) {
        null
    }
    
    if (decrypted != null) {
        return decrypted
    } else {
        // attempt to parse from plaintext
        return try {
            deserializeData(this)
        } catch (e: Exception) {
            null
        }
    }
}