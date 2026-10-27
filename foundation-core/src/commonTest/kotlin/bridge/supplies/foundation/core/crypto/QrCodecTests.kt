package bridge.supplies.foundation.core.crypto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class QrCodecTests {
    
    @Serializable
    private data class Message(@SerialName("m") val message: String)
    
    private val codec = FoundationQrCodec("BridgeDevKey0016")
    
    @Test
    fun v2RoundTripsEncryptedAndPlain() {
        val value = Message("Hello Foundation v2")
        val encrypted = codec.encode(value, Message.serializer(), encrypt = true)
        val plain = codec.encode(value, Message.serializer(), encrypt = false)
        assertTrue(encrypted.startsWith("BS2E"))
        assertTrue(plain.startsWith("BS2P"))
        assertEquals(value, codec.decode(encrypted, Message.serializer()).getOrThrow())
        assertEquals(value, codec.decode(plain, Message.serializer()).getOrThrow())
    }
    
    @Test
    fun v2UsesARandomIv() {
        val value = Message("same input")
        assertNotEquals(codec.encode(value, Message.serializer()), codec.encode(value, Message.serializer()))
    }
    
    @Test
    fun decodesLegacyV1Payloads() {
        // Fixture produced by Foundation 1.x (key = IV), see CryptoFixtureTests
        val v1Encrypted =
            "5a7aa703d39a787b28c51e305eeeb29950d45ba19f1d5065af5f44b717b02e36a6856df4449d01c027fb90fc0ca77a41ec1d196f9eed565716ea1d802021b6d2"
        assertEquals(Message("Foundation crypto fixture"), codec.decode(v1Encrypted, Message.serializer()).getOrThrow())
        // v1 "unencrypted" sharing was plain JSON
        assertEquals(Message("plain"), codec.decode("{\"m\":\"plain\"}", Message.serializer()).getOrThrow())
    }
    
    @Test
    fun wrongKeyOrGarbageFails() {
        val encoded = codec.encode(Message("secret"), Message.serializer())
        assertTrue(FoundationQrCodec("SomeOtherKey0016").decode(encoded, Message.serializer()).isFailure)
        assertTrue(codec.decode("not a payload", Message.serializer()).isFailure)
        assertTrue(codec.decode("BS2Eabc", Message.serializer()).isFailure)
    }
    
    @Test
    fun bytesRoundTripEncryptedAndPlain() {
        val bytes = ByteArray(150) { (it * 37 + 11).toByte() }
        val encrypted = codec.encodeBytes(bytes)
        val plain = codec.encodeBytes(bytes, encrypt = false)
        assertTrue(encrypted.startsWith("BS2BE"))
        assertTrue(plain.startsWith("BS2BP"))
        assertContentEquals(bytes, codec.decodeBytes(encrypted).getOrThrow())
        assertContentEquals(bytes, codec.decodeBytes(plain).getOrThrow())
        assertContentEquals(ByteArray(0), codec.decodeBytes(codec.encodeBytes(ByteArray(0))).getOrThrow())
        assertNotEquals(encrypted, codec.encodeBytes(bytes)) // random IV
    }
    
    @Test
    fun bytesTextIsQrAlphanumeric() {
        val alphanumeric = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"
        val text = codec.encodeBytes(ByteArray(151) { it.toByte() })
        assertTrue(text.all { it in alphanumeric }, text)
    }
    
    @Test
    fun bytesAndJsonPayloadsDontCrossDecode() {
        val bytesText = codec.encodeBytes(byteArrayOf(1, 2, 3))
        val jsonText = codec.encode(Message("json"), Message.serializer())
        assertTrue(codec.decode(bytesText, Message.serializer()).isFailure)
        assertTrue(codec.decodeBytes(jsonText).isFailure)
        assertTrue(FoundationQrCodec("SomeOtherKey0016").decodeBytes(bytesText).isFailure)
        assertTrue(codec.decodeBytes("BS2BE").isFailure)
        assertTrue(codec.decodeBytes("BS2BPabc").isFailure) // lowercase isn't Base45
    }
    
    @Test
    fun base45MatchesRfc9285Examples() {
        assertEquals("BB8", Base45.encode("AB".encodeToByteArray()))
        assertEquals("%69 VD92EX0", Base45.encode("Hello!!".encodeToByteArray()))
        assertEquals("UJCLQE7W581", Base45.encode("base-45".encodeToByteArray()))
        assertEquals("ietf!", Base45.decode("QED8WEX0").decodeToString())
    }
}
