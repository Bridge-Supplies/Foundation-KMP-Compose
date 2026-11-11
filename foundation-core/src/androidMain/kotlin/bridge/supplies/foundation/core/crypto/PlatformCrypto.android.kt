package bridge.supplies.foundation.core.crypto

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

// PKCS5Padding in javax.crypto is PKCS#7 for 16-byte blocks.
private const val AES_CBC_PKCS7 = "AES/CBC/PKCS5Padding"

private val secureRandom = SecureRandom()

internal actual fun aesCbcEncrypt(input: ByteArray, key: ByteArray, iv: ByteArray): ByteArray =
    aesCbc(Cipher.ENCRYPT_MODE, input, key, iv)

internal actual fun aesCbcDecrypt(input: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
    requireAesCipherText(input)
    return aesCbc(Cipher.DECRYPT_MODE, input, key, iv)
}

private fun aesCbc(mode: Int, input: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
    requireAesArguments(key, iv)
    val cipher = Cipher.getInstance(AES_CBC_PKCS7)
    cipher.init(mode, SecretKeySpec(key, "AES"), IvParameterSpec(iv))
    return cipher.doFinal(input)
}

internal actual fun sha256(input: ByteArray): ByteArray =
    MessageDigest.getInstance("SHA-256").digest(input)

internal actual fun secureRandomBytes(size: Int): ByteArray =
    ByteArray(size).also { secureRandom.nextBytes(it) }
