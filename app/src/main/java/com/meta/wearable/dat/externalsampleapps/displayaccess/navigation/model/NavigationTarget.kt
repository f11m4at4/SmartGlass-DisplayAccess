package com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model

import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model.PlaceCandidate

data class NavigationTarget(
    val place: PlaceCandidate,
    val selectedAtMillis: Long,
)
