package hung.deptrai.simplesudoku.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import hung.deptrai.simplesudoku.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
        delay(2000)
        navController.navigate("home") {
            popUpTo("splash") {
                inclusive = true
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.8f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing), label = ""
    )

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 300), label = ""
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.sudoku_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(200.dp)
                .scale(scale)
                .alpha(alpha)
        )
    }
}
