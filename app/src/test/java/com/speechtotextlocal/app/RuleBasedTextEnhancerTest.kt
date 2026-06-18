package com.speechtotextlocal.app

import com.speechtotextlocal.app.enhance.RuleBasedTextEnhancer
import org.junit.Assert.assertEquals
import org.junit.Test

class RuleBasedTextEnhancerTest {

    private val enhancer = RuleBasedTextEnhancer()

    @Test
    fun removesFillerWordsAndCapitalises() {
        assertEquals("I think we should go", enhancer.enhance("um i think uh we should go"))
    }

    @Test
    fun collapsesImmediateRepetition() {
        assertEquals("I think the report is good", enhancer.enhance("i think the the report is good"))
    }

    @Test
    fun removesFillerPhrase() {
        assertEquals("We should, send it", enhancer.enhance("we should, you know, send it"))
    }

    @Test
    fun removesLeadingFillerPhrase() {
        assertEquals("We should go", enhancer.enhance("i mean we should go"))
    }

    @Test
    fun keepsMeaningfulWords() {
        assertEquals("I really like coffee", enhancer.enhance("i really like coffee"))
    }

    @Test
    fun emptyInputReturnsEmpty() {
        assertEquals("", enhancer.enhance("   "))
    }
}
