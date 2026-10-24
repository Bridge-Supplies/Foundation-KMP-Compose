package data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The demo's share payload: free text, encoded with Foundation's [bridge.supplies.foundation.core.crypto.QrCodec]. */
@Serializable
data class SharedData(
    @SerialName("m") val message: String
)
