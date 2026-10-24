package bridge.supplies.foundation.core.crypto

import korlibs.crypto.AES
import korlibs.crypto.Padding
import korlibs.crypto.SecureRandom
import korlibs.io.compression.compress
import korlibs.io.compression.deflate.GZIP
import korlibs.io.compression.uncompress
import korlibs.io.lang.UTF8
import korlibs.io.lang.toByteArray
import korlibs.io.lang.toString
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64

/** Encodes app data for sharing as text (QR codes), and decodes shared text back into data. */
public interface QrCodec {
    public fun <T> encode(value: T, serializer: KSerializer<T>, encrypt: Boolean = true): String
    
    /** Decodes v2 and legacy v1 payloads. Fails if [text] isn't a payload of this type (or wrong key). */
    public fun <T> decode(text: String, serializer: KSerializer<T>): Result<T>
}

/**
 * Foundation's share format.
 *
 * **v2** (written): `"BS2"` + `E` (encrypted) or `P` (plain) + base64url(payload), where payload is
 * `gzip(json)` or, when encrypted, `IV(16 random bytes) + AES-CBC/PKCS7(gzip(json))`.
 *
 * **v1** (read only, Foundation 1.x apps): hex(AES-CBC(gzip(json))) with the key as the IV, or plain JSON.
 *
 * [key] must be exactly 16 bytes.
 */
public class FoundationQrCodec(
    private val key: String,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : QrCodec {
    
    override fun <T> encode(value: T, serializer: KSerializer<T>, encrypt: Boolean): String {
        val compressed = GZIP.compress(json.encodeToString(serializer, value).toByteArray())
        val payload = if (encrypt) {
            val iv = SecureRandom.nextBytes(IV_SIZE)
            iv + AES.encryptAesCbc(compressed, key.toByteArray(), iv, Padding.PKCS7Padding)
        } else {
            compressed
        }
        return V2_PREFIX + (if (encrypt) ENCRYPTED else PLAIN) + Base64.UrlSafe.encode(payload)
    }
    
    override fun <T> decode(text: String, serializer: KSerializer<T>): Result<T> = runCatching {
        val jsonText = if (text.startsWith(V2_PREFIX)) {
            decodeV2(text)
        } else {
            decryptAndDecompressV1(text, key) ?: text // v1 encrypted, else v1 plain JSON
        }
        json.decodeFromString(serializer, jsonText)
    }
    
    private fun decodeV2(text: String): String {
        val mode = text[V2_PREFIX.length]
        val payload = Base64.UrlSafe.decode(text.substring(V2_PREFIX.length + 1))
        val compressed = when (mode) {
            ENCRYPTED -> {
                require(payload.size > IV_SIZE) { "Payload too short" }
                val iv = payload.copyOfRange(0, IV_SIZE)
                AES.decryptAesCbc(payload.copyOfRange(IV_SIZE, payload.size), key.toByteArray(), iv, Padding.PKCS7Padding)
            }
            PLAIN -> payload
            else -> error("Unknown v2 mode '$mode'")
        }
        return GZIP.uncompress(compressed).toString(UTF8)
    }
    
    private companion object {
        const val V2_PREFIX = "BS2"
        const val ENCRYPTED = 'E'
        const val PLAIN = 'P'
        const val IV_SIZE = 16
    }
}
