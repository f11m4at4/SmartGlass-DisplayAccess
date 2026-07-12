package com.meta.wearable.dat.externalsampleapps.displayaccess

object AppConfig {
  val openAiApiKey: String
    get() = BuildConfig.OPENAI_API_KEY

  val googleMapsApiKey: String
    get() = BuildConfig.GOOGLE_MAPS_API_KEY
}
