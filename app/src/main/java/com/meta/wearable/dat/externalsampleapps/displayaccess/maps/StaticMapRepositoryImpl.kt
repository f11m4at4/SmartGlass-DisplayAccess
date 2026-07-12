package com.meta.wearable.dat.externalsampleapps.displayaccess.maps

import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model.MiniMapState
import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model.NavigationTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

private const val ENDPOINT = "https://maps.googleapis.com/maps/api/staticmap"
private const val MAP_SIZE = "300x200"

class StaticMapRepositoryImpl(
    private val apiKey: String,
    private val client: OkHttpClient = OkHttpClient(),
) : StaticMapRepository {

  override suspend fun createMap(
      currentLatitude: Double,
      currentLongitude: Double,
      currentHeadingDegrees: Double,
      target: NavigationTarget,
  ): Result<MiniMapState> =
      withContext(Dispatchers.IO) {
        runCatching {
          if (apiKey.isBlank()) {
            throw StaticMapRequestException("Google Maps API key is not configured")
          }

          val imageUrl =
              buildImageUrl(currentLatitude, currentLongitude, target.place.latitude, target.place.longitude)
          val request = Request.Builder().url(imageUrl).build()

          client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
              throw StaticMapRequestException("Static map request failed: HTTP ${response.code}")
            }
            response.body?.bytes()
          }

          MiniMapState(
              imageUrl = imageUrl,
              lastRenderLatitude = currentLatitude,
              lastRenderLongitude = currentLongitude,
              lastRenderHeadingDegrees = currentHeadingDegrees,
              lastUpdatedAtMillis = System.currentTimeMillis(),
              isFallbackMode = false,
          )
        }
      }

  private fun buildImageUrl(
      currentLatitude: Double,
      currentLongitude: Double,
      destinationLatitude: Double,
      destinationLongitude: Double,
  ): String =
      ENDPOINT.toHttpUrl()
          .newBuilder()
          .addQueryParameter("size", MAP_SIZE)
          .addQueryParameter("maptype", "roadmap")
          .addQueryParameter("markers", "color:blue|label:A|$currentLatitude,$currentLongitude")
          .addQueryParameter(
              "markers",
              "color:red|label:B|$destinationLatitude,$destinationLongitude",
          )
          .addQueryParameter(
              "path",
              "color:0x3478f6ff|weight:4|$currentLatitude,$currentLongitude|" +
                  "$destinationLatitude,$destinationLongitude",
          )
          .addQueryParameter("key", apiKey)
          .build()
          .toString()
}
