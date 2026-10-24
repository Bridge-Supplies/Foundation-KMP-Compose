package bridge.supplies.foundation.core.platform

expect fun randomUuid(): String

expect fun browseWeb(url: String): Boolean

expect fun systemAppSettings()
