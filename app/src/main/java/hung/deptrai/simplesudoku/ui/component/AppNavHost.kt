package hung.deptrai.simplesudoku.ui.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    NavHost(
        navController,
        startDestination = "splash"
    ) {
        composable("splash"){

        }
        composable("home"){

        }
        composable("playing"){

        }
    }
}