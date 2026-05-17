package com.devara.splitnow.ui.screen.describe

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.ui.components.FlowNav
import com.devara.splitnow.ui.components.Pill
import com.devara.splitnow.ui.components.PrimaryButton
import com.devara.splitnow.ui.components.SplitNowMark
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
    val detected = detectPeople(value.text)
    val canSplit = value.text.trim().length >= 6

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(t.bg)
            .imePadding(),
    ) {
        FlowNav(title = "Who got what?", onBack = onBack, backLabel = "Items")

        // Hint
        Text(
            "Type naturally. As you write, we pick out who ordered what — no need to add people first.",
            color = t.ink2,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 24.dp),
        )

        // Big text editor
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
        ) {
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
                cursorBrush = androidx.compose.ui.graphics.SolidColor(t.accent),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                decorationBox = { inner ->
                    Box {
                        if (value.text.isEmpty()) {
                            Text(
                                "e.g. Budi got the nasi goreng pedas and an es teh. Tina ordered nasi goreng seafood…",
                                color = t.ink3,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.W400,
                                lineHeight = 31.sp,
                            )
                        }
                        inner()
                    }
                },
            )
        }

        // Detected status
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 14.dp)
                .padding(top = 14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(99.dp))
                        .background(t.accentSoft)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SplitNowMark(size = 12.dp, color = t.accent)
                        Spacer(Modifier.width(4.dp))
                        Text("DETECTED", color = t.accent, fontSize = 11.sp, fontWeight = FontWeight.W700, letterSpacing = 0.4.sp)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "${detected.size} ${if (detected.size == 1) "person" else "people"} detected",
                    color = t.ink2,
                    fontSize = 13.sp,
                )
            }
            Spacer(Modifier.height(8.dp))
            if (detected.isNotEmpty()) {
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    detected.forEach { name -> Pill(name, soft = false) }
                }
            }
            Spacer(Modifier.height(14.dp))
        }

        Box(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 4.dp, bottom = 16.dp)) {
            PrimaryButton(
                label = "Split it",
                enabled = canSplit,
                onClick = {
                    flow.description = value.text
                    onSplit(value.text)
                },
                background = t.ink,
                leadingIcon = { SplitNowMark(size = 18.dp, color = t.accentInk) },
            )
        }
    }
}

/** Very lightweight on-device person detection — capitalized words followed by a verb hint. */
private fun detectPeople(text: String): List<String> {
    if (text.isBlank()) return emptyList()
    val verbHints = listOf("got", "ordered", "had", "took", "want", "ate", "drank", "shared")
    val found = linkedSetOf<String>()
    val tokens = text.split(Regex("[^A-Za-z]+")).filter { it.isNotBlank() }
    for (i in tokens.indices) {
        val token = tokens[i]
        if (token.length >= 2 && token[0].isUpperCase() && token.drop(1).all { it.isLowerCase() }) {
            val next = tokens.getOrNull(i + 1)?.lowercase()
            if (next != null && next in verbHints) found.add(token)
        }
    }
    return found.toList()
}
