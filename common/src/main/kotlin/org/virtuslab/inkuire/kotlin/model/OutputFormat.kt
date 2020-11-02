package org.virtuslab.inkuire.kotlin.model

data class Match(
    val prettifiedSignature: String,
    val functionName: String,
    val localization: String
)

data class OutputFormat(
    val query: String,
    val matches: List<Match>
)
