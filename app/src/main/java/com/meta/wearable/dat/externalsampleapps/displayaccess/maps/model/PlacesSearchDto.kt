package com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model

import kotlinx.serialization.Serializable

@Serializable data class PlacesSearchRequestDto(val textQuery: String)

@Serializable data class PlacesSearchResponseDto(val places: List<PlaceDto> = emptyList())

@Serializable
data class PlaceDto(
    val displayName: PlaceDisplayNameDto? = null,
    val formattedAddress: String? = null,
    val location: PlaceLocationDto? = null,
)

@Serializable data class PlaceDisplayNameDto(val text: String = "")

@Serializable
data class PlaceLocationDto(val latitude: Double = 0.0, val longitude: Double = 0.0)
