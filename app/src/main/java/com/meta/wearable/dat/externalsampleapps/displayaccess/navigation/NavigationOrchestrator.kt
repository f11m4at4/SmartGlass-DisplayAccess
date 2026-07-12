package com.meta.wearable.dat.externalsampleapps.displayaccess.navigation

import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model.PlaceResolutionResult

interface NavigationOrchestrator {
  suspend fun resolveDestination(utterance: String): PlaceResolutionResult
}
