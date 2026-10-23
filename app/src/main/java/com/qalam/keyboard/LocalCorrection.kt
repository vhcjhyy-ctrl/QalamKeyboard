package com.qalam.keyboard

import java.text.Normalizer
import java.util.Locale

/** Lightweight, deterministic correction that never needs network access. */
object LocalCorrection {
    private val replacements = mapOf(
        // Common Arabic spelling and typing mistakes
        "انشاءالله" to "إن شاء الله", "ان شاءالله" to "إن شاء الله", "انشاء الله" to "إن شاء الله",
        "لاكن" to "لكن", "لاكنه" to "لكنه", "هاذا" to "هذا", "هاذه" to "هذه",
        "هاذي" to "هذه", "الذيي" to "الذي", "اللذي" to "الذي", "اللتي" to "التي",
        "انت" to "أنت", "انتا" to "أنت", "انتي" to "أنتِ", "انتم" to "أنتم",
        "انتما" to "أنتما", "انا" to "أنا", "احنا" to "نحن", "علشان" to "لأن",
        "عشان" to "لأن", "دايما" to "دائمًا", "دائما" to "دائمًا", "مفيش" to "لا يوجد",
        "مسوول" to "مسؤول", "مسئول" to "مسؤول", "مساله" to "مسألة", "مشكله" to "مشكلة",
        "الاملاء" to "الإملاء", "املاء" to "إملاء", "الان" to "الآن", "اولا" to "أولًا",
        "ثانيا" to "ثانيًا", "ايضا" to "أيضًا", "الى" to "إلى", "لان" to "لأن",
        "ان" to "أن", "اذا" to "إذا", "استخدام" to "استخدام", "شيئ" to "شيء",
        "شيءا" to "شيئًا", "جزا" to "جزء", "جزءا" to "جزءًا", "امل" to "أمل",
        "ابدا" to "أبدًا", "احيانا" to "أحيانًا", "اكثر" to "أكثر", "اقل" to "أقل",
        "اهم" to "أهم", "اخيرا" to "أخيرًا", "بداء" to "بدأ", "بدء" to "بدء",
        // English common typos
        "thier" to "their", "teh" to "the", "recieve" to "receive", "adn" to "and",
        "wich" to "which", "becuase" to "because", "definately" to "definitely",
        "seperate" to "separate", "occured" to "occurred", "adress" to "address",
        "langauge" to "language", "untill" to "until", " goverment" to " government"
    )

    private val preserveCaseKeys = setOf("i")

    fun correct(word: String): String {
        if (word.isBlank()) return word
        val trailing = word.takeLastWhile { it in ".,!?،؛:؟)]}" }
        val leading = word.takeWhile { it in "([{" }
        val core = word.drop(leading.length).dropLast(trailing.length)
        if (core.isEmpty()) return word
        val fixed = replacements[core.lowercase(Locale.ROOT)] ?: return word
        val adjusted = when {
            core.all { !it.isLetter() || !it.isUpperCase() } -> fixed
            core.firstOrNull()?.isUpperCase() == true -> fixed.replaceFirstChar { it.uppercase(Locale.ROOT) }
            else -> fixed
        }
        return leading + adjusted + trailing
    }

    /** Correct only tokens we know; unknown terms, names and code are left intact. */
    fun correctText(text: String): String = TOKEN_REGEX.replace(text) { match -> correct(match.value) }

    fun suggestions(prefix: String, limit: Int = 5): List<String> {
        if (prefix.length < 2) return emptyList()
        val needle = prefix.lowercase(Locale.ROOT)
        return replacements.keys.asSequence()
            .filter { it.startsWith(needle) && it != needle }
            .mapNotNull { replacements[it] }
            .distinct()
            .take(limit.coerceIn(1, 10))
            .toList()
    }

    private val TOKEN_REGEX = Regex("[^\\s]+")
}
