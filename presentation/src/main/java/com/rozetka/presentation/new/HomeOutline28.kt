package com.rozetka.presentation.new

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val HomeOutline28: ImageVector
    get() {
        if (_HomeOutline28 != null) {
            return _HomeOutline28!!
        }
        _HomeOutline28 = ImageVector.Builder(
            name = "HomeOutline28",
            defaultWidth = 28.dp,
            defaultHeight = 28.dp,
            viewportWidth = 28f,
            viewportHeight = 28f
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveToRelative(24f, 11.15f)
                lineToRelative(-8.9f, -8.5f)
                curveToRelative(-0.6f, -0.6f, -1.6f, -0.6f, -2.3f, 0f)
                lineToRelative(-8.9f, 8.5f)
                curveToRelative(-0.6f, 0.6f, -0.9f, 1.4f, -0.9f, 2.2f)
                verticalLineToRelative(8.6f)
                curveToRelative(0f, 1.4f, 1.1f, 2.5f, 2.5f, 2.5f)
                horizontalLineToRelative(6f)
                curveToRelative(0.6f, 0f, 1f, -0.4f, 1f, -1f)
                verticalLineToRelative(-7f)
                horizontalLineToRelative(3f)
                verticalLineToRelative(7f)
                curveToRelative(0f, 0.6f, 0.4f, 1f, 1f, 1f)
                horizontalLineToRelative(6f)
                curveToRelative(1.4f, 0f, 2.5f, -1.1f, 2.5f, -2.5f)
                verticalLineToRelative(-8.6f)
                curveToRelative(0f, -0.8f, -0.4f, -1.6f, -1f, -2.2f)
                close()
                moveTo(23f, 21.95f)
                curveToRelative(0f, 0.3f, -0.2f, 0.5f, -0.5f, 0.5f)
                horizontalLineToRelative(-5f)
                verticalLineToRelative(-7f)
                curveToRelative(0f, -0.6f, -0.4f, -1f, -1f, -1f)
                horizontalLineToRelative(-5f)
                curveToRelative(-0.6f, 0f, -1f, 0.4f, -1f, 1f)
                verticalLineToRelative(7f)
                horizontalLineToRelative(-5f)
                curveToRelative(-0.3f, 0f, -0.5f, -0.2f, -0.5f, -0.5f)
                verticalLineToRelative(-8.6f)
                curveToRelative(0f, -0.3f, 0.1f, -0.5f, 0.3f, -0.7f)
                lineToRelative(8.7f, -8.3f)
                lineToRelative(8.7f, 8.3f)
                curveToRelative(0.2f, 0.2f, 0.3f, 0.5f, 0.3f, 0.7f)
                verticalLineToRelative(8.6f)
                close()
            }
        }.build()

        return _HomeOutline28!!
    }

@Suppress("ObjectPropertyName")
private var _HomeOutline28: ImageVector? = null
