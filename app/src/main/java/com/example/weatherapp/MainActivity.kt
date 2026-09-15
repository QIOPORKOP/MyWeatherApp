package com.example.weatherapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.ui.theme.WeatherAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WeatherMainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherMainScreen(modifier: Modifier = Modifier) {
    // Column - это колонка, которая выстраивает элементы друг под другом
    Column(
        modifier = modifier
            .fillMaxSize() // Занимаем весь экран
            .padding(16.dp), // Отступы от краев
        horizontalAlignment = Alignment.CenterHorizontally, // Выравниваем всё по центру
        verticalArrangement = Arrangement.Center
    ) {
        // Название города
        Text(
            text = "Актобе",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp)) // Пустое пространство (отступ)

        // Заглушка под будущую красивую Lottie-анимацию
        Text(
            text = "☀️",
            fontSize = 100.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Температура
        Text(
            text = "+25°C",
            fontSize = 64.sp,
            fontWeight = FontWeight.Light
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Описание погоды
        Text(
            text = "Ясно",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Эта функция нужна только для того, чтобы видеть результат справа в редакторе (Preview)
@Preview(showBackground = true)
@Composable
fun WeatherMainScreenPreview() {
    WeatherAppTheme {
        WeatherMainScreen()
    }
}
