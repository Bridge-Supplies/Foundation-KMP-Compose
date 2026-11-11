package bridge.supplies.foundation.core.storage

import bridge.supplies.foundation.core.config.FoundationConfig
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.NSUUID
import platform.Foundation.create
import platform.Foundation.stringWithContentsOfFile
import platform.Foundation.writeToFile

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class SettingsStorageIosTests {
    private val fileManager = NSFileManager.defaultManager
    private val root = "${NSTemporaryDirectory()}settings-test-${NSUUID().UUIDString}"
    private val support = "$root/Library/Application Support"
    private val documents = "$root/Documents"
    private val name = "settings.preferences_pb"

    @AfterTest
    fun cleanUp() {
        fileManager.removeItemAtPath(root, error = null)
    }

    private fun write(path: String, text: String) {
        val directory = path.substringBeforeLast('/')
        fileManager.createDirectoryAtPath(directory, true, null, null)
        assertTrue(
            NSString.create(string = text).writeToFile(path, true, NSUTF8StringEncoding, null)
        )
    }

    private fun read(path: String): String? =
        NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)

    @Test
    fun createsTheDirectoryWhenThereIsNoFile() {
        val path = settingsFileIn(support, documents, name)
        assertEquals("$support/$name", path)
        assertTrue(fileManager.fileExistsAtPath(support))
        assertFalse(fileManager.fileExistsAtPath(path))
    }

    @Test
    fun movesAFileFromDocumentsOnce() {
        write("$documents/$name", "old")
        val path = settingsFileIn(support, documents, name)
        assertEquals("$support/$name", path)
        assertEquals("old", read(path))
        assertFalse(fileManager.fileExistsAtPath("$documents/$name"))
    }

    @Test
    fun keepsTheNewFileWhenBothExist() {
        write("$documents/$name", "old")
        write("$support/$name", "new")
        val path = settingsFileIn(support, documents, name)
        assertEquals("new", read(path))
        assertEquals("old", read("$documents/$name")) // left alone
    }

    @Test
    fun defaultLocationIsApplicationSupport() {
        val config =
            FoundationConfig(
                appName = "Test",
                packageName = "test",
                version = "1",
                build = "1",
                encryptionKey = "BridgeDevKey0016",
            )
        assertTrue(
            settingsFilePath(config).endsWith("/Library/Application Support/$name"),
            settingsFilePath(config),
        )
    }
}
