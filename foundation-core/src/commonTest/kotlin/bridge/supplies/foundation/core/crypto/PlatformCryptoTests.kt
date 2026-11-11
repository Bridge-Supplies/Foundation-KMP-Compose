package bridge.supplies.foundation.core.crypto

import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Pins the operating system's crypto (javax.crypto, CommonCrypto) to output made by
 * `com.soywiz:korlibs-crypto` 6.0.1, which Foundation used up to 2.1. The codes below were encoded
 * by that version: apps must keep reading them, and codes made now must read in apps still on it.
 * Never change these fixtures.
 */
class PlatformCryptoTests {

    @Serializable private data class Message(@SerialName("m") val message: String)

    private val keyText = "BridgeDevKey0016"
    private val key = keyText.encodeToByteArray()
    private val iv = ByteArray(16) { (it * 17 + 3).toByte() }
    private val codec = FoundationQrCodec(keyText)

    // AES-CBC/PKCS7 with [key] and [iv], from korlibs-crypto 6.0.1
    private val emptyCipherHex = "3598bc700fde461480cd9ebda4aaa553"
    private val blockCipherHex = "94c9fac07bfe0e45d4ec525393af986604bef611f1b09bb1d6e53f8ae24dec7e"
    private val longCipherHex =
        "719a6594d3eddc2937b068f26dbcc245eb49560d1a54bf5b6095ea8dfa0aa1b586b6232b210257fe32fc6ac4eeeb675329cd512d1e28c8eed8d58fc413e4a1d9e7910e882463bfd595c118bed1a0fe04644f12f9295e455d44ff53cf27da711accf4c78c8b7947820293f46f41922f7c9aacf375dd00d4cb3306bd111ba4a6467104d8715008243ce1dc4155bac34a64d9cace3f1d4307875880da55f76751b0c6a8d99dc4e2a7c2aef89bba1c3416ae070216a8de1ecb05d054c8ac9a292df33192003b438260f3f090ebc143d2ed494484b29b82e6c4062b475733e6fef1960cf110c3db6759303b28b769ab7ee9d05cbdfbea3b9bb7060821b12f764bb08321f8a31bb45f321b802c62ecf9488aa451c0631a8b89e5c20ad035a6a8a8afc12fac1df138671991ef680db243f668971b38d40e4c987fce532a77a7646c872203bd60e2e5e61787c94d01cecc35e0152506e0c84384dd56eda2feeb7ea8891bd09b73db73d79b020c4ad30ba059ba9807d18566a530afe2591ce3e74672afe9a6362c61e9d602cbfdfa4324712cf4a4e0b3a1ffe0ca45cc46b06becda511e3df9be7e6416eba05d0813ea70768be11a0cbec75920f316619f4529b7733fe28c9f85cb7ce72c2af544dcd4c341a62cb9898e203fd5019f7688c42f8f023680afe93542d0372aa41fbca123dd2dddcd1bb00f354eb7434da66fbcdc856844930b87fb10f38035d5156cf5b9b8aaa2c1a29811eab73456cbef5729573d995c12fe7769fdbcd396f9cd6c30d531f96c67f8406568c78779040d14d9075e6d74711124648346edf68ee180e68a03ba807a047d7f4578f0f400308f8e1837cb1ebde01440da2d05163f3fca96c318e43f03c9b1b09fc6fc12278e9a7cf6ff4b8254d4903e4efb57a7e625d2fd51ab805e79d84e8fb331a491350fa09c6a2140c76d32e01269a7aaf8860e0589b08589454e8f986484c2ce801298e29153de0afdb0459e4d170cebbd75e07e15aced45ae7abdc13cc731158322998df149f74147df135980bf0bebe33858011475acafdb51f308aef09c42a91b72a429e51d973402a55133e97dde4df294ed90d7a651bf4fb4e4df0c3158547b0ed6c778905d77a61d0295944f66b0d383df932f4f3031eaece05d9c7b6bc715e80ee32f2380c7b1b14a0a32187b702647f1bbee4ce0ea950fe3d2afbb3b4ee8da337a767fe822c6f87ea7564d54d14d33c31d30772b87677f0d5e64fdac2c001fffaf63d24b8745b1fdca229283814ff5e590714299bd84a4113e9e04d7bed41c5d2665b1d275476d915fcf3aa264b486246fc7377e9892fcdcf7916179236cabbb41d6b110e724cd1dc227436d946de4b05bc5ec58ca7d1811e06aca99fad8f3404a92f80a5c9985031d2f56459ff1bfd55a5d5e55cdfeb3"

    // Share codes encoded by FoundationQrCodec with korlibs-crypto 6.0.1 and the dev key
    private val v2Fixture =
        "BS2ELF4vEipTc3QQWnDG1PmH6tG8dSUoOfswjkTK-BFMbmLtcEwarVG2Ln4f0hN2MiTbXnw_CuFWH83K48Yy61Q2USdcCcbw1ZqEyv6IoCM0Bh0="
    private val v2LongFixture =
        "BS2EfDv8GviphsFMSeG0cmSbgYNNByp3s6RVFkBRfuWKdj1TLyaRpofXRig08IktCWypatV8GtqYCbtOTbliodW3nw=="
    private val bytesFixture =
        "BS2BEZL7KG8MT1GJDWEN\$Y9A7ED87CXE9N7\$OMDY8ZGDP/REMEYTIJGA7OHSFO-Y0KP2H*1U:N+4S-Z7\$*T97HV11MCHPG4GAQN5L"
    private val emptyBytesFixture = "BS2BE847U-P-%G5MDU9H-PU\$%EJCW 9JT7I .C+6B8/I\$5OH*6FF6"

    private fun pattern(size: Int, step: Int, offset: Int) =
        ByteArray(size) { (it * step + offset).toByte() }

    @Test
    fun aesMatchesFixturesForEmptyBlockSizedAndLongInput() {
        val cases =
            listOf(
                ByteArray(0) to emptyCipherHex,
                pattern(16, 1, 0) to blockCipherHex,
                pattern(1000, 31, 7) to longCipherHex,
            )
        for ((plain, cipherHex) in cases) {
            assertEquals(
                cipherHex,
                aesCbcEncrypt(plain, key, iv).toHexString(),
                "size ${plain.size}",
            )
            assertContentEquals(plain, aesCbcDecrypt(cipherHex.hexToByteArray(), key, iv))
        }
    }

    @Test
    fun sha256MatchesStandardVectors() {
        // FIPS 180-2 examples
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            sha256(ByteArray(0)).toHexString(),
        )
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            sha256("abc".encodeToByteArray()).toHexString(),
        )
        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
            sha256("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".encodeToByteArray())
                .toHexString(),
        )
    }

    @Test
    fun secureRandomBytesHaveTheRequestedSizeAndVary() {
        assertEquals(0, secureRandomBytes(0).size)
        val a = secureRandomBytes(32)
        val b = secureRandomBytes(32)
        assertEquals(32, a.size)
        assertFalse(a.contentEquals(b))
    }

    @Test
    fun invalidAesArgumentsAreRejected() {
        assertFailsWith<IllegalArgumentException> { aesCbcEncrypt(ByteArray(1), ByteArray(15), iv) }
        assertFailsWith<IllegalArgumentException> { aesCbcEncrypt(ByteArray(1), key, ByteArray(8)) }
    }

    @Test
    fun damagedAesInputThrowsInsteadOfCrashing() {
        val cipher = blockCipherHex.hexToByteArray()
        // Not a whole number of blocks
        assertTrue(runCatching { aesCbcDecrypt(cipher.copyOf(cipher.size - 1), key, iv) }.isFailure)
        assertTrue(runCatching { aesCbcDecrypt(ByteArray(0), key, iv) }.isFailure)
        // The last block (all padding here) damaged, so the padding is invalid
        val damaged = cipher.copyOf().also { it[it.size - 1] = (it[it.size - 1] + 1).toByte() }
        assertTrue(runCatching { aesCbcDecrypt(damaged, key, iv) }.isFailure)
    }

    @Test
    fun paddingIsCheckedStrictlyOnEveryPlatform() {
        val block =
            ByteArray(16) { 7 }
                .also {
                    it[15] = 2
                    it[14] = 2
                }
        assertContentEquals(ByteArray(14) { 7 }, removePkcs7Padding(block))
        // Last byte says 2 bytes of padding, but the one before it isn't 2
        val badPadding = block.copyOf().also { it[14] = 3 }
        assertFailsWith<IllegalArgumentException> { removePkcs7Padding(badPadding) }
        assertFailsWith<IllegalArgumentException> { removePkcs7Padding(ByteArray(16)) }
        assertFailsWith<IllegalArgumentException> { removePkcs7Padding(ByteArray(16) { 17 }) }
        // The same through each platform's decryption: a valid block encrypted without padding
        val unpadded = aesCbcEncrypt(ByteArray(0), key, iv) // one block of 16 x 0x10
        assertContentEquals(ByteArray(0), aesCbcDecrypt(unpadded, key, iv))
    }

    @Test
    fun decodesCodesFromKorlibsCrypto() {
        assertEquals(
            Message("Foundation v2 fixture"),
            codec.decode(v2Fixture, Message.serializer()).getOrThrow(),
        )
        assertEquals(
            Message("x".repeat(600) + "é😀"),
            codec.decode(v2LongFixture, Message.serializer()).getOrThrow(),
        )
        assertContentEquals(pattern(40, 1, 0), codec.decodeBytes(bytesFixture).getOrThrow())
        assertContentEquals(ByteArray(0), codec.decodeBytes(emptyBytesFixture).getOrThrow())
    }

    @Test
    fun newCodesKeepTheFormatOldAppsRead() {
        // Old apps decrypt payload[16..] with payload[0..16] as the IV; check that layout directly
        val text = codec.encode(Message("for an old app"), Message.serializer())
        val payload = Base64.UrlSafe.decode(text.removePrefix("BS2E"))
        val compressed =
            aesCbcDecrypt(payload.copyOfRange(16, payload.size), key, payload.copyOf(16))
        assertEquals(0x1f, compressed[0].toInt() and 0xff) // gzip magic
        assertEquals(0x8b, compressed[1].toInt() and 0xff)
        val long = Message("y".repeat(5000))
        assertEquals(
            long,
            codec
                .decode(codec.encode(long, Message.serializer()), Message.serializer())
                .getOrThrow(),
        )
    }

    @Test
    fun fixturesFailCleanlyWithAWrongKey() {
        val other = FoundationQrCodec("SomeOtherKey0016")
        assertTrue(other.decode(v2Fixture, Message.serializer()).isFailure)
        assertTrue(other.decodeBytes(bytesFixture).isFailure)
        assertTrue(other.decodeBytes(emptyBytesFixture).isFailure)
        val v1 = aesCbcEncrypt(ByteArray(16), key, key).toHexString()
        assertNull(decryptAndDecompressV1(v1, "SomeOtherKey0016"))
    }

    @Test
    fun damagedIvOrPaddingFailsCleanly() {
        val v2 = Base64.UrlSafe.decode(v2Fixture.removePrefix("BS2E"))
        val bytes = Base45.decode(bytesFixture.removePrefix("BS2BE"))
        for (index in listOf(0, 15, v2.size - 1)) {
            val text = "BS2E" + Base64.UrlSafe.encode(v2.flipped(index))
            assertTrue(codec.decode(text, Message.serializer()).isFailure, "v2 byte $index")
        }
        for (index in listOf(0, 15, bytes.size - 1)) {
            val text = "BS2BE" + Base45.encode(bytes.flipped(index))
            assertTrue(codec.decodeBytes(text).isFailure, "bytes byte $index")
        }
        // A payload of only an IV, or one cut short by part of a block
        assertTrue(codec.decodeBytes("BS2BE" + Base45.encode(bytes.copyOf(16))).isFailure)
        assertTrue(
            codec.decodeBytes("BS2BE" + Base45.encode(bytes.copyOf(bytes.size - 3))).isFailure
        )
    }

    @Test
    fun malformedV1TextIsNotAV1Payload() {
        assertNull(decryptAndDecompressV1("abc", keyText)) // odd length
        assertNull(decryptAndDecompressV1("zz00", keyText)) // not hex
        assertNull(decryptAndDecompressV1("", keyText))
    }

    private fun ByteArray.flipped(index: Int) =
        copyOf().also { it[index] = (it[index].toInt() xor 0x01).toByte() }
}
