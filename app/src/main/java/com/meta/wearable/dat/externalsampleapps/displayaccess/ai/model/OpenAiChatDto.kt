package com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class OpenAiChatRequestDto(
    val model: String,
    val messages: List<OpenAiChatMessageDto>,
    @SerialName("response_format") val responseFormat: OpenAiResponseFormatDto,
    val temperature: Double = 0.0,
)

@Serializable data class OpenAiChatMessageDto(val role: String, val content: String)

@Serializable
data class OpenAiResponseFormatDto(
    val type: String = "json_schema",
    @SerialName("json_schema") val jsonSchema: OpenAiJsonSchemaDto,
)

@Serializable
data class OpenAiJsonSchemaDto(
    val name: String,
    val strict: Boolean = true,
    val schema: JsonObject,
)

@Serializable
data class OpenAiChatResponseDto(val choices: List<OpenAiChatChoiceDto> = emptyList())

@Serializable data class OpenAiChatChoiceDto(val message: OpenAiChatMessageDto)
