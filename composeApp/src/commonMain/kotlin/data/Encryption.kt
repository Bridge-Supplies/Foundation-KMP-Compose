package data

import bridge.supplies.foundation.BuildConfig
import bridge.supplies.foundation.core.crypto.compressAndEncryptV1
import bridge.supplies.foundation.core.crypto.decryptAndDecompressV1
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class SharedData(
    @SerialName("m") val message: String
) {
    companion object {
        fun prepare(
            message: String?,
            encrypted: Boolean = true
        ): String? {
            if (message != null) {
                val sharedData = SharedData(message = message)
                return if (encrypted) sharedData.compressAndEncrypt() else serializeData(sharedData)
            } else {
                return null
            }
        }
    }
}

inline fun <reified T> serializeData(data: T): String =
    Json.encodeToString(data)

inline fun <reified T> deserializeData(json: String): T? = try {
    Json.decodeFromString<T>(json)
} catch (e: Exception) {
    null
}

fun SharedData.compressAndEncrypt(): String? =
    compressAndEncryptV1(serializeData(this), BuildConfig.ENCRYPTION_KEY)

fun String.decryptAndUncompress(): SharedData? {
    val decrypted = decryptAndDecompressV1(this, BuildConfig.ENCRYPTION_KEY)?.let { deserializeData<SharedData>(it) }
    if (decrypted != null) {
        return decrypted
    } else {
        // attempt to parse from plaintext
        return try {
            deserializeData<SharedData>(this)
        } catch (e: Exception) {
            null
        }
    }
}
