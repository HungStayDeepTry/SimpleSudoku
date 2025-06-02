package hung.deptrai.simplesudoku.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import hung.deptrai.simplesudoku.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController
) {
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate("home"){
            popUpTo("splash"){
                inclusive = true
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF6200EA)), // Màu nền splash
        contentAlignment = Alignment.Center
    ) {
        // Logo ứng dụng - thay bằng resource của bạn
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "App Logo",
            modifier = Modifier.size(200.dp) // Kích thước logo
        )
    }
}