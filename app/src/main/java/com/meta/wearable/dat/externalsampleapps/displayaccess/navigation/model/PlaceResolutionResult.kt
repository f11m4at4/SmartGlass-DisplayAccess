package com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model

import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model.ParsedPlaceIntent

sealed interface PlaceResolutionResult {
  data class Success(val parsedIntent: ParsedPlaceIntent, val target: NavigationTarget) :
      PlaceResolutionResult

  data class AiFailure(val message: String) : PlaceResolutionResult

  data class PlacesFailure(val parsedIntent: ParsedPlaceIntent, val message: String) :
      PlaceResolutionResult

  data class NoCandidates(val parsedIntent: ParsedPlaceIntent) : PlaceResolutionResult
}
