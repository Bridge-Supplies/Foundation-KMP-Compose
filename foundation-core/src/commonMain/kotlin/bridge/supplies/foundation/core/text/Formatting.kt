package bridge.supplies.foundation.core.text

// FORMATTING

public fun Int.formatDecimalSeparator(): String {
    return toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
}
