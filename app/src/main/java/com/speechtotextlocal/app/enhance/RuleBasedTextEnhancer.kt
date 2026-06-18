package com.speechtotextlocal.app.enhance

/**
 * Offline, rule-based transcript clean-up.
 *
 * Removes filler words/phrases and stutters, collapses immediate repetition,
 * normalises whitespace and punctuation, and fixes capitalisation — with no
 * network connection. Mirrors the iOS/macOS RuleBasedTextEnhancer.
 */
class RuleBasedTextEnhancer(
    private val fillerWords: List<String> = listOf(
        "um", "umm", "uh", "uhh", "uhm", "er", "err",
        "ah", "ahh", "hmm", "mm", "mmm", "mhm", "eh",
    ),
    private val fillerPhrases: List<String> = listOf("you know", "i mean"),
) : TextEnhancer {

    override fun enhance(raw: String): String {
        if (raw.isBlank()) return ""
        var text = raw
        text = removeFillerPhrases(text)
        text = removeFillerWords(text)
        text = collapseRepeatedWords(text)
        text = cleanUpStrayPunctuation(text)
        text = normaliseWhitespace(text)
        text = capitaliseSentences(text)
        text = capitaliseStandaloneI(text)
        return text.trim()
    }

    private fun removeFillerPhrases(text: String): String {
        if (fillerPhrases.isEmpty()) return text
        val alternation = fillerPhrases.joinToString("|")
        return text.replace(Regex("\\b(?:$alternation)\\b,?", RegexOption.IGNORE_CASE), "")
    }

    private fun removeFillerWords(text: String): String {
        if (fillerWords.isEmpty()) return text
        val alternation = fillerWords.joinToString("|")
        return text.replace(Regex("\\b(?:$alternation)\\b,?", RegexOption.IGNORE_CASE), "")
    }

    private fun collapseRepeatedWords(text: String): String =
        text.replace(Regex("\\b(\\w+)(\\s+\\1\\b)+", RegexOption.IGNORE_CASE)) { it.groupValues[1] }

    private fun cleanUpStrayPunctuation(text: String): String {
        var t = text
        t = t.replace(Regex("\\s+([,.!?;:])"), "$1")       // no space before punctuation
        t = t.replace(Regex("([,;:])(?=\\S)"), "$1 ")      // ensure a space after , ; :
        t = t.replace(Regex("(?:\\s*,\\s*){2,}"), ", ")    // collapse repeated commas
        t = t.replace(Regex("(^|[.!?]\\s*),\\s*"), "$1")   // drop comma at a sentence start
        return t
    }

    private fun normaliseWhitespace(text: String): String {
        var t = text
        t = t.replace(Regex("[ \\t]+"), " ")
        t = t.replace(Regex(" *\\n *"), "\n")
        t = t.replace(Regex("\\n{3,}"), "\n\n")
        return t
    }

    private fun capitaliseSentences(text: String): String {
        val builder = StringBuilder(text.length)
        var capitaliseNext = true
        for (character in text) {
            if (capitaliseNext && character.isLetter()) {
                builder.append(character.uppercaseChar())
                capitaliseNext = false
            } else {
                builder.append(character)
            }
            if (character == '.' || character == '!' || character == '?') {
                capitaliseNext = true
            }
        }
        return builder.toString()
    }

    private fun capitaliseStandaloneI(text: String): String =
        text.replace(Regex("\\bi\\b", RegexOption.IGNORE_CASE), "I")
}
