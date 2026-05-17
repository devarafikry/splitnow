package com.devara.splitnow.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalFocusManager

/**
 * iOS convention: tapping outside any text field dismisses the keyboard.
 * Wrap a screen root with `Modifier.dismissKeyboardOnTap()` so any tap that
 * doesn't hit a consuming child clears focus (which closes the soft keyboard).
 *
 * The `indication = null` + custom MutableInteractionSource keeps the modifier
 * invisible — no ripple, no hover state — so it acts purely as a fall-through
 * gesture handler.
 */
@Composable
fun Modifier.dismissKeyboardOnTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val source = remember { MutableInteractionSource() }
    this.clickable(
        interactionSource = source,
        indication = null,
    ) { focusManager.clearFocus() }
}
