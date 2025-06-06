package hung.deptrai.simplesudoku.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import hung.deptrai.simplesudoku.common.Difficulty
import hung.deptrai.simplesudoku.viewmodel.SudokuViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val vm = hiltViewModel<SudokuViewModel>()
    val uiState by vm.uiState.collectAsState()
    val selectedCell by vm.internalState.collectAsState()
    NavHost(
        navController,
        startDestination = "splash"
    ) {
        composable("splash"){
            SplashScreen(navController)
        }
        composable("home"){
            val coroutineScope = rememberCoroutineScope()
            HomeScreen(
                onGameEvent = {
                    coroutineScope.launch {
                        navController.navigate("playing") {
                            popUpTo("playing") { inclusive = true }
                            launchSingleTop = true // đảm bảo không đẩy bản sao nếu đang ở "playing"
                        }
                        vm.onGameEvent(it)
                    }
                }
            )
        }
        composable("playing"){
            SudokuGameScreen(
                modifier = Modifier.padding(),
                uiState = uiState,
                onAction = {
                    vm.onPlayEvent(it)
                },
                selectedCell = selectedCell.selectedCell,
                onGameEvent = {
                    vm.onGameEvent(it)
                }
            )
        }
    }
}

@Composable
fun DifficultyLauncher(
    triggerDialog: Boolean,
    onDismiss: () -> Unit,
    onDifficultySelected: (Difficulty) -> Unit
) {
    if (triggerDialog) {
        DifficultyDialog(
            onDismiss = onDismiss,
            onConfirm = { difficulty ->
                onDismiss()
                onDifficultySelected(difficulty)
            }
        )
    }
}
