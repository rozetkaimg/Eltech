package com.rozetka.presentation.ui.maps



import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


private data class Room(
    val number: String,
    val path: Path,
    val center: Offset,
    val color: Color,
    val rotateText: Boolean = false
)

private val lightBlue = Color(0xFFCDE6F4)
private val lightGray = Color(0xFFE0E0E0)
private val wallColor = Color(0xFFD9D9D9)
private val pinColor = Color(0xFF007AFF)
private val textColor = Color.Black.copy(alpha = 0.7f)
private val floorSelectorBg = Color(0xFFF5F5F5)

private val buildingFootprintPath = Path().apply {
    moveTo(0f, 334.27f)
    lineTo(308f, 331f) // Bottom horizontal line
    lineTo(327f, 237f)
    lineTo(393f, 177f)
    lineTo(460f, 145f)
    lineTo(507f, 105f)
    lineTo(458f, 50f)
    lineTo(443f, 35f)
    lineTo(259f, 198f)
    lineTo(0f, 197f)
    close()
}

@Composable
private fun rememberRoomListFloor1(): List<Room> {
    return remember {
        listOf(
            Room("2107", Path().apply {
                moveTo(259f, 198f); lineTo(309f, 156f); lineTo(327f, 172f); lineTo(278f, 213f); close()
            }, Offset(293f, 185f), lightBlue, rotateText = true),
            Room("2108", Path().apply {
                moveTo(309f, 156f); lineTo(358f, 112f); lineTo(375f, 128f); lineTo(327f, 172f); close()
            }, Offset(342f, 142f), lightBlue, rotateText = true),
            Room("2109", Path().apply {
                moveTo(358f, 112f); lineTo(393f, 80f); lineTo(410f, 95f); lineTo(375f, 128f); close()
            }, Offset(384f, 104f), lightBlue, rotateText = true),
            Room("2110", Path().apply {
                moveTo(393f, 80f); lineTo(443f, 35f); lineTo(458f, 50f); lineTo(410f, 95f); close()
            }, Offset(426f, 65f), lightBlue, rotateText = true),
            Room("2114", Path().apply {
                moveTo(278f, 213f); lineTo(300f, 195f); lineTo(347f, 220f); lineTo(327f, 237f); close()
            }, Offset(313f, 216f), lightBlue, rotateText = true),
            Room("2115", Path().apply {
                moveTo(327f, 237f); lineTo(347f, 220f); lineTo(410f, 192f); lineTo(393f, 177f); close()
            }, Offset(369f, 206f), lightBlue, rotateText = true),
            Room("2116", Path().apply {
                moveTo(393f, 177f); lineTo(410f, 192f); lineTo(460f, 145f); lineTo(445f, 130f); close()
            }, Offset(427f, 161f), lightBlue, rotateText = true),
            Room("", Path().apply {
                moveTo(4f, 197f); lineTo(57f, 197f); lineTo(57f, 331f); lineTo(4f, 331f); close()
            }, Offset(30f, 264f), lightBlue),
            Room("", Path().apply {
                moveTo(61f, 197f); lineTo(116f, 197f); lineTo(116f, 239f); lineTo(61f, 239f); close()
            }, Offset(88f, 218f), lightBlue),
            Room("", Path().apply {
                moveTo(119f, 197f); lineTo(183f, 197f); lineTo(183f, 239f); lineTo(119f, 239f); close()
            }, Offset(151f, 218f), lightGray),
            Room("", Path().apply {
                moveTo(186f, 197f); lineTo(256f, 197f); lineTo(270f, 284f); lineTo(186f, 284f); lineTo(186f, 239f); close()
            }, Offset(221f, 240f), lightGray),
            Room("hanger", Path().apply {
                moveTo(160f, 272f); lineTo(253f, 272f); lineTo(253f, 331f); lineTo(160f, 331f); close()
            }, Offset(206f, 301f), lightGray),
            Room("", Path().apply {
                moveTo(445f, 130f); lineTo(492f, 90f); lineTo(507f, 105f); lineTo(460f, 145f); close()
            }, Offset(476f, 117f), lightGray)
        )
    }
}

@Composable
private fun rememberRoomListFloor2(): List<Room> {
    return remember {
        listOf(
            Room("3107", Path().apply {
                moveTo(259f, 198f); lineTo(309f, 156f); lineTo(327f, 172f); lineTo(278f, 213f); close()
            }, Offset(293f, 185f), lightBlue, rotateText = true),
            Room("3108", Path().apply {
                moveTo(309f, 156f); lineTo(358f, 112f); lineTo(375f, 128f); lineTo(327f, 172f); close()
            }, Offset(342f, 142f), lightBlue, rotateText = true),
            Room("3109", Path().apply {
                moveTo(358f, 112f); lineTo(393f, 80f); lineTo(410f, 95f); lineTo(375f, 128f); close()
            }, Offset(384f, 104f), lightBlue, rotateText = true),
            Room("3110", Path().apply {
                moveTo(393f, 80f); lineTo(443f, 35f); lineTo(458f, 50f); lineTo(410f, 95f); close()
            }, Offset(426f, 65f), lightBlue, rotateText = true),
            Room("3114", Path().apply {
                moveTo(278f, 213f); lineTo(300f, 195f); lineTo(347f, 220f); lineTo(327f, 237f); close()
            }, Offset(313f, 216f), lightBlue, rotateText = true),
            Room("3115", Path().apply {
                moveTo(327f, 237f); lineTo(347f, 220f); lineTo(410f, 192f); lineTo(393f, 177f); close()
            }, Offset(369f, 206f), lightBlue, rotateText = true),
            Room("3116", Path().apply {
                moveTo(393f, 177f); lineTo(410f, 192f); lineTo(460f, 145f); lineTo(445f, 130f); close()
            }, Offset(427f, 161f), lightBlue, rotateText = true),
            Room("", Path().apply {
                moveTo(4f, 197f); lineTo(57f, 197f); lineTo(57f, 331f); lineTo(4f, 331f); close()
            }, Offset(30f, 264f), lightBlue),
            Room("", Path().apply {
                moveTo(61f, 197f); lineTo(116f, 197f); lineTo(116f, 239f); lineTo(61f, 239f); close()
            }, Offset(88f, 218f), lightBlue),
            Room("", Path().apply {
                moveTo(119f, 197f); lineTo(183f, 197f); lineTo(183f, 239f); lineTo(119f, 239f); close()
            }, Offset(151f, 218f), lightGray),
            Room("", Path().apply {
                moveTo(186f, 197f); lineTo(256f, 197f); lineTo(270f, 284f); lineTo(186f, 284f); lineTo(186f, 239f); close()
            }, Offset(221f, 240f), lightGray),
            Room("hanger", Path().apply {
                moveTo(160f, 272f); lineTo(253f, 272f); lineTo(253f, 331f); lineTo(160f, 331f); close()
            }, Offset(206f, 301f), lightGray),
            Room("", Path().apply {
                moveTo(445f, 130f); lineTo(492f, 90f); lineTo(507f, 105f); lineTo(460f, 145f); close()
            }, Offset(476f, 117f), lightGray)
        )
    }
}


// --- Иконки ---
// (код пропущен)

/**
 * Новый главный компонент, который включает выбор этажа и саму карту.
 */
@Composable
fun BuildingViewer(
    cabinetToHighlight: String? = null,
    modifier: Modifier = Modifier
) {
    var selectedFloor by remember { mutableStateOf(1) }
    val roomListFloor1 = rememberRoomListFloor1()
    val roomListFloor2 = rememberRoomListFloor2()

    // --- НАЧАЛО ИЗМЕНЕНИЙ: Поднятое состояние ---
    var scale by remember { mutableStateOf(1.0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var isInitialized by remember { mutableStateOf(false) }

    // Константы для управления зумом
    val minScale = 0.8f
    val maxScale = 5f
    val zoomFactor = 1.2f // На 20% зум за клик
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---


    val (prefix, currentRoomList) = if (selectedFloor == 1) {
        "2" to roomListFloor1
    } else {
        "3" to roomListFloor2
    }

    // Показываем пин, только если он начинается с префикса текущего этажа (2... или 3...)
    val cabinetToShow = if (cabinetToHighlight?.startsWith(prefix) == true) {
        cabinetToHighlight
    } else {
        null
    }

    // --- НАЧАЛО ИЗМЕНЕНИЙ: Функции обратного вызова для управления состоянием ---

    // Вызывается из InteractiveFloorMap при жесте
    val onTransform = { zoomChange: Float, panChange: Offset ->
        scale = (scale * zoomChange).coerceIn(minScale, maxScale)
        offset += panChange
    }

    // Вызывается из InteractiveFloorMap один раз, когда он получает свой размер
    val onSizeReady = { size: IntSize ->
        if (!isInitialized) {
            // 1. Габариты контента (из SVG)
            val contentWidth = 507f
            val contentHeight = 334f
            val contentCenter = Offset(contentWidth / 2f, contentHeight / 2f)

            // 2. Размер экрана
            val screenWidth = size.width.toFloat()
            val screenHeight = size.height.toFloat()
            val screenCenter = Offset(screenWidth / 2f, screenHeight / 2f)

            // 3. Начальный масштаб
            val initialScale = minOf(screenWidth / contentWidth, screenHeight / contentHeight) * 0.8f
            scale = initialScale.coerceIn(minScale, maxScale)

            // 4. Начальное смещение
            offset = (screenCenter - contentCenter) * scale
            isInitialized = true
        }
    }

    // Вызываются кнопками "+"
    val onZoomIn = {
        scale = (scale * zoomFactor).coerceIn(minScale, maxScale)
    }

    // Вызываются кнопками "–"
    val onZoomOut = {
        scale = (scale / zoomFactor).coerceIn(minScale, maxScale)
    }
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---


    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {

        // Карта рисуется ПЕРВОЙ
        InteractiveFloorMap(
            cabinetNumber = cabinetToShow,
            roomList = currentRoomList,
            modifier = Modifier.fillMaxSize(),
            // --- ИЗМЕНЕНИЕ: Передаем состояние и коллбэки ---
            scale = scale,
            offset = offset,
            onTransform = onTransform,
            onSizeReady = onSizeReady
            // --- КОНЕЦ ИЗМЕНЕНИЯ ---
        )

        // Селектор этажа
        FloorSelector(
            selectedFloor = selectedFloor,
            onFloorSelected = { selectedFloor = it },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(16.dp)
        )

        // --- НАЧАЛО ИЗМЕНЕНИЙ: Добавляем кнопки зума ---
        ZoomControls(
            onZoomIn = onZoomIn,
            onZoomOut = onZoomOut,
            modifier = Modifier
                .align(Alignment.TopStart) // Размещаем внизу слева
                .padding(16.dp)
        )
        // --- КОНЕЦ ИЗМЕНЕНИЙ ---
    }
}

@Composable
private fun FloorSelector(
    selectedFloor: Int,
    onFloorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = MaterialTheme.colorScheme.surfaceContainerLow // lightGray
    val inactiveColor = MaterialTheme.colorScheme.surface
    val buttonShape = RoundedCornerShape(16.dp)
    val backgroundShape = RoundedCornerShape(24.dp)

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainer, shape = backgroundShape)
            .padding(vertical = 6.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val buttonModifier = Modifier
            .padding(4.dp)
            .clip(buttonShape)
            .clickable { onFloorSelected(1) }
            .background(if (selectedFloor == 1) activeColor else inactiveColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)

        Text(
            text = "1",
            modifier = buttonModifier,
            textAlign = TextAlign.Center,

        )

        val buttonModifier2 = Modifier
            .padding(4.dp)
            .clip(buttonShape)
            .clickable { onFloorSelected(2) }
            .background(if (selectedFloor == 2) activeColor else inactiveColor)
            .padding(horizontal = 16.dp, vertical = 12.dp)

        Text(
            text = "2",
            modifier = buttonModifier2,
            textAlign = TextAlign.Center,

        )
        Text(
            text = "3",
            modifier = buttonModifier2,
            textAlign = TextAlign.Center,

        )
        Text(
            text = "4",
            modifier = buttonModifier2,
            textAlign = TextAlign.Center,

        )
        Text(
            text = "5",
            modifier = buttonModifier2,
            textAlign = TextAlign.Center,

        )
        Text(
            text = "6",
            modifier = buttonModifier2,
            textAlign = TextAlign.Center,

        )
        Text(
            text = "7",
            modifier = buttonModifier2,
            textAlign = TextAlign.Center,

        )
        Text(
            text = "8",
            modifier = buttonModifier2,
            textAlign = TextAlign.Center,

        )
    }
}

// --- НАЧАЛО ИЗМЕНЕНИЙ: Новый Composable для кнопок зума ---
@Composable
private fun ZoomControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonShape = RoundedCornerShape(16.dp)
    val backgroundShape = RoundedCornerShape(24.dp)
    val buttonBg = MaterialTheme.colorScheme.surfaceContainer

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceContainer, shape = backgroundShape)

            .padding(vertical = 6.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Кнопка "+"
        Text(
            text = "+",
            modifier = Modifier
                .padding(4.dp)
                .clip(buttonShape)
                .clickable { onZoomIn() }
                .background(buttonBg)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            textAlign = TextAlign.Center,

            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        // Кнопка "–"
        Text(
            text = "–", // Используем en-dash (–) для красоты
            modifier = Modifier
                .padding(4.dp)
                .clip(buttonShape)
                .clickable { onZoomOut() }
                .background(buttonBg)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}
// --- КОНЕЦ ИЗМЕНЕНИЙ ---


/**
 * Отрисовывает план этажа с возможностью зума и панорамирования.
 * Теперь он "глупый" (stateless) и управляется извне.
 */
@Composable
private fun InteractiveFloorMap(
    cabinetNumber: String? = null,
    roomList: List<Room>,
    modifier: Modifier = Modifier,
    // --- НАЧАЛО ИЗМЕНЕНИЙ: Новые параметры состояния ---
    scale: Float,
    offset: Offset,
    onTransform: (zoomChange: Float, panChange: Offset) -> Unit,
    onSizeReady: (IntSize) -> Unit
    // --- КОНЕЦ ИЗМЕНЕНИЙ ---
) {
    // --- Состояние (scale, offset, isInitialized) удалено ---
    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier.clipToBounds()
    ) {
        // --- ИЗМЕНЕНИЕ: Состояние теперь сообщает об изменениях "наверх" ---
        val state = rememberTransformableState { zoomChange, panChange, _ ->
            onTransform(zoomChange, panChange)
        }

        Canvas(
            modifier = Modifier
                .matchParentSize()
                .transformable(state = state)
                .onSizeChanged { size ->
                    // Сообщаем родительскому элементу о нашем размере для инициализации
                    onSizeReady(size)
                }
                .graphicsLayer(
                    // Применяем состояние, полученное от родителя
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            val roomToHighlight = roomList.find { it.number == cabinetNumber }
            // Понижаем порог, т.к. начальный зум может быть < 1
            val zoomThreshold = 1.0f
            val showDetails = scale >= zoomThreshold

            if (showDetails) {
                // --- Детальный вид (приближено) ---

                // 1. Рисуем заливку комнат
                roomList.forEach { room ->
                    drawPath(path = room.path, color = room.color)
                }

                // 2. Рисуем ВСЕ стены
                drawExternalWalls()
                drawInternalWalls()

                // 3. Рисуем номера кабинетов
                drawRoomNumbers(textMeasurer, roomList)

                // 4. Рисуем маркер
                if (roomToHighlight != null) {
                    drawHighlightPin(roomToHighlight.center)
                }
            } else {
                // --- "Вид с крыши" (отдалено) ---

                // 1. Рисуем заливку всего здания цветом стен
                drawPath(path = buildingFootprintPath, color = wallColor)

                // 2. Рисуем только ВНЕШНИЕ стены
                drawExternalWalls()
            }
        }
    }
}

// --- Функции отрисовки ---
// (Весь код drawRoomNumbers, drawHighlightPin, drawExternalWalls, drawInternalWalls... без изменений)
// ... (код пропущен для краткости) ...
@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawRoomNumbers(textMeasurer: TextMeasurer, rooms: List<Room>) {
    val textStyle = TextStyle(
        color = textColor,
        fontSize = 12.sp, // Увеличено
        fontWeight = FontWeight.SemiBold
    )

    rooms.forEach { room ->
        if (room.number.isNotEmpty() && room.number != "hanger") {
            val textLayoutResult = textMeasurer.measure(room.number, textStyle)
            val textOffset = room.center - Offset(textLayoutResult.size.width / 2f, textLayoutResult.size.height / 2f)

            if (room.rotateText) {
                rotate(degrees = -41.5f, pivot = room.center) {
                    drawText(
                        textLayoutResult = textLayoutResult,
                        topLeft = textOffset
                    )
                }
            } else {
                drawText(
                    textLayoutResult = textLayoutResult,
                    topLeft = textOffset
                )
            }
        }
    }
}
private fun DrawScope.drawHighlightPin(center: Offset) {
    drawCircle(
        color = pinColor,
        radius = 12f,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = 5f,
        center = center
    )
}
private fun DrawScope.drawExternalWalls() {
    val color = wallColor
    drawRect(color = color, topLeft = Offset(0f, 197f), size = Size(263f, 4f))
    rotate(degrees = -90f, pivot = Offset(0f, 334.27f)) {
        drawRect(color = color, topLeft = Offset(0f, 334.27f), size = Size(137.27f, 4f))
    }
    rotate(degrees = -132.862f, pivot = Offset(570.376f, 103.339f)) {
        drawRect(color = color, topLeft = Offset(570.376f, 103.339f), size = Size(137.27f, 4f))
    }
    drawRect(color = color, topLeft = Offset(0f, 331f), size = Size(308f, 4f))
    rotate(degrees = -41.462f, pivot = Offset(256f, 198.212f)) {
        drawRect(color = color, topLeft = Offset(256f, 198.212f), size = Size(297.991f, 4.3533f))
    }
    rotate(degrees = -41.462f, pivot = Offset(305.579f, 330.904f)) {
        drawRect(color = color, topLeft = Offset(305.579f, 330.904f), size = Size(353.263f, 4.3533f))
    }
}
private fun DrawScope.drawInternalWalls() {
    val color = wallColor
    rotate(degrees = -41.462f, pivot = Offset(266f, 240.481f)) {
        drawRect(color = color, topLeft = Offset(266f, 240.481f), size = Size(52.0773f, 4f))
    }
    rotate(degrees = -41.462f, pivot = Offset(318f, 197.481f)) {
        drawRect(color = color, topLeft = Offset(318f, 197.481f), size = Size(45.4252f, 4.3533f))
    }
    rotate(degrees = -41.462f, pivot = Offset(363f, 157.077f)) {
        drawRect(color = color, topLeft = Offset(363f, 157.077f), size = Size(40.962f, 4.3533f))
    }
    rotate(degrees = -41.462f, pivot = Offset(400f, 124.122f)) {
        drawRect(color = color, topLeft = Offset(400f, 124.122f), size = Size(30.6482f, 4.3533f))
    }
    rotate(degrees = -41.462f, pivot = Offset(430.416f, 98.8955f)) {
        drawRect(color = color, topLeft = Offset(430.416f, 98.8955f), size = Size(22.8879f, 5.05547f))
    }
    rotate(degrees = -41.462f, pivot = Offset(495f, 53.1548f)) {
        drawRect(color = color, topLeft = Offset(495f, 53.1548f), size = Size(22.8879f, 4f))
    }
    rotate(degrees = -41.462f, pivot = Offset(460f, 69.1548f)) {
        drawRect(color = color, topLeft = Offset(460f, 69.1548f), size = Size(12.4274f, 5.05547f))
    }
    rotate(degrees = -41.462f, pivot = Offset(473.646f, 71.4902f)) {
        drawRect(color = color, topLeft = Offset(473.646f, 71.4902f), size = Size(12.8226f, 4f))
    }
    rotate(degrees = -41.462f, pivot = Offset(425f, 145.122f)) {
        drawRect(color = color, topLeft = Offset(425f, 145.122f), size = Size(49.547f, 4.3533f))
    }
    rotate(degrees = -41.462f, pivot = Offset(307f, 249.122f)) {
        drawRect(color = color, topLeft = Offset(307f, 249.122f), size = Size(60.6119f, 4.3533f))
    }
    rotate(degrees = -41.462f, pivot = Offset(474f, 101.133f)) {
        drawRect(color = color, topLeft = Offset(474f, 101.133f), size = Size(70.8696f, 4.3533f))
    }
    rotate(degrees = -41.462f, pivot = Offset(292f, 284.973f)) {
        drawRect(color = color, topLeft = Offset(292f, 284.973f), size = Size(18.0823f, 4f))
    }
    rotate(degrees = -41.462f, pivot = Offset(366f, 197.122f)) {
        drawRect(color = color, topLeft = Offset(366f, 197.122f), size = Size(59.3073f, 4.3533f))
    }
    drawRect(color = color, topLeft = Offset(4f, 266f), size = Size(29f, 4f))
    drawRect(color = color, topLeft = Offset(51f, 268f), size = Size(131f, 4f))
    drawRect(color = color, topLeft = Offset(56f, 272f), size = Size(4f, 63f))
    drawRect(color = color, topLeft = Offset(58f, 201f), size = Size(2f, 1f))
    drawRect(color = color, topLeft = Offset(59f, 198f), size = Size(4f, 52f))
    drawRect(color = color, topLeft = Offset(63f, 235f), size = Size(56f, 4f))
    drawRect(color = color, topLeft = Offset(116f, 202f), size = Size(3f, 37f))
    drawRect(color = color, topLeft = Offset(199f, 266f), size = Size(57f, 4f))
    drawPath(
        path = Path().apply {
            moveTo(270f, 284f); lineTo(295f, 284f); lineTo(295f, 288f); lineTo(270f, 288f); close()
        },
        color = color
    )
    drawRect(color = color, topLeft = Offset(253f, 295f), size = Size(4f, 36f))
    drawRect(color = color, topLeft = Offset(183f, 216f), size = Size(3f, 25f))
    drawRect(color = color, topLeft = Offset(183f, 216f), size = Size(3f, 25f))
    drawRect(color = color, topLeft = Offset(212f, 198f), size = Size(3f, 18f))
    drawRect(color = color, topLeft = Offset(156f, 311f), size = Size(4f, 23f))
    drawRect(color = color, topLeft = Offset(270f, 285f), size = Size(4f, 46f))
    rotate(degrees = -40.1029f, pivot = Offset(309.229f, 249.829f)) {
        drawRect(color = color, topLeft = Offset(309.229f, 249.829f), size = Size(3f, 59.2108f))
    }
    rotate(degrees = -40.1029f, pivot = Offset(326f, 236.933f)) {
        drawRect(color = color, topLeft = Offset(326f, 236.933f), size = Size(3f, 59.2108f))
    }
    rotate(degrees = -40.1029f, pivot = Offset(393f, 176.933f)) {
        drawRect(color = color, topLeft = Offset(393f, 176.933f), size = Size(3f, 59.2108f))
    }
    rotate(degrees = -40.1029f, pivot = Offset(445f, 129.933f)) {
        drawRect(color = color, topLeft = Offset(445f, 129.933f), size = Size(3f, 59.2108f))
    }
    rotate(degrees = -40.1029f, pivot = Offset(381f, 91.9326f)) {
        drawRect(color = color, topLeft = Offset(381f, 91.9326f), size = Size(3f, 39.6017f))
    }
    rotate(degrees = -40.1029f, pivot = Offset(358f, 111.933f)) {
        drawRect(color = color, topLeft = Offset(358f, 111.933f), size = Size(3f, 39.6017f))
    }
    rotate(degrees = -40.1029f, pivot = Offset(309f, 155.933f)) {
        drawRect(color = color, topLeft = Offset(309f, 155.933f), size = Size(3f, 39.6017f))
    }
    rotate(degrees = -40.1029f, pivot = Offset(259f, 198.577f)) {
        drawRect(color = color, topLeft = Offset(259f, 198.577f), size = Size(4f, 40.3518f))
    }
    rotate(degrees = -40.1029f, pivot = Offset(413f, 59.9326f)) {
        drawRect(color = color, topLeft = Offset(413f, 59.9326f), size = Size(3f, 39.6017f))
    }
    rotate(degrees = -40.1029f, pivot = Offset(443f, 34.9326f)) {
        drawRect(color = color, topLeft = Offset(443f, 34.9326f), size = Size(3f, 51f))
    }
    rotate(degrees = -40.1029f, pivot = Offset(492f, 89.9326f)) {
        drawRect(color = color, topLeft = Offset(492f, 89.9326f), size = Size(3f, 59.2108f))
    }
    drawRect(color = color, topLeft = Offset(156f, 272f), size = Size(4f, 23f))
    drawRect(color = color, topLeft = Offset(4f, 246f), size = Size(29f, 4f))
    drawRect(color = color, topLeft = Offset(183f, 239f), size = Size(86f, 4f))
    drawRect(color = color, topLeft = Offset(183f, 212f), size = Size(32f, 4f))
    drawRect(color = color, topLeft = Offset(47f, 246f), size = Size(14f, 4f))
}



@Preview(showBackground = true, widthDp = 360, heightDp = 640)
@Composable
fun BuildingViewerPreview() {
    BuildingViewer(
        cabinetToHighlight = "2109", // Тест: выделяем кабинет 2109
        modifier = Modifier
            .fillMaxSize()
    )
}