package dubrowgn.wattz

data class PduConfig(
    val iconMetric: String,
    val bodyEntries: Set<String>,
)

fun parsePduList(s: String): List<PduConfig> {
    if (s.isBlank()) return listOf(PduConfig("W", emptySet()))
    return s.split(";").mapNotNull { part ->
        val idx = part.indexOf('|')
        if (idx < 0) return@mapNotNull null
        val icon = part.substring(0, idx).ifBlank { "W" }
        val bodyStr = part.substring(idx + 1)
        val body = if (bodyStr.isBlank()) emptySet() else bodyStr.split(",").toSet()
        PduConfig(icon, body)
    }.ifEmpty { listOf(PduConfig("W", emptySet())) }
}

fun serializePduList(pdus: List<PduConfig>): String =
    pdus.joinToString(";") { "${it.iconMetric}|${it.bodyEntries.joinToString(",")}" }
