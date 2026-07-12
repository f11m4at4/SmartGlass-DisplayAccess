package com.meta.wearable.dat.externalsampleapps.displayaccess.ai.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaceIntentResponseDto(
    @SerialName("intent_type") val intentType: String = "place_search",
    @SerialName("place_query") val placeQuery: String,
    @SerialName("search_area_hint") val searchAreaHint: String? = null,
    @SerialName("needs_clarification") val needsClarification: Boolean = false,
)
