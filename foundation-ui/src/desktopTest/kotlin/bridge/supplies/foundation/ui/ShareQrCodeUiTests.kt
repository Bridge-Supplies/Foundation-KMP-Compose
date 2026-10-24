package bridge.supplies.foundation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import bridge.supplies.foundation.ui.qr.ShareQrCode
import bridge.supplies.foundation.ui.theme.FoundationTheme
import kotlinx.serialization.builtins.serializer
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ShareQrCodeUiTests {

    private lateinit var settings: InMemoryFoundationSettings

    @BeforeTest
    fun setUp() {
        stopKoin()
        settings = InMemoryFoundationSettings()
        startKoin {
            modules(testFoundationModule(settings))
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun shareQrCodeDisplaysCodeForValue() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(
                LocalWindowInfo provides FakeWindowInfo(containerSize = IntSize(800, 800))
            ) {
                FoundationTheme {
                    Box(Modifier.size(300.dp, 300.dp)) {
                        ShareQrCode(
                            value = "https://bridge.supplies/test-qr-code-share",
                            serializer = String.serializer(),
                            encrypt = false
                        )
                    }
                }
            }
        }

        // Wait for debounce and generation
        waitUntil(timeoutMillis = 5000) {
            runCatching {
                onNodeWithContentDescription("QR code").assertIsDisplayed()
                true
            }.getOrDefault(false)
        }

        onNodeWithContentDescription("QR code").assertIsDisplayed()
    }
}
