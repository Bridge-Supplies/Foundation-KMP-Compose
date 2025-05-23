package bridge.supplies.foundation

import data.SharedData
import data.compressAndEncrypt
import data.decryptAndUncompress
import kotlin.test.Test
import kotlin.test.assertEquals

class EncryptionTests {
    @Test
    fun encryptAndDecrypt() {
        val testData = SharedData("test_string")
        
        val encryptedData = testData.compressAndEncrypt() ?: ""
        val decryptedData = encryptedData.decryptAndUncompress()
        
        assertEquals(testData, decryptedData)
    }
}