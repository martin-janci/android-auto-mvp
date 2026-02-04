package com.example.calltasks.ai

import com.aallam.openai.api.chat.ChatCompletion
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.aallam.openai.client.OpenAIConfig
import com.example.calltasks.BuildConfig
import kotlin.time.Duration.Companion.seconds

/**
 * Client for interacting with OpenAI API.
 * Configured with 30 second timeout and uses gpt-3.5-turbo model.
 */
class OpenAiClient {

    private val openAI: OpenAI? = createClient()

    companion object {
        private const val MODEL = "gpt-3.5-turbo"
        private val TIMEOUT = 30.seconds
    }

    private fun createClient(): OpenAI? {
        val apiKey = BuildConfig.OPENAI_API_KEY
        if (apiKey.isBlank()) {
            return null
        }

        val config = OpenAIConfig(
            token = apiKey,
            timeout = Timeout(request = TIMEOUT)
        )
        return OpenAI(config)
    }

    /**
     * Check if the client is properly configured with an API key.
     */
    fun isConfigured(): Boolean = openAI != null

    /**
     * Send a chat completion request to OpenAI.
     *
     * @param systemPrompt The system message to set context
     * @param userMessage The user message/query
     * @return The assistant's response text, or null if failed
     */
    suspend fun chat(systemPrompt: String, userMessage: String): Result<String> {
        val client = openAI ?: return Result.failure(
            IllegalStateException("OpenAI client not configured. Set OPENAI_API_KEY.")
        )

        return try {
            val request = ChatCompletionRequest(
                model = ModelId(MODEL),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = systemPrompt
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = userMessage
                    )
                )
            )

            val completion: ChatCompletion = client.chatCompletion(request)
            val response = completion.choices.firstOrNull()?.message?.content
                ?: return Result.failure(Exception("Empty response from OpenAI"))

            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
