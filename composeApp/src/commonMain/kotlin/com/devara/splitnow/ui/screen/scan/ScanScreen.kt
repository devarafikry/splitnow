package com.devara.splitnow.ui.screen.scan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devara.splitnow.scan.rememberCameraPicker
import com.devara.splitnow.scan.rememberGalleryPicker

@Composable
fun ScanScreen(
    onClose: () -> Unit,
    onCaptured: (ByteArray) -> Unit,
) {
    var hint by remember { mutableStateOf("Hold steady. We'll snap it for you.") }
    val launchCamera = rememberCameraPicker { bytes -> if (bytes != null) onCaptured(bytes) }
    val launchGallery = rememberGalleryPicker { bytes -> if (bytes != null) onCaptured(bytes) }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A0A0A))) {
        // Top bar: close + auto-detect pill + photos
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GlassBtn(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text("Auto-detect receipt", color = Color.White, fontWeight = FontWeight.W600, fontSize = 14.sp)
            }
            Spacer(Modifier.weight(1f))
            GlassBtn(onClick = launchGallery) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }

        // Hint
        Text(
            hint,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 130.dp),
        )

        // Neutral receipt placeholder — gray bars, no merchant text or items
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(240.dp, 360.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFF4F1EA))
                .padding(20.dp),
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF2A2823).copy(alpha = 0.42f)),
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(5.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Color(0xFF2A2823).copy(alpha = 0.22f)),
                )
                Spacer(Modifier.height(20.dp))
                val widths = listOf(0.78f, 0.62f, 0.85f, 0.55f, 0.70f, 0.50f)
                widths.forEach { w ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(w)
                            .height(6.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF2A2823).copy(alpha = 0.30f)),
                    )
                    Spacer(Modifier.height(10.dp))
                }
                Spacer(Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.25f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF2A2823).copy(alpha = 0.55f)),
                    )
                    Spacer(Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.32f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color(0xFF2A2823).copy(alpha = 0.55f)),
                    )
                }
            }
        }

        // Corner brackets
        Brackets()

        // Bottom controls — gallery, big shutter, flip placeholder
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 36.dp, vertical = 56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.14f))
                    .clickable { launchGallery() },
                contentAlignment = Alignment.Center,
            ) {
                Text("Photos", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp, fontWeight = FontWeight.W600)
            }
            Box(
                modifier = Modifier
                    .size(78.dp)
                    .clip(RoundedCornerShape(39.dp))
                    .background(Color.White)
                    .border(width = 4.dp, color = Color.White.copy(alpha = 0.3f), shape = RoundedCornerShape(39.dp))
                    .clickable { launchCamera() },
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(31.dp))
                        .background(Color.White)
                        .border(width = 2.dp, color = Color(0xFF0A0A0A), shape = RoundedCornerShape(31.dp)),
                )
            }
            Box(modifier = Modifier.size(52.dp)) // flip placeholder
        }
    }
}

@Composable
private fun GlassBtn(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.14f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) { content() }
}

@Composable
private fun Brackets() {
    val c = Color.White
    val stroke = 3.dp
    val len = 28.dp
    // Top-left
    Box(modifier = Modifier.offset(x = 40.dp, y = 200.dp).size(len).border(stroke, c, RoundedCornerShape(6.dp, 0.dp, 0.dp, 0.dp)))
    // Top-right
    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier
            .align(Alignment.TopEnd)
            .offset(x = (-40).dp, y = 200.dp)
            .size(len)
            .border(stroke, c, RoundedCornerShape(0.dp, 6.dp, 0.dp, 0.dp)))
        Box(modifier = Modifier
            .align(Alignment.BottomStart)
            .offset(x = 40.dp, y = (-220).dp)
            .size(len)
            .border(stroke, c, RoundedCornerShape(0.dp, 0.dp, 0.dp, 6.dp)))
        Box(modifier = Modifier
            .align(Alignment.BottomEnd)
            .offset(x = (-40).dp, y = (-220).dp)
            .size(len)
            .border(stroke, c, RoundedCornerShape(0.dp, 0.dp, 6.dp, 0.dp)))
    }
}
