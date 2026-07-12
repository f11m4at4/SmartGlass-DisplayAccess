/*
 * Copyright (c) Meta Platforms, Inc. and affiliates.
 * All rights reserved.
 *
 * This source code is licensed under the license found in the
 * LICENSE file in the root directory of this source tree.
 */

package com.meta.wearable.dat.externalsampleapps.displayaccess.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.meta.wearable.dat.externalsampleapps.displayaccess.R
import com.meta.wearable.dat.externalsampleapps.displayaccess.navigation.NavigationViewModel
import java.util.Locale

private val CardColor = Color(0xFFFCFCFD)
private val ButtonEnabledColor = Color(0xFF597FF6)
private val ButtonDisabledColor = Color(0xFFD4DAE4)
private val ErrorColor = Color(0xFFB3261E)

@Composable
fun VoicePipelineScreen(
    modifier: Modifier = Modifier,
    viewModel: NavigationViewModel = viewModel(),
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) { viewModel.start() }

  Column(
      modifier =
          modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(horizontal = 32.dp, vertical = 28.dp),
  ) {
    Text(
        text = stringResource(R.string.voice_pipeline_title),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.SemiBold,
    )

    Spacer(modifier = Modifier.height(16.dp))

    val statusText =
        when {
          uiState.isSpeaking -> stringResource(R.string.voice_paused_status)
          uiState.isListening -> stringResource(R.string.voice_listening_status)
          else -> stringResource(R.string.voice_stopped_status)
        }
    Text(
        text = statusText,
        style = MaterialTheme.typography.titleMedium,
        color = if (uiState.isListening) ButtonEnabledColor else Color.Black.copy(alpha = 0.5f),
    )

    Spacer(modifier = Modifier.height(24.dp))

    TranscriptCard(
        label = stringResource(R.string.voice_partial_transcript_label),
        text = uiState.speech.partialText,
    )

    Spacer(modifier = Modifier.height(16.dp))

    TranscriptCard(
        label = stringResource(R.string.voice_final_transcript_label),
        text = uiState.speech.finalText,
    )

    uiState.errorMessage?.let { message ->
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = message, color = ErrorColor, style = MaterialTheme.typography.bodyMedium)
    }

    Spacer(modifier = Modifier.height(24.dp))

    if (uiState.isResolvingDestination) {
      Text(
          text = stringResource(R.string.voice_resolving_destination),
          style = MaterialTheme.typography.bodyMedium,
          color = Color.Black.copy(alpha = 0.5f),
      )
      Spacer(modifier = Modifier.height(16.dp))
    }

    val parsedIntent = uiState.parsedIntent
    TranscriptCard(
        label = stringResource(R.string.voice_parsed_intent_label),
        text =
            parsedIntent?.let { intent ->
              "${intent.placeQuery} (${intent.intentType}, confidence=${intent.confidence})"
            } ?: "",
    )

    Spacer(modifier = Modifier.height(16.dp))

    val target = uiState.target
    TranscriptCard(
        label = stringResource(R.string.voice_destination_label),
        text =
            target?.let { "${it.place.name} — ${it.place.address}" }
                ?: stringResource(R.string.voice_no_destination),
    )

    uiState.aiErrorMessage?.let { message ->
      Spacer(modifier = Modifier.height(16.dp))
      Text(
          text = "OpenAI: $message",
          color = ErrorColor,
          style = MaterialTheme.typography.bodyMedium,
      )
    }

    uiState.placesErrorMessage?.let { message ->
      Spacer(modifier = Modifier.height(16.dp))
      Text(
          text = "Places: $message",
          color = ErrorColor,
          style = MaterialTheme.typography.bodyMedium,
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    val snapshot = uiState.snapshot
    TranscriptCard(
        label = stringResource(R.string.nav_status_label),
        text =
            snapshot?.let {
              String.format(
                  Locale.US,
                  "heading %.0f° · bearing %.0f° · turn %.0f° · %.0fm",
                  it.headingDegrees,
                  it.targetBearingDegrees,
                  it.turnAngleDegrees,
                  it.distanceMeters,
              )
            } ?: stringResource(R.string.nav_no_snapshot),
    )

    Spacer(modifier = Modifier.height(16.dp))

    val miniMapState = uiState.miniMapState
    TranscriptCard(
        label = stringResource(R.string.nav_minimap_label),
        text =
            when {
              target == null -> stringResource(R.string.nav_minimap_no_target)
              miniMapState == null -> stringResource(R.string.voice_resolving_destination)
              else -> {
                val ageSeconds = (System.currentTimeMillis() - miniMapState.lastUpdatedAtMillis) / 1000
                val fallbackSuffix =
                    if (miniMapState.isFallbackMode) {
                      stringResource(R.string.nav_minimap_fallback_suffix)
                    } else {
                      ""
                    }
                "updated ${ageSeconds}s ago$fallbackSuffix"
              }
            },
    )

    uiState.mapErrorMessage?.let { message ->
      Spacer(modifier = Modifier.height(16.dp))
      Text(text = "Map: $message", color = ErrorColor, style = MaterialTheme.typography.bodyMedium)
    }

    Spacer(modifier = Modifier.height(24.dp))

    ActionButton(
        label =
            stringResource(
                if (uiState.isListening) {
                  R.string.voice_stop_listening_button
                } else {
                  R.string.voice_start_listening_button
                },
            ),
        enabled = true,
        onClick = {
          if (uiState.isListening) viewModel.stopListening() else viewModel.startListening()
        },
    )

    Spacer(modifier = Modifier.height(12.dp))

    ActionButton(
        label = stringResource(R.string.voice_speak_final_button),
        enabled = uiState.speech.finalText.isNotBlank() && !uiState.isSpeaking,
        onClick = { viewModel.speak(uiState.speech.finalText) },
    )
  }
}

@Composable
private fun TranscriptCard(label: String, text: String) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(20.dp))
              .background(CardColor)
              .padding(20.dp),
  ) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = Color.Black.copy(alpha = 0.5f),
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = text.ifBlank { stringResource(R.string.voice_no_transcript) },
        style = MaterialTheme.typography.bodyLarge,
    )
  }
}

@Composable
private fun ActionButton(label: String, enabled: Boolean, onClick: () -> Unit) {
  Column(
      modifier =
          Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(999.dp))
              .background(if (enabled) ButtonEnabledColor else ButtonDisabledColor)
              .clickable(enabled = enabled, onClick = onClick)
              .padding(vertical = 18.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    Text(
        text = label,
        color = Color.White,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
  }
}
