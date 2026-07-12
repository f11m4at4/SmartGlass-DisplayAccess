package com.meta.wearable.dat.externalsampleapps.displayaccess.ai

import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model.ParsedPlaceIntent
import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model.PlaceIntentResponseDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

class PlaceIntentParserImpl(private val json: Json = Json { ignoreUnknownKeys = true }) :
    PlaceIntentParser {

  override fun parse(rawResponse: String, originalUtterance: String): ParsedPlaceIntent {
    val dto = json.decodeFromString<PlaceIntentResponseDto>(rawResponse)
    return ParsedPlaceIntent(
        originalUtterance = originalUtterance,
        placeQuery = dto.placeQuery,
        intentType = dto.intentType,
        confidence = if (dto.needsClarification) 0.4f else 1f,
        searchAreaHint = dto.searchAreaHint,
        needsClarification = dto.needsClarification,
    )
  }
}
