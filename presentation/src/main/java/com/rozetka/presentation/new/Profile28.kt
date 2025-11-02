package com.rozetka.presentation.new

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp


val Profile28: ImageVector
    get() {
        if (_profile28 != null) {
            return _profile28!!
        }
        _profile28 = Builder(
            name = "Profile28", defaultWidth = 28.0.dp, defaultHeight = 28.0.dp,
            viewportWidth = 28.0f, viewportHeight = 28.0f
        ).apply {
            path(
                fill = SolidColor(Color(0xFF000000)), stroke = SolidColor(Color(0x00000000)),
                strokeLineWidth = 1.0f, strokeLineCap = Butt, strokeLineJoin = Miter,
                strokeLineMiter = 4.0f, pathFillType = NonZero
            ) {
                moveTo(14.0f, 2.0f)
                curveTo(20.6274f, 2.0f, 26.0f, 7.3726f, 26.0f, 14.0f)
                curveTo(26.0f, 20.6274f, 20.6274f, 26.0f, 14.0f, 26.0f)
                curveTo(7.3726f, 26.0f, 2.0f, 20.6274f, 2.0f, 14.0f)
                curveTo(2.0f, 7.3726f, 7.3726f, 2.0f, 14.0f, 2.0f)
                close()
                moveTo(14.0f, 20.5f)
                curveTo(11.9141f, 20.5f, 9.92f, 21.082f, 8.2032f, 22.149f)
                curveTo(9.8382f, 23.3146f, 11.839f, 24.0f, 14.0f, 24.0f)
                curveTo(16.1605f, 24.0f, 18.161f, 23.3148f, 19.796f, 22.15f)
                curveTo(18.0795f, 21.0818f, 16.0857f, 20.5f, 14.0f, 20.5f)
                close()
                moveTo(14.0f, 4.0f)
                curveTo(8.4772f, 4.0f, 4.0f, 8.4772f, 4.0f, 14.0f)
                curveTo(4.0f, 16.6157f, 5.0043f, 18.9968f, 6.6482f, 20.7788f)
                curveTo(8.7856f, 19.3082f, 11.331f, 18.5f, 14.0f, 18.5f)
                curveTo(16.6693f, 18.5f, 19.2149f, 19.3084f, 21.3527f, 20.7774f)
                curveTo(22.9961f, 18.9959f, 24.0f, 16.6152f, 24.0f, 14.0f)
                curveTo(24.0f, 8.4772f, 19.5228f, 4.0f, 14.0f, 4.0f)
                close()
                moveTo(14.0f, 7.5f)
                curveTo(16.6242f, 7.5f, 18.75f, 9.6258f, 18.75f, 12.25f)
                curveTo(18.75f, 14.8742f, 16.6242f, 17.0f, 14.0f, 17.0f)
                curveTo(11.3758f, 17.0f, 9.25f, 14.8742f, 9.25f, 12.25f)
                curveTo(9.25f, 9.6258f, 11.3758f, 7.5f, 14.0f, 7.5f)
                close()
                moveTo(14.0f, 9.5f)
                curveTo(12.4804f, 9.5f, 11.25f, 10.7304f, 11.25f, 12.25f)
                curveTo(11.25f, 13.7696f, 12.4804f, 15.0f, 14.0f, 15.0f)
                curveTo(15.5196f, 15.0f, 16.75f, 13.7696f, 16.75f, 12.25f)
                curveTo(16.75f, 10.7304f, 15.5196f, 9.5f, 14.0f, 9.5f)
                close()
            }
        }
            .build()
        return _profile28!!
    }

private var _profile28: ImageVector? = null
