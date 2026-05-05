package com.github.unastaziabo.aicognitointellijplugin.toolWindow

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import com.github.unastaziabo.aicognitointellijplugin.ai.AiService
import com.github.unastaziabo.aicognitointellijplugin.voice.VoiceRecorder
import java.awt.BorderLayout
import java.io.File
import javax.swing.*

class MyToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {

        val panel = JPanel(BorderLayout())

        val outputArea = JEditorPane("text/html", "").apply {
            isEditable = false
        }

        val scrollPane = JScrollPane(outputArea)

        val aiService = AiService()
        val loadingLabel = JLabel("")

        // Initial UI message
        outputArea.text = """
            <html>
            <h2>Cognitive Assistant</h2>
            <p>Select how to analyze your code:</p>
            <ul>
                <li>New</li>
                <li>Modify</li>
                <li>Debug</li>
                <li>Explain</li>
                <li>Or use voice input</li>
            </ul>
            </html>
        """.trimIndent()

        // Simple text-to-speech using system command
        fun speak(text: String) {
            try {
                Runtime.getRuntime().exec(arrayOf("espeak", text.take(150)))
            } catch (_: Exception) {}
        }

        // Formats AI response into readable HTML
        fun formatResult(text: String): String {
            return """
                <html>
                <body style="font-family: sans-serif; padding: 10px;">
                    ${text
                .replace("Summary:", "<h3>Summary</h3>")
                .replace("Next steps:", "<h3>Next Steps</h3>")
                .replace("Impact:", "<h3>Impact</h3>")
                .replace("Risk:", "<h3>Risk</h3>")
                .replace("Problem:", "<h3>Problem</h3>")
                .replace("Fix:", "<h3>Fix</h3>")
                .replace("\n", "<br>")
            }
                </body>
                </html>
            """.trimIndent()
        }

        // Safely retrieves selected text from editor
        fun getSelectedText(): String? {
            return ApplicationManager.getApplication().runReadAction<String?> {
                val editor = FileEditorManager.getInstance(project).selectedTextEditor
                editor?.selectionModel?.selectedText
            }
        }

        // Runs AI analysis
        fun runAI(mode: String) {

            val selectedText = getSelectedText()

            if (selectedText.isNullOrEmpty()) {
                outputArea.text = "<html><b>Please select code</b></html>"
                return
            }

            val prompt = when (mode) {
                "new" -> "Next steps:\n$selectedText"
                "modify" -> "Impact and risks:\n$selectedText"
                "debug" -> "Find problem and fix:\n$selectedText"
                else -> "Explain:\n$selectedText"
            }

            loadingLabel.text = "Thinking..."

            ApplicationManager.getApplication().executeOnPooledThread {
                try {
                    val result = aiService.askAI(prompt)

                    ApplicationManager.getApplication().invokeLater {
                        loadingLabel.text = "Done"
                        outputArea.text = formatResult(result)
                    }

                    speak(result)

                } catch (e: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        loadingLabel.text = "Error"
                        outputArea.text = "<html><b>Error:</b> ${e.message}</html>"
                    }
                }
            }
        }

        // Detects mode from voice input
        fun detectModeFromVoice(text: String): String {
            val lower = text.lowercase()
            return when {
                lower.contains("new") -> "new"
                lower.contains("modify") || lower.contains("change") -> "modify"
                lower.contains("debug") || lower.contains("error") || lower.contains("wrong") -> "debug"
                else -> "explain"
            }
        }

        // Voice interaction flow
        fun runVoice() {

            val file = File(System.getProperty("java.io.tmpdir"), "recording.wav")

            loadingLabel.text = "Recording (8s)..."

            ApplicationManager.getApplication().executeOnPooledThread {

                try {
                    VoiceRecorder.recordAudio(file, 8)

                    if (!file.exists() || file.length() == 0L) {
                        ApplicationManager.getApplication().invokeLater {
                            loadingLabel.text = "No audio recorded"
                        }
                        return@executeOnPooledThread
                    }

                    ApplicationManager.getApplication().invokeLater {
                        loadingLabel.text = "Transcribing..."
                    }

                    val text = aiService.transcribeAudio(file)
                    val mode = detectModeFromVoice(text)

                    ApplicationManager.getApplication().invokeLater {
                        loadingLabel.text = "Detected: \"$text\" → $mode"
                    }

                    runAI(mode)

                } catch (_: Exception) {
                    ApplicationManager.getApplication().invokeLater {
                        loadingLabel.text = "Voice error"
                    }
                }
            }
        }

        // Buttons
        val newButton = JButton("New")
        val modifyButton = JButton("Modify")
        val debugButton = JButton("Debug")
        val explainButton = JButton("Explain")
        val voiceButton = JButton("Speak")

        newButton.addActionListener { runAI("new") }
        modifyButton.addActionListener { runAI("modify") }
        debugButton.addActionListener { runAI("debug") }
        explainButton.addActionListener { runAI("explain") }
        voiceButton.addActionListener { runVoice() }

        val buttonPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            add(newButton)
            add(modifyButton)
            add(debugButton)
            add(explainButton)
            add(voiceButton)
        }

        panel.add(buttonPanel, BorderLayout.NORTH)
        panel.add(scrollPane, BorderLayout.CENTER)
        panel.add(loadingLabel, BorderLayout.SOUTH)

        val content = ContentFactory.getInstance()
            .createContent(panel, "", false)

        toolWindow.contentManager.addContent(content)
    }
}