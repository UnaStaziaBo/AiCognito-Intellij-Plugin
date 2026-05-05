package org.jetbrains.plugins.template.ai

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class AiService {

    private val client = OkHttpClient()

    // API key is loaded from environment variables
    private val apiKey: String = System.getenv("OPENAI_API_KEY")
        ?: throw RuntimeException("OPENAI_API_KEY not set")

    fun askAI(prompt: String): String {

        val requestBody = createChatRequestBody(prompt)

        val request = Request.Builder()
            .url(CHAT_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw RuntimeException("Empty response from AI")

        return parseChatResponse(responseBody)
    }

    fun transcribeAudio(file: File): String {

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.name,
                file.asRequestBody("audio/wav".toMediaType())
            )
            .addFormDataPart("model", TRANSCRIBE_MODEL)
            .build()

        val request = Request.Builder()
            .url(TRANSCRIBE_URL)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()
            ?: throw RuntimeException("Empty transcription response")

        return parseTranscription(responseBody)
    }

    // Builds request body for chat completion
    private fun createChatRequestBody(prompt: String) =
        JSONObject().apply {
            put("model", CHAT_MODEL)

            val messages = JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            }

            put("messages", messages)
        }.toString().toRequestBody(JSON_MEDIA_TYPE)

    // Extracts assistant response text
    private fun parseChatResponse(response: String): String {
        val json = JSONObject(response)

        return json
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    // Extracts transcribed text
    private fun parseTranscription(response: String): String {
        val json = JSONObject(response)
        return json.optString("text", "No speech detected")
    }

    companion object {
        private const val CHAT_URL = "https://api.openai.com/v1/chat/completions"
        private const val TRANSCRIBE_URL = "https://api.openai.com/v1/audio/transcriptions"

        private const val CHAT_MODEL = "gpt-4.1-mini"
        private const val TRANSCRIBE_MODEL = "gpt-4o-mini-transcribe"

        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }
}