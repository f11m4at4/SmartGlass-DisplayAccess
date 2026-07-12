package com.meta.wearable.dat.externalsampleapps.displayaccess.ai

import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model.OpenAiChatMessageDto
import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model.OpenAiChatRequestDto
import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model.OpenAiChatResponseDto
import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model.OpenAiJsonSchemaDto
import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model.OpenAiResponseFormatDto
import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model.ParsedPlaceIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private const val ENDPOINT = "https://api.openai.com/v1/chat/completions"
private const val MODEL = "gpt-4o-mini"
private val JSON_MEDIA_TYPE = "application/json".toMediaType()

private const val SYSTEM_PROMPT =
    "You extract a destination search intent from a spoken navigation request. " +
        "Respond only with the requested JSON fields. " +
        "If the utterance is not about navigating anywhere, or the destination is too vague " +
        "to search for, set needs_clarification to true."

private val RESPONSE_SCHEMA = buildJsonObject {
  put("type", JsonPrimitive("object"))
  put(
      "properties",
      buildJsonObject {
        put(
            "intent_type",
            buildJsonObject {
              put("type", JsonPrimitive("string"))
              put(
                  "enum",
                  buildJsonArray {
                    add(JsonPrimitive("place_search"))
                    add(JsonPrimitive("clarification_needed"))
                  },
              )
            },
        )
        put("place_query", buildJsonObject { put("type", JsonPrimitive("string")) })
        put(
            "search_area_hint",
            buildJsonObject {
              put(
                  "type",
                  buildJsonArray {
                    add(JsonPrimitive("string"))
                    add(JsonPrimitive("null"))
                  },
              )
            },
        )
        put("needs_clarification", buildJsonObject { put("type", JsonPrimitive("boolean")) })
      },
  )
  put(
      "required",
      buildJsonArray {
        add(JsonPrimitive("intent_type"))
        add(JsonPrimitive("place_query"))
        add(JsonPrimitive("search_area_hint"))
        add(JsonPrimitive("needs_clarification"))
      },
  )
  put("additionalProperties", JsonPrimitive(false))
}

class OpenAiRepositoryImpl(
    private val apiKey: String,
    private val client: OkHttpClient = OkHttpClient(),
    private val parser: PlaceIntentParser = PlaceIntentParserImpl(),
    private val json: Json = Json { ignoreUnknownKeys = true; encodeDefaults = true },
) : OpenAiRepository {

  override suspend fun parsePlaceIntent(transcript: String): Result<ParsedPlaceIntent> =
      withContext(Dispatchers.IO) {
        runCatching {
          if (apiKey.isBlank()) {
            throw OpenAiRequestException("OpenAI API key is not configured")
          }

          val requestDto =
              OpenAiChatRequestDto(
                  model = MODEL,
                  messages =
                      listOf(
                          OpenAiChatMessageDto(role = "system", content = SYSTEM_PROMPT),
                          OpenAiChatMessageDto(role = "user", content = transcript),
                      ),
                  responseFormat =
                      OpenAiResponseFormatDto(
                          jsonSchema =
                              OpenAiJsonSchemaDto(name = "place_intent", schema = RESPONSE_SCHEMA),
                      ),
              )

          val request =
              Request.Builder()
                  .url(ENDPOINT)
                  .header("Authorization", "Bearer $apiKey")
                  .post(json.encodeToString(requestDto).toRequestBody(JSON_MEDIA_TYPE))
                  .build()

          client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
              throw OpenAiRequestException(
                  "OpenAI request failed: HTTP ${response.code} - ${responseBody.take(500)}",
              )
            }

            val chatResponse = json.decodeFromString<OpenAiChatResponseDto>(responseBody)
            val content =
                chatResponse.choices.firstOrNull()?.message?.content
                    ?: throw OpenAiRequestException("OpenAI response had no choices")

            parser.parse(content, transcript)
          }
        }
      }
}
