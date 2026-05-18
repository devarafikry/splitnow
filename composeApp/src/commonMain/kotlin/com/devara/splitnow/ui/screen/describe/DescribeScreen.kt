package com.devara.splitnow.ui.screen.describe

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.ui.components.FlowNav
import com.devara.splitnow.ui.components.PrimaryButton
import com.devara.splitnow.ui.components.SplitNowMark
import com.devara.splitnow.ui.components.dismissKeyboardOnTap
import com.devara.splitnow.ui.flow.SplitFlowState
import com.devara.splitnow.ui.theme.SplitNowTokens
import org.koin.compose.koinInject

@Composable
fun DescribeScreen(
    onBack: () -> Unit,
    onSplit: (description: String) -> Unit,
) {
    val t = SplitNowTokens.colors
    val flow = koinInject<SplitFlowState>()
    var value by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(flow.description))
    }
    val canSplit = value.text.trim().length >= 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
            .imePadding()
            .dismissKeyboardOnTap(),
    ) {
        FlowNav(title = "Who got what?", onBack = onBack, backLabel = "Back")

        Text(
            "Type naturally. AI figures out who ordered what — no need to add people first.",
            color = t.ink2,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
        )

        // Plain free-text editor — no highlights, no person tags
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            if (value.text.isEmpty()) {
                Text(
                    "e.g. Alex got the pizza and a soda. Maria had the pasta. Sam only had water. We shared the bread.",
                    color = t.ink3,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W400,
                    lineHeight = 31.sp,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxSize(),
                textStyle = TextStyle(
                    color = t.ink,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W400,
                    letterSpacing = (-0.4).sp,
                    lineHeight = 31.sp,
                ),
                cursorBrush = SolidColor(t.accent),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Sentences,
                ),
            )
        }

        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 16.dp)) {
            PrimaryButton(
                label = "Split it",
                enabled = canSplit,
                onClick = {
                    flow.description = value.text
                    onSplit(value.text)
                },
                background = t.ink,
                foreground = t.bg,
                leadingIcon = { SplitNowMark(size = 18.dp, color = t.accentInk) },
            )
        }
    }
}
