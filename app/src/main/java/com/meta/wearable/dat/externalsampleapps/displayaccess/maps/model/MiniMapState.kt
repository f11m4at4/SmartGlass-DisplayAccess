package com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model

data class MiniMapState(
    val imageUrl: String = "",
    val lastRenderLatitude: Double? = null,
    val lastRenderLongitude: Double? = null,
    val lastRenderHeadingDegrees: Double? = null,
    val lastUpdatedAtMillis: Long = 0L,
    val isFallbackMode: Boolean = false,
)
