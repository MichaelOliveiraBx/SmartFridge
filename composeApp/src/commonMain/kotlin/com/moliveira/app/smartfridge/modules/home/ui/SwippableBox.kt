package com.moliveira.app.smartfridge.modules.home.ui

import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.aakira.napier.Napier
import kotlin.math.roundToInt

enum class DragAnchors {
    Start,
    End,
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeableBox(
    modifier: Modifier,
    isEnable: Boolean = true,
    firstContent: @Composable (modifier: Modifier) -> Unit,
    secondContent: @Composable (modifier: Modifier) -> Unit,
    offsetSize: Dp,
    onTriggered: () -> Unit = {}
) {
    val density = LocalDensity.current
    val positionalThresholds: (totalDistance: Float) -> Float =
        { totalDistance -> totalDistance * 0.5f }
    val velocityThreshold: () -> Float = { with(density) { 100.dp.toPx() } }

    val state = remember(isEnable) {
        AnchoredDraggableState(
            initialValue = DragAnchors.Start,
            positionalThresholds,
            velocityThreshold,
            animationSpec = tween(),
            confirmValueChange = {
                if (it == DragAnchors.End && isEnable) onTriggered()
                false
            },
        ).apply {
            val newAnchors = with(density) {
                DraggableAnchors {
                    DragAnchors.Start at 0.dp.toPx()
                    DragAnchors.End at -(offsetSize * 2f).toPx()
                }
            }
            updateAnchors(newAnchors)
        }
    }

    Box(
        modifier = modifier
    ) {
        secondContent(
            Modifier
                .align(Alignment.CenterEnd)
                .fillMaxHeight()
        )
        firstContent(
            Modifier
                .fillMaxWidth()
                .offset {
                    if (isEnable) {
                        IntOffset(
                            (state.requireOffset() * 0.5f).roundToInt(), 0
                        )
                    } else {
                        IntOffset.Zero
                    }
                }
                .anchoredDraggable(state, Orientation.Horizontal)
        )
    }
}