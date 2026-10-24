package bridge.supplies.foundation.demo

import bridge.supplies.foundation.BuildConfig
import bridge.supplies.foundation.core.crypto.FoundationQrCodec
import bridge.supplies.foundation.demo.data.SharedData
import kotlin.test.Test
import kotlin.test.assertEquals

class EncryptionTests {
    @Test
    fun encryptAndDecrypt() {
        // Uses this build's key (real keystore key, or the dev fallback)
        val codec = FoundationQrCodec(BuildConfig.ENCRYPTION_KEY)
        val testData = SharedData("test_string")
        
        val encryptedData = codec.encode(testData, SharedData.serializer())
        val decryptedData = codec.decode(encryptedData, SharedData.serializer()).getOrThrow()
        
        assertEquals(testData, decryptedData)
    }
}
