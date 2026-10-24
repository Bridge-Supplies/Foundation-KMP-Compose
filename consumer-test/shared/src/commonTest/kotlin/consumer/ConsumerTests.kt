package consumer

import bridge.supplies.foundation.core.crypto.FoundationQrCodec
import bridge.supplies.foundation.ui.theme.ColorTheme
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.getString
import kotlin.test.Test
import kotlin.test.assertEquals

class ConsumerTests {
    
    /** foundation-ui's Compose resources are packaged into a consumer build and load at runtime (incl. iOS). */
    @Test
    fun foundationUiStringsLoad() = runTest {
        assertEquals("Fire", getString(ColorTheme.RED.titleRes))
        assertEquals("Darkest", getString(bridge.supplies.foundation.ui.theme.DarkMode.AMOLED.titleRes))
    }
    
    @Serializable
    data class Payload(val name: String, val score: Int)
    
    @Test
    fun qrCodecRoundTripsAppTypes() {
        val codec = FoundationQrCodec("ConsumerTestKey!")
        val payload = Payload("consumer", 42)
        listOf(true, false).forEach { encrypt ->
            val text = codec.encode(payload, Payload.serializer(), encrypt)
            assertEquals(payload, codec.decode(text, Payload.serializer()).getOrThrow())
        }
    }
}
