package com.example.weatherapp

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.scale
import com.airbnb.lottie.compose.*
import kotlin.math.sin
import kotlin.random.Random

private data class Star(
    val xOffset: Float,
    val yOffset: Float,
    val radius: Float,
    val alpha: Float,
    val twinkleSpeed: Int
)

@Composable
fun WelcomeScreen(onContinueClick: () -> Unit) {
    val stars = remember {
        List(70) {
            Star(
                xOffset = Random.nextFloat(),
                yOffset = Random.nextFloat(),
                radius = Random.nextFloat() * 2f + 1f,
                alpha = Random.nextFloat() * 0.5f + 0.3f,
                twinkleSpeed = Random.nextInt(2000, 5000)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val twinkleProgress by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "twinkle"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // --- КОСМИЧЕСКИЙ ФОН ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF060713),
                        Color(0xFF0F1338),
                        Color(0xFF1E1348),
                        Color(0xFF090A1A)
                    )
                )
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF7E57C2).copy(alpha = 0.22f), Color.Transparent),
                    center = Offset(canvasWidth * 0.2f, canvasHeight * 0.3f),
                    radius = canvasWidth * 0.6f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF29B6F6).copy(alpha = 0.15f), Color.Transparent),
                    center = Offset(canvasWidth * 0.8f, canvasHeight * 0.7f),
                    radius = canvasWidth * 0.5f
                )
            )

            stars.forEach { star ->
                val phase = (System.currentTimeMillis() % star.twinkleSpeed) / star.twinkleSpeed.toFloat()
                val currentAlpha = star.alpha * (0.5f + 0.5f * sin(phase * 2 * Math.PI).toFloat()) * twinkleProgress

                drawCircle(
                    color = Color.White.copy(alpha = currentAlpha.coerceIn(0f, 1f)),
                    radius = star.radius,
                    center = Offset(star.xOffset * canvasWidth, star.yOffset * canvasHeight)
                )
            }
        }

        // --- ОСНОВНОЙ КОНТЕНТ ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Верхняя часть: Полумесяц слева и крупная Ракета справа
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(115.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val moonComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.space_moon))
                    val moonProgress by animateLottieCompositionAsState(
                        composition = moonComposition,
                        iterations = LottieConstants.IterateForever
                    )
                    LottieAnimation(
                        composition = moonComposition,
                        progress = { moonProgress },
                        modifier = Modifier.requiredSize(135.dp)
                    )
                }

                Box(
                    modifier = Modifier.size(115.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val rocketComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.rocket))
                    val rocketProgress by animateLottieCompositionAsState(
                        composition = rocketComposition,
                        iterations = LottieConstants.IterateForever,
                        speed = 1.0f
                    )
                    LottieAnimation(
                        composition = rocketComposition,
                        progress = { rocketProgress },
                        modifier = Modifier.requiredSize(235.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Центральная часть (поднята выше, рамка текста шире, космонавт крупнее и без рывка)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Слева: крупный космонавт с обрезанным концом анимации и плавным реверсом
                Box(
                    modifier = Modifier
                        .width(105.dp)
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val personComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.person))
                    val personProgress by animateLottieCompositionAsState(
                        composition = personComposition,
                        iterations = LottieConstants.IterateForever,
                        reverseOnRepeat = true,
                        clipSpec = remember { LottieClipSpec.Frame(min = 20, max = 215) }
                    )
                    LottieAnimation(
                        composition = personComposition,
                        progress = { personProgress },
                        modifier = Modifier
                            .requiredSize(245.dp)
                            .offset(x = (-1).dp)
                            .scale(scaleX = 0.85f, scaleY = 1.0f)
                    )
                }

                // Справа: широкая карточка с текстом
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp),
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomEnd = 20.dp,
                        bottomStart = 4.dp
                    ),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A1B35).copy(alpha = 0.88f)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Color(0xFF9575CD).copy(alpha = 0.6f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = tr("Привет! Я твой погодный гид"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            lineHeight = 20.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = tr("Это приложение создано для того, чтобы погода никогда не застала тебя врасплох. Здесь ты можешь в один клик узнать температуру по своей геолокации, собрать коллекцию любимых городов мира и получить умные советы, что надеть перед выходом на улицу!"),
                            fontSize = 13.5.sp,
                            lineHeight = 18.sp,
                            color = Color.White.copy(alpha = 0.92f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Нижняя часть: компактная кнопка повыше
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF6650A4),
                    contentColor = Color.White
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.8f))
            ) {
                Text(
                    text = tr("Начать пользоваться"),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}