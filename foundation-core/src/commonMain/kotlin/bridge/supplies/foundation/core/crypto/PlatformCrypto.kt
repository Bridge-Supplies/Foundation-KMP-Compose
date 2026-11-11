package bridge.supplies.foundation.core.crypto

/*
 * The operating system's own crypto: javax.crypto on Android and desktop, CommonCrypto and
 * Security on iOS. Only standard algorithms the share format needs; no crypto code of our own.
 */

/**
 * AES-CBC with PKCS#7 padding. [key] is 16, 24 or 32 bytes (AES-128, -192 or -256), [iv] 16 bytes.
 * Throws on invalid arguments.
 */
internal expect fun aesCbcEncrypt(input: ByteArray, key: ByteArray, iv: ByteArray): ByteArray

/**
 * Reverses [aesCbcEncrypt]. Throws on invalid arguments, an empty [input] or one that isn't a whole
 * number of blocks, or bad padding (a wrong key or damaged data usually ends up here).
 */
internal expect fun aesCbcDecrypt(input: ByteArray, key: ByteArray, iv: ByteArray): ByteArray

/** SHA-256 digest (32 bytes) of [input]. */
internal expect fun sha256(input: ByteArray): ByteArray

/** [size] bytes from the system's cryptographically secure random generator. */
internal expect fun secureRandomBytes(size: Int): ByteArray

internal const val AES_BLOCK_SIZE: Int = 16

internal fun requireAesArguments(key: ByteArray, iv: ByteArray) {
    require(key.size == 16 || key.size == 24 || key.size == 32) {
        "AES key must be 16, 24 or 32 bytes (got ${key.size})"
    }
    require(iv.size == AES_BLOCK_SIZE) {
        "AES-CBC IV must be $AES_BLOCK_SIZE bytes (got ${iv.size})"
    }
}

/** Checks what every platform must reject the same way before decrypting. */
internal fun requireAesCipherText(input: ByteArray) {
    require(input.isNotEmpty() && input.size % AES_BLOCK_SIZE == 0) {
        "AES-CBC ciphertext must be a whole number of $AES_BLOCK_SIZE-byte blocks (got ${input.size})"
    }
}

/**
 * Removes PKCS#7 padding from decrypted [data], checking every padding byte (as javax.crypto does),
 * so damaged data fails the same way on every platform.
 */
internal fun removePkcs7Padding(data: ByteArray): ByteArray {
    require(data.isNotEmpty() && data.size % AES_BLOCK_SIZE == 0) { "Bad padding" }
    val padding = data.last().toInt() and 0xff
    require(padding in 1..AES_BLOCK_SIZE) { "Bad padding" }
    for (i in data.size - padding until data.size) {
        require(data[i].toInt() and 0xff == padding) { "Bad padding" }
    }
    return data.copyOf(data.size - padding)
}
