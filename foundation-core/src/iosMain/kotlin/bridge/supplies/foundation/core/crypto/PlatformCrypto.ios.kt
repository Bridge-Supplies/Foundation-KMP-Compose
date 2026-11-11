package bridge.supplies.foundation.core.crypto

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.value
import platform.CoreCrypto.CCCrypt
import platform.CoreCrypto.CCOptions
import platform.CoreCrypto.CC_SHA256
import platform.CoreCrypto.CC_SHA256_DIGEST_LENGTH
import platform.CoreCrypto.kCCAlgorithmAES128
import platform.CoreCrypto.kCCDecrypt
import platform.CoreCrypto.kCCEncrypt
import platform.CoreCrypto.kCCOptionPKCS7Padding
import platform.CoreCrypto.kCCSuccess
import platform.Security.SecRandomCopyBytes
import platform.Security.errSecSuccess
import platform.Security.kSecRandomDefault
import platform.posix.size_tVar

internal actual fun aesCbcEncrypt(input: ByteArray, key: ByteArray, iv: ByteArray): ByteArray =
    aesCbc(encrypt = true, input, key, iv)

/*
 * Decrypts without CommonCrypto's padding option and checks the padding in common code: CommonCrypto
 * accepts some damaged padding that javax.crypto rejects, and every platform should fail the same
 * way.
 */
internal actual fun aesCbcDecrypt(input: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
    requireAesCipherText(input)
    return removePkcs7Padding(aesCbc(encrypt = false, input, key, iv))
}

/**
 * One-shot CCCrypt in CBC mode (CommonCrypto's default; no kCCOptionECBMode). kCCAlgorithmAES128
 * (the same value as kCCAlgorithmAES) picks AES-128, -192 or -256 from the key length. Encryption
 * adds PKCS#7 padding; decryption leaves it in place for [removePkcs7Padding].
 */
@OptIn(ExperimentalForeignApi::class)
private fun aesCbc(encrypt: Boolean, input: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
    requireAesArguments(key, iv)
    // Encryption adds at most one block of padding; decryption never grows the data.
    val output = ByteArray(input.size + AES_BLOCK_SIZE)
    // addressOf(0) needs at least one element; the length passed below stays input.size.
    val source = if (input.isEmpty()) ByteArray(1) else input
    val options: CCOptions = if (encrypt) kCCOptionPKCS7Padding.convert() else 0u
    val written = memScoped {
        val moved = alloc<size_tVar>()
        val status = key.usePinned { keyPin ->
            iv.usePinned { ivPin ->
                source.usePinned { inPin ->
                    output.usePinned { outPin ->
                        CCCrypt(
                            op = (if (encrypt) kCCEncrypt else kCCDecrypt).convert(),
                            alg = kCCAlgorithmAES128.convert(),
                            options = options,
                            key = keyPin.addressOf(0),
                            keyLength = key.size.convert(),
                            iv = ivPin.addressOf(0),
                            dataIn = inPin.addressOf(0),
                            dataInLength = input.size.convert(),
                            dataOut = outPin.addressOf(0),
                            dataOutAvailable = output.size.convert(),
                            dataOutMoved = moved.ptr,
                        )
                    }
                }
            }
        }
        check(status == kCCSuccess) {
            "AES-CBC ${if (encrypt) "encryption" else "decryption"} failed (CCCryptorStatus $status)"
        }
        moved.value.toInt()
    }
    return output.copyOf(written)
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun sha256(input: ByteArray): ByteArray {
    val digest = ByteArray(CC_SHA256_DIGEST_LENGTH)
    val source = if (input.isEmpty()) ByteArray(1) else input
    source.usePinned { inPin ->
        digest.usePinned { outPin ->
            CC_SHA256(inPin.addressOf(0), input.size.convert(), outPin.addressOf(0).reinterpret())
        }
    }
    return digest
}

@OptIn(ExperimentalForeignApi::class)
internal actual fun secureRandomBytes(size: Int): ByteArray {
    val bytes = ByteArray(size)
    if (size == 0) return bytes
    val status = bytes.usePinned { pin ->
        SecRandomCopyBytes(kSecRandomDefault, size.convert(), pin.addressOf(0))
    }
    check(status == errSecSuccess) { "SecRandomCopyBytes failed (OSStatus $status)" }
    return bytes
}
