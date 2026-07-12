package com.meta.wearable.dat.externalsampleapps.displayaccess.navigation

import com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model.ParsedPlaceIntent
import com.meta.wearable.dat.externalsampleapps.displayaccess.audio.model.RecognizedSpeech
import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model.MiniMapState
import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model.NavigationSnapshot
import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model.NavigationTarget

data class NavigationUiState(
    val speech: RecognizedSpeech = RecognizedSpeech(),
    val parsedIntent: ParsedPlaceIntent? = null,
    val target: NavigationTarget? = null,
    val snapshot: NavigationSnapshot? = null,
    val miniMapState: MiniMapState? = null,
    val isListening: Boolean = false,
    val isSpeaking: Boolean = false,
    val isResolvingDestination: Boolean = false,
    val errorMessage: String? = null,
    val aiErrorMessage: String? = null,
    val placesErrorMessage: String? = null,
    val mapErrorMessage: String? = null,
)
