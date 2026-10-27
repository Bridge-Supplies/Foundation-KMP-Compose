package bridge.supplies.foundation.ui

import bridge.supplies.foundation.core.crypto.FoundationQrCodec
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import qrcode.QRCode
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertTrue

class QrCodeSizeTests {
    
    @Serializable
    private data class Wrapped(@SerialName("d") val data: String)
    
    private val codec = FoundationQrCodec("BridgeDevKey0016")
    
    @Test
    fun bytesPayloadsMakeSmallerQrCodesThanWrappedJson() {
        // A compact 150-byte app format (e.g. a game turn); random-looking bytes, as encrypted data is
        val bytes = ByteArray(150) { (it * 151 + 7).toByte() }
        val bytesVersion = QRCode.ofSquares().build(codec.encodeBytes(bytes)).typeNum
        val jsonVersion = QRCode.ofSquares().build(codec.encode(Wrapped(Base64.encode(bytes)), Wrapped.serializer())).typeNum
        assertTrue(bytesVersion < jsonVersion, "encodeBytes v$bytesVersion should be smaller than encode v$jsonVersion")
    }
}
