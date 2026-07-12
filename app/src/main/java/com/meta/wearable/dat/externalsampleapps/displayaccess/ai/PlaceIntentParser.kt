package com.meta.wearable.dat.externalsampleapps.displayaccess.ai

import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model.ParsedPlaceIntent

interface PlaceIntentParser {
  fun parse(rawResponse: String, originalUtterance: String): ParsedPlaceIntent
}
