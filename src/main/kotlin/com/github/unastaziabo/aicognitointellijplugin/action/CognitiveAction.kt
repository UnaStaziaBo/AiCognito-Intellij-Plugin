package com.github.unastaziabo.aicognitointellijplugin.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import com.github.unastaziabo.aicognitointellijplugin.ai.AiService

class CognitiveAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {

        // Retrieve selected code from editor
        val editor = e.getData(CommonDataKeys.EDITOR)
        val selectedText = editor?.selectionModel?.selectedText

        if (selectedText.isNullOrBlank()) {
            Messages.showMessageDialog(
                "Please select some code first",
                "No Selection",
                null
            )
            return
        }

        // Ask user to choose analysis mode
        val mode = Messages.showInputDialog(
            "Choose mode: new / modify / debug",
            "Cognitive Mode",
            null
        )?.lowercase()

        // Build prompt based on selected mode
        val prompt = buildPrompt(mode, selectedText)

        val aiService = AiService()

        try {
            val result = aiService.askAI(prompt)

            Messages.showMessageDialog(
                result,
                "Cognitive Insights",
                null
            )

        } catch (ex: Exception) {
            Messages.showMessageDialog(
                "Error: ${ex.message}",
                "AI Error",
                null
            )
        }
    }

    // Generates structured prompt for the AI depending on the mode
    private fun buildPrompt(mode: String?, code: String): String {
        return when (mode) {

            "new" -> """
                The developer is writing new code.
                
                Provide:
                Next steps:
                - validation
                - edge cases
                - improvements
                - tests
                
                Keep it short and actionable.
                
                Code:
                $code
            """.trimIndent()

            "modify" -> """
                The developer is modifying existing code.
                
                Provide:
                Impact:
                - what this change affects
                
                Risk:
                - what might break
                
                Keep it concise.
                
                Code:
                $code
            """.trimIndent()

            "debug" -> """
                The developer is debugging code.
                
                Provide:
                Problem:
                - possible issue
                
                Fix:
                - how to solve it
                
                Keep it simple.
                
                Code:
                $code
            """.trimIndent()

            else -> """
                Explain this code briefly and clearly.
                
                Code:
                $code
            """.trimIndent()
        }
    }
}