package hung.deptrai.simplesudoku.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import hung.deptrai.simplesudoku.common.Difficulty
import hung.deptrai.simplesudoku.ui.component.util.parseTimeElapsedFromText
import hung.deptrai.simplesudoku.viewmodel.HomeAction
import hung.deptrai.simplesudoku.viewmodel.PlayAction
import hung.deptrai.simplesudoku.viewmodel.SudokuViewModel
import kotlinx.coroutines.launch

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val vm = hiltViewModel<SudokuViewModel>()
    val uiState by vm.uiState.collectAsState()
    val selectedCell by vm.uiState.collectAsState()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    var previousRoute by remember { mutableStateOf("") }

    LaunchedEffect(navBackStackEntry) {
        vm.hasActiveInStore()
    }

    LaunchedEffect(navBackStackEntry) {
        val currentRoute = navBackStackEntry?.destination?.route ?: ""

        if (previousRoute == "playing" && currentRoute == "home") {
            vm.onPlayEvent(PlayAction.PauseGame(parseTimeElapsedFromText(uiState.timerText)))
        }

        previousRoute = currentRoute

    }

    NavHost(
        navController,
        startDestination = "splash",
        modifier = modifier
    ) {
        composable("splash") {
            SplashScreen(navController)
        }
        composable("home") {
            val coroutineScope = rememberCoroutineScope()
            HomeScreen(
                onDifficultySelected = { difficulty ->
                    coroutineScope.launch {
                        vm.onGameEvent(HomeAction.onPlayGame(difficulty))
                        navController.navigate("playing")
                    }
                },
                onResumeGame = {
                    coroutineScope.launch {
                        vm.onGameEvent(HomeAction.onResumeGame)
                        navController.navigate("playing")
                    }
                },
                onDeleteExistedGame = {
                    coroutineScope.launch {
                        vm.onGameEvent(HomeAction.onDeleteExistedGame)
                    }
                },
                uiState = uiState
            )
        }
        composable("playing") {
            SudokuGameScreen(
                modifier = Modifier.padding(),
                uiState = uiState,
                onAction = {
                    vm.onPlayEvent(it)
                },
                selectedCell = selectedCell.selectedCell,
                onGameEvent = {
                    vm.onGameEvent(it)
                },
                onExit = {
                    vm.saveGame()
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
