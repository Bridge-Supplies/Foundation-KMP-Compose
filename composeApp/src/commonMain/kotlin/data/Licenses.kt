package data

import foundation.composeapp.generated.resources.Res
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@Serializable
data class License(
    val moduleName: String = "",
    val moduleUrl: String = "",
    val moduleVersion: String = "",
    val moduleLicense: String = "",
    val moduleLicenseUrl: String = ""
)

@Serializable
data class LicenseList(
    @SerialName("dependencies") val licenses: List<License> = emptyList()
)

data class LicenseMap(
    val mapping: Map<String, List<License>> = emptyMap()
)

@OptIn(ExperimentalResourceApi::class)
suspend fun loadLicenseFile(): LicenseList {
    val bytes = Res.readBytes("files/json/licenses.json")
    return Json.decodeFromString(bytes.decodeToString())
}