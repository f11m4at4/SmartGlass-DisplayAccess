package com.meta.wearable.dat.externalsampleapps.displayaccess.ai

import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model.ParsedPlaceIntent

interface OpenAiRepository {
  suspend fun parsePlaceIntent(transcript: String): Result<ParsedPlaceIntent>
}
