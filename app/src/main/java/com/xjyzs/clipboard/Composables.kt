package com.xjyzs.clipboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import kotlin.math.tan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: @Composable (() -> Unit)? = null
) {
    var text by remember { mutableStateOf("") }
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        maxLines = 7,
        interactionSource = interactionSource,
        textStyle = LocalTextStyle.current.copy(if (!isSystemInDarkTheme()) Color.Black else Color.White),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp)
            .border(
                width = 1.5.dp,
                color = if (isFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .background(
                Color.Transparent,
                RoundedCornerShape(20.dp)
            ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Center) {
                TextFieldDefaults.DecorationBox(
                    value = text,
                    innerTextField = innerTextField,
                    enabled = true,
                    singleLine = false,
                    visualTransformation = VisualTransformation.None,
                    interactionSource = interactionSource,
                    placeholder = placeholder,
                    container = {},
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    )
                )
            }
        }
    )
}

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val currentTopColor by rememberUpdatedState(
        if (isDark) Color(0xFF1A1C1A) else Color(0xFFF5FFEF)
    )
    val currentBottomColor by rememberUpdatedState(
        if (isDark) Color(0xFF00230F) else Color(0xFFF2FFF8)
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .drawWithCache {
                    val offset = size.height * tan(Math.toRadians(11.06).toFloat())
                    val start = Offset(size.width / 2 + offset / 2, 0f)
                    val end = Offset(size.width / 2 - offset / 2, size.height)
                    val topGradient = Brush.linearGradient(
                        0f to currentTopColor,
                        0.724f to Color.Transparent,
                        start = start,
                        end = end,
                    )
                    val bottomGradient = Brush.linearGradient(
                        0.2552f to Color.Transparent,
                        1f to currentBottomColor,
                        start = start,
                        end = end,
                    )
                    onDrawBehind {
                        drawRect(topGradient)
                        drawRect(bottomGradient)
                    }
                },
        ) { content() }
    }
}