package org.virtuslab.inkuire.kotlin.model

data class Match(
    val prettifiedSignature: String,
    val functionName: String,
    val packageLocation: String,
    val pageLocation: String
)

data class OutputFormat(
    val query: String,
    val matches: List<Match>
)
