package com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model

data class ParsedPlaceIntent(
    val originalUtterance: String,
    val placeQuery: String,
    val intentType: String = "place_search",
    val confidence: Float = 0f,
    val searchAreaHint: String? = null,
    val needsClarification: Boolean = false,
)
