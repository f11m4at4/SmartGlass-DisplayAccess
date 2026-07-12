package com.meta.wearable.dat.externalsampleapps.displayaccess.display

import com.meta.wearable.dat.display.views.Alignment
import com.meta.wearable.dat.display.views.ContentScope
import com.meta.wearable.dat.display.views.CornerRadius
import com.meta.wearable.dat.display.views.Direction
import com.meta.wearable.dat.display.views.FlexBoxBackground
import com.meta.wearable.dat.display.views.IconName
import com.meta.wearable.dat.display.views.IconStyle
import com.meta.wearable.dat.display.views.ImageSize
import com.meta.wearable.dat.display.views.TextColor
import com.meta.wearable.dat.display.views.TextStyle
import com.meta.wearable.dat.externalsampleapps.displayaccess.maps.model.MiniMapState
import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model.NavigationSnapshot
import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.model.NavigationTarget
import java.util.Locale

/** Renders the current navigation state as a single root card for the glasses display. */
fun ContentScope.renderNavigation(
    target: NavigationTarget?,
    snapshot: NavigationSnapshot?,
    miniMapState: MiniMapState?,
) {
  if (target == null) {
    renderWaitingForDestination()
    return
  }

  flexBox(direction = Direction.COLUMN, gap = 16, padding = 24, background = FlexBoxBackground.CARD) {
    flexBox(direction = Direction.ROW, gap = 12, crossAlignment = Alignment.CENTER) {
      icon(name = turnIcon(snapshot?.turnAngleDegrees), style = IconStyle.FILLED)
      flexBox(direction = Direction.COLUMN, flexGrow = 1f) {
        text(target.place.name, style = TextStyle.HEADING)
        text(distanceLabel(snapshot), style = TextStyle.BODY, color = TextColor.SECONDARY)
      }
    }

    if (miniMapState != null && miniMapState.imageUrl.isNotBlank() && !miniMapState.isFallbackMode) {
      image(
          uri = miniMapState.imageUrl,
          sizePreset = ImageSize.FILL,
          cornerRadius = CornerRadius.MEDIUM,
      )
    } else {
      text("Map unavailable", style = TextStyle.META, color = TextColor.SECONDARY)
    }
  }
}

private fun ContentScope.renderWaitingForDestination() {
  flexBox(
      direction = Direction.COLUMN,
      gap = 8,
      padding = 24,
      background = FlexBoxBackground.CARD,
      alignment = Alignment.CENTER,
      crossAlignment = Alignment.CENTER,
  ) {
    icon(name = IconName.COMPASS_NORTH_UP_RED, style = IconStyle.FILLED)
    text("Say a destination", style = TextStyle.BODY, color = TextColor.SECONDARY)
  }
}

/** Buckets the turn angle into a small set of icons; positive angles mean turn right. */
private fun turnIcon(turnAngleDegrees: Double?): IconName {
  val angle = turnAngleDegrees ?: return IconName.COMPASS_NORTH_UP_RED
  return when {
    angle <= -150.0 || angle >= 150.0 -> IconName.ARROW_U_LEFT
    angle <= -60.0 -> IconName.ARROW_LEFT
    angle <= -15.0 -> IconName.CARET_LEFT
    angle < 15.0 -> IconName.CARET_UP
    angle < 60.0 -> IconName.CARET_RIGHT
    angle < 150.0 -> IconName.ARROW_RIGHT
    else -> IconName.COMPASS_NORTH_UP_RED
  }
}

private fun distanceLabel(snapshot: NavigationSnapshot?): String {
  val meters = snapshot?.distanceMeters ?: return "Calculating distance…"
  return if (meters >= 1000) {
    String.format(Locale.US, "%.1f km", meters / 1000.0)
  } else {
    String.format(Locale.US, "%.0f m", meters)
  }
}
