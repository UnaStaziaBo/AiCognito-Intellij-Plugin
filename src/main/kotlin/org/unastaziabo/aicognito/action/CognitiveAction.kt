package org.unastaziabo.aicognito.action

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.ui.Messages
import org.unastaziabo.aicognito.ai.AiService

class CognitiveAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {

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

        val mode = Messages.showInputDialog(
            "Choose mode: new / modify / debug / explain",
            "AiCognito",
            null
        )?.lowercase()

        val prompt = buildPrompt(mode, selectedText)

        val aiService = AiService()

        try {
            val result = aiService.askAI(prompt)

            Messages.showMessageDialog(
                result,
                "AiCognito",
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

    private fun buildPrompt(mode: String?, code: String): String {

        return when (mode) {

            "debug" -> """
                Problem:
                - identify the issue

                Fix:
                - give exact fix
                - include minimal code snippet

                Why:
                - short reason

                Code:
                $code
            """.trimIndent()

            "modify" -> """
                Impact:
                - what this change affects

                Risk:
                - what might break

                Suggestion:
                - optional improvement

                Code:
                $code
            """.trimIndent()

            "new" -> """
                Next steps:
                - concrete step
                - concrete step
                - concrete step

                Optional code:
                ```java
                minimal example
                ```

                Code:
                $code
            """.trimIndent()

            else -> """
                Summary:
                - short explanation

                Key idea:
                - main concept

                Code:
                $code
            """.trimIndent()
        }
    }
}