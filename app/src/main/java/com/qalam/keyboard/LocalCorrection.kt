package com.qalam.keyboard

import java.util.Locale

object LocalCorrection {
    private val replacements = mapOf(
        "انشاءالله" to "إن شاء الله",
        "ان شاءالله" to "إن شاء الله",
        "لاكن" to "لكن",
        "انت" to "أنت",
        "انتي" to "أنتِ",
        "thier" to "their",
        "teh" to "the",
        "recieve" to "receive",
        "adn" to "and"
    )
    fun correct(word: String): String {
        val trailing = word.takeLastWhile { it in ".,!?،؛:؟" }
        val core = word.dropLast(trailing.length)
        val fixed = replacements[core.lowercase(Locale.ROOT)] ?: return word
        val adjusted = if (core.firstOrNull()?.isUpperCase() == true)
            fixed.replaceFirstChar { it.uppercase() } else fixed
        return adjusted + trailing
    }
}
