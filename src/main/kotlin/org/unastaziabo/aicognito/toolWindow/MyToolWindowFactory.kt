package org.unastaziabo.aicognito.toolWindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import org.unastaziabo.aicognito.ai.AiService
import org.unastaziabo.aicognito.voice.VoiceRecorder
import java.awt.BorderLayout
import java.awt.Color
import java.io.File
import javax.swing.*

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {

        val panel = JPanel(BorderLayout())

        val outputArea = JEditorPane("text/html", "").apply {
            isEditable = false
            background = Color(43, 43, 43)
            foreground = Color(187, 187, 187)
        }

        val scrollPane = JScrollPane(outputArea)

        val loadingLabel = JLabel("Select a mode").apply {
            foreground = Color(120, 120, 120)
        }

        val aiService = AiService()

        outputArea.text = """
            <html>
            <body style="color:#bbbbbb;">
            <b>AiCognito</b><br>
            Select a mode
            </body>
            </html>
        """.trimIndent()

        fun escape(text: String): String {
            return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
        }

        fun extractCode(text: String): String? {
            val regex = Regex("```[a-zA-Z]*\\n(.*?)```", RegexOption.DOT_MATCHES_ALL)
            return regex.find(text)?.groupValues?.get(1)?.trim()
        }

        fun formatResult(text: String): String {

            val code = extractCode(text)

            val cleanText = text
                .replace(Regex("```[a-zA-Z]*\\n(.*?)```", RegexOption.DOT_MATCHES_ALL), "")
                .trim()

            val safeText = escape(cleanText).replace("\n", "<br>")
            val safeCode = code?.let { escape(it) }

            return """
                <html>
                <body style="background:#2b2b2b; color:#bbbbbb; font-family:Arial;">

                $safeText

                ${
                if (safeCode != null) """
                    <br><br>
                    <b>Suggested fix:</b><br>
                    <pre style="
                        background:#1e1e1e;
                        color:#d4d4d4;
                        padding:10px;
                        font-family:monospace;
                        white-space:pre;
                    ">
                    $safeCode
                    </pre>
                """.trimIndent() else ""
            }

                </body>
                </html>
            """.trimIndent()
        }

        fun getSelectedText(): String? {
            return ApplicationManager.getApplication().runReadAction<String?> {
                FileEditorManager.getInstance(project)
                    .selectedTextEditor
                    ?.selectionModel
                    ?.selectedText
            }
        }

        fun buildPrompt(mode: String, code: String): String {

            return when (mode) {

                "debug" -> """
                    Problem:
                    - identify issue

                    Fix:
                    - include minimal code

                    Why:
                    - short reason

                    Code:
                    $code
                """.trimIndent()

                "modify" -> """
                    Impact:
                    - what changes

                    Risk:
                    - what might break

                    Suggestion:
                    - improvement

                    Code:
                    $code
                """.trimIndent()

                "new" -> """
                    Next steps:
                    - step
                    - step
                    - step

                    Optional code:
                    ```java
                    example
                    ```

                    Code:
                    $code
                """.trimIndent()

                else -> """
                    Explain:
                    - short explanation

                    Code:
                    $code
                """.trimIndent()
            }
        }

        fun runAI(mode: String) {

            val selectedText = getSelectedText()

            if (selectedText.isNullOrEmpty()) {
                outputArea.text = "<html>Please select code</html>"
                return
            }

            val prompt = buildPrompt(mode, selectedText)

            loadingLabel.text = "Thinking..."
            outputArea.text = "<html><i>Processing...</i></html>"

            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    val result = aiService.askAI(prompt)

                    ApplicationManager.getApplication().invokeLater {
                        loadingLabel.text = "Done"
                        outputArea.text = formatResult(result)
                    }

                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        loadingLabel.text = "Error"
                        outputArea.text = "<html>Error: ${e.message}</html>"
                    }
                }
            }
        }

        fun runVoice() {

            val file = File(System.getProperty("java.io.tmpdir"), "recording.wav")

            ApplicationManager.getApplication().executeOnPooledThread {

                VoiceRecorder.recordAudio(file, 8)

                val text = aiService.transcribeAudio(file)

                val mode = when {
                    text.contains("debug", true) -> "debug"
                    text.contains("modify", true) -> "modify"
                    text.contains("new", true) -> "new"
                    else -> "explain"
                }

                runAI(mode)
            }
        }

        val buttonPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)

            add(JButton("New").apply { addActionListener { runAI("new") } })
            add(JButton("Modify").apply { addActionListener { runAI("modify") } })
            add(JButton("Debug").apply { addActionListener { runAI("debug") } })
            add(JButton("Explain").apply { addActionListener { runAI("explain") } })
            add(JButton("Voice").apply { addActionListener { runVoice() } })
        }

        panel.add(buttonPanel, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)
        panel.add(loadingLabel, BorderLayout.SOUTH)

        toolWindow.contentManager.addContent(
            ContentFactory.getInstance().createContent(panel, "", false)
        )
    }
}