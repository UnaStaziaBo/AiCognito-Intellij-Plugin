package com.github.unastaziabo.aicognitointellijplugin

import org.junit.Test
import org.junit.Assert.assertEquals

class VoiceIntentTest {

    private fun detectMode(text: String): String {
        val lower = text.lowercase()

        return when {
            lower.contains("new") -> "new"
            lower.contains("modify") || lower.contains("change") -> "modify"
            lower.contains("debug") || lower.contains("error") || lower.contains("wrong") -> "debug"
            else -> "explain"
        }
    }

    @Test
    fun `should detect new mode`() {
        val result = detectMode("create new function")
        assertEquals("new", result)
    }

    @Test
    fun `should detect modify mode`() {
        val result = detectMode("please change this logic")
        assertEquals("modify", result)
    }

    @Test
    fun `should detect debug mode`() {
        val result = detectMode("what is wrong here")
        assertEquals("debug", result)
    }

    @Test
    fun `should fallback to explain`() {
        val result = detectMode("explain what this does")
        assertEquals("explain", result)
    }
}