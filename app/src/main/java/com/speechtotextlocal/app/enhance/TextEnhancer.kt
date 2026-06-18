package com.speechtotextlocal.app.enhance

/**
 * Transforms a raw speech transcript into cleaner, more readable text.
 *
 * Implementations can range from simple rule-based clean-up to an on-device
 * language model. The UI depends only on this interface.
 */
interface TextEnhancer {
    fun enhance(raw: String): String
}
