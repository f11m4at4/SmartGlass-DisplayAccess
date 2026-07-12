package com.meta.wearable.dat.externalsampleapps.displayaccess.maps

import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model.PlaceCandidate
import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model.PlaceDto
import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model.PlacesSearchRequestDto
import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model.PlacesSearchResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private const val ENDPOINT = "https://places.googleapis.com/v1/places:searchText"
private const val FIELD_MASK = "places.displayName,places.formattedAddress,places.location"
private val JSON_MEDIA_TYPE = "application/json".toMediaType()

class PlacesRepositoryImpl(
    private val apiKey: String,
    private val client: OkHttpClient = OkHttpClient(),
    private val json: Json = Json { ignoreUnknownKeys = true },
) : PlacesRepository {

  override suspend fun search(query: String): Result<List<PlaceCandidate>> =
      withContext(Dispatchers.IO) {
        runCatching {
          if (apiKey.isBlank()) {
            throw PlacesRequestException("Google Places API key is not configured")
          }

          val request =
              Request.Builder()
                  .url(ENDPOINT)
                  .header("X-Goog-Api-Key", apiKey)
                  .header("X-Goog-FieldMask", FIELD_MASK)
                  .post(
                      json.encodeToString(PlacesSearchRequestDto(textQuery = query))
                          .toRequestBody(JSON_MEDIA_TYPE),
                  )
                  .build()

          client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
              throw PlacesRequestException(
                  "Places request failed: HTTP ${response.code} - ${responseBody.take(500)}",
              )
            }

            val parsed = json.decodeFromString<PlacesSearchResponseDto>(responseBody)
            parsed.places.mapNotNull { it.toPlaceCandidateOrNull() }
          }
        }
      }

  private fun PlaceDto.toPlaceCandidateOrNull(): PlaceCandidate? {
    val location = location ?: return null
    return PlaceCandidate(
        name = displayName?.text.orEmpty(),
        address = formattedAddress.orEmpty(),
        latitude = location.latitude,
        longitude = location.longitude,
    )
  }
}
