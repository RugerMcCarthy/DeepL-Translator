package com.github.nthily.translator.utils

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Indication
import androidx.compose.foundation.gestures.PressGestureScope
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


@Composable
private fun PressedInteractionSourceDisposableEffect(
    interactionSource: MutableInteractionSource,
    pressedInteraction: MutableState<PressInteraction.Press?>
) {
    DisposableEffect(interactionSource) {
        onDispose {
            pressedInteraction.value?.let { oldValue ->
                val interaction = PressInteraction.Cancel(oldValue)
                interactionSource.tryEmit(interaction)
                pressedInteraction.value = null
            }
        }
    }
}
private suspend fun PressGestureScope.handlePressInteraction(
    pressPoint: Offset,
    interactionSource: MutableInteractionSource,
    pressedInteraction: MutableState<PressInteraction.Press?>
) {
    val pressInteraction = PressInteraction.Press(pressPoint)
    interactionSource.emit(pressInteraction)
    pressedInteraction.value = pressInteraction
    val success = tryAwaitRelease()
    val endInteraction =
        if (success) {
            PressInteraction.Release(pressInteraction)
        } else {
            PressInteraction.Cancel(pressInteraction)
        }
    interactionSource.emit(endInteraction)
    pressedInteraction.value = null
}

@ExperimentalFoundationApi
fun Modifier.longClickable(
    interactionSource: MutableInteractionSource,
    indication: Indication?,
    enabled: Boolean = true,
    onLongClick: (() -> Unit)? = null
) = composed(
    factory = {
        val pressedInteraction = remember { mutableStateOf<PressInteraction.Press?>(null) }
        val gesture = if (enabled) {
            PressedInteractionSourceDisposableEffect(interactionSource, pressedInteraction)
            Modifier.pointerInput(onLongClick, interactionSource) {
                detectTapGestures(
                    onLongPress = if (onLongClick != null) {
                        {
                            onLongClick()
                            GlobalScope.launch{
                                interactionSource.emit(PressInteraction.Release(PressInteraction.Press(it)))
                            }
                        }
                    } else {
                        null
                    },
                    onPress = { offset ->
                        handlePressInteraction(offset, interactionSource, pressedInteraction)
                    },
                )
            }
        } else {
            Modifier
        }
        Modifier
            .indication(interactionSource, indication)
            .then(gesture)
    }
)