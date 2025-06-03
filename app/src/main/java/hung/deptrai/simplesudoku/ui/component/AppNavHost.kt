package hung.deptrai.simplesudoku.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hung.deptrai.simplesudoku.viewmodel.SudokuViewModel

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val vm = hiltViewModel<SudokuViewModel>()
    val uiState by vm.uiState.collectAsState()
    val selectedCell by vm.selectedCell.collectAsState()
    NavHost(
        navController,
        startDestination = "splash"
    ) {
        composable("splash"){
            SplashScreen(navController)
        }
        composable("home"){
            HomeScreen({
                navController.navigate("playing")
            }) {

            }
        }
        composable("playing"){
            SudokuGameScreen(
                modifier = Modifier.padding(),
                uiState = uiState,
                onAction = {
                    vm.onPlayEvent(it)
                },
                selectedCell = selectedCell
            )
        }
    }
}