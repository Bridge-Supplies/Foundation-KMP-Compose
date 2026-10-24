package bridge.supplies.foundation.ui

import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

object GoldenTestHelper {
    private val goldenDir: File by lazy {
        val currentDir = File("").absoluteFile
        val candidates = listOf(
            File(currentDir, "src/desktopTest/resources/golden"),
            File(currentDir, "foundation-ui/src/desktopTest/resources/golden"),
            File(currentDir, "resources/golden")
        )
        val dir = candidates.firstOrNull { it.parentFile?.exists() == true } ?: candidates.first()
        dir.apply { mkdirs() }
    }

    val isRecordMode: Boolean
        get() = System.getProperty("recordGolden") == "true" || System.getenv("RECORD_GOLDEN") == "true"

    fun assertGolden(image: BufferedImage, goldenName: String, maxDiffFraction: Double = 0.002) {
        val file = File(goldenDir, "$goldenName.png")
        if (isRecordMode) {
            file.parentFile.mkdirs()
            ImageIO.write(image, "PNG", file)
            println("Recorded golden: ${file.absolutePath}")
            return
        }

        if (!file.exists()) {
            throw AssertionError("Golden file not found at ${file.absolutePath}. Run tests with -DrecordGolden=true to record.")
        }

        val expected = ImageIO.read(file)
        if (expected.width != image.width || expected.height != image.height) {
            throw AssertionError("Golden dimension mismatch for $goldenName: expected ${expected.width}x${expected.height}, got ${image.width}x${image.height}")
        }

        var diffPixels = 0
        val totalPixels = expected.width * expected.height
        for (y in 0 until expected.height) {
            for (x in 0 until expected.width) {
                if (expected.getRGB(x, y) != image.getRGB(x, y)) {
                    diffPixels++
                }
            }
        }

        val diffFraction = diffPixels.toDouble() / totalPixels
        if (diffFraction > maxDiffFraction) {
            throw AssertionError(
                "Golden pixel mismatch for $goldenName: $diffPixels / $totalPixels " +
                    "(${String.format("%.2f", diffFraction * 100)}%) differ (max allowed: ${String.format("%.2f", maxDiffFraction * 100)}%)"
            )
        }
    }
}
