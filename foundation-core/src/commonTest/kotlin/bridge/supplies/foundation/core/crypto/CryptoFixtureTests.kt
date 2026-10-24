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
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Guards QR/share-code compatibility across crypto library upgrades.
 *
 * The fixtures below were produced with korlibs krypto 4.0.10 (before the swap to
 * `com.soywiz:korlibs-crypto`) using the dev key and must never change. They use the same
 * pipeline as `compressAndEncrypt()` (JSON -> GZIP -> AES-CBC/PKCS7 with key = IV -> hex),
 * but with an explicit key so the result doesn't depend on the build's `ENCRYPTION_KEY`.
 */
class CryptoFixtureTests {
    private val key = "BridgeDevKey0016".toByteArray()
    private val json = "{\"m\":\"Foundation crypto fixture\"}"
    private val compressedHex =
        "1f8b0800000000000000ab56ca55b25272cb2fcd4b492cc9cccf53482eaa2c28c95748cbac28292d4a55aa0500b84ffdaa21000000"
    private val cipherHex =
        "5a7aa703d39a787b28c51e305eeeb29950d45ba19f1d5065af5f44b717b02e36a6856df4449d01c027fb90fc0ca77a41ec1d196f9eed565716ea1d802021b6d2"
    private val rawCipherHex =
        "cbe6548fbd0e5bec89214aa22365565035bb60ef88c14d7d8101b3ffcc75ecc1fb14dbf1dc612ac0b50f7c989d2e53af"

    @Test
    fun decryptsFixtureIdentically() {
        val decrypted = AES.decryptAesCbc(Hex.decode(cipherHex), key, key, Padding.PKCS7Padding)
        assertEquals(compressedHex, Hex.encode(decrypted))
        assertEquals(json, GZIP.uncompress(decrypted).toString(UTF8))
    }

    @Test
    fun producesIdenticalCiphertext() {
        val compressed = GZIP.compress(json.toByteArray())
        assertEquals(compressedHex, Hex.encode(compressed))
        val cipher = AES.encryptAesCbc(compressed, key, key, Padding.PKCS7Padding)
        assertEquals(cipherHex, Hex.encode(cipher))
    }

    @Test
    fun rawAesMatchesFixture() {
        val cipher = AES.encryptAesCbc(json.toByteArray(), key, key, Padding.PKCS7Padding)
        assertEquals(rawCipherHex, Hex.encode(cipher))
        val plain = AES.decryptAesCbc(Hex.decode(rawCipherHex), key, key, Padding.PKCS7Padding)
        assertEquals(json, plain.toString(UTF8))
    }
}
