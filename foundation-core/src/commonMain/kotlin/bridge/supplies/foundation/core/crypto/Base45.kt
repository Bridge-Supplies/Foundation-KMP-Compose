package bridge.supplies.foundation.core.crypto

/**
 * Base45 (RFC 9285): 2 bytes → 3 characters from the QR alphanumeric set, so QR codes can use the denser
 * alphanumeric mode instead of byte mode.
 */
internal object Base45 {
    private const val ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ $%*+-./:"

    fun encode(bytes: ByteArray): String = buildString((bytes.size / 2) * 3 + 2) {
        var i = 0
        while (i + 1 < bytes.size) {
            val v = ((bytes[i].toInt() and 0xFF) shl 8) or (bytes[i + 1].toInt() and 0xFF)
            append(ALPHABET[v % 45]).append(ALPHABET[v / 45 % 45]).append(ALPHABET[v / (45 * 45)])
            i += 2
        }
        if (i < bytes.size) {
            val v = bytes[i].toInt() and 0xFF
            append(ALPHABET[v % 45]).append(ALPHABET[v / 45])
        }
    }

    fun decode(text: String): ByteArray {
        require(text.length % 3 != 1) { "Invalid Base45 length ${text.length}" }
        val out = ByteArray((text.length / 3) * 2 + if (text.length % 3 == 2) 1 else 0)
        var o = 0
        var i = 0
        while (i + 2 < text.length) {
            val v = value(text[i]) + value(text[i + 1]) * 45 + value(text[i + 2]) * 45 * 45
            require(v <= 0xFFFF) { "Invalid Base45 group at $i" }
            out[o++] = (v shr 8).toByte()
            out[o++] = v.toByte()
            i += 3
        }
        if (i < text.length) {
            val v = value(text[i]) + value(text[i + 1]) * 45
            require(v <= 0xFF) { "Invalid Base45 group at $i" }
            out[o] = v.toByte()
        }
        return out
    }

    private fun value(c: Char): Int = ALPHABET.indexOf(c).also { require(it >= 0) { "Invalid Base45 character '$c'" } }
}
