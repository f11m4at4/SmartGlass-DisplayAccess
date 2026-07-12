package com.meta.wearable.dat.externalsampleapps.displayaccess.maps

import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model.PlaceCandidate

interface PlacesRepository {
  suspend fun search(query: String): Result<List<PlaceCandidate>>
}
