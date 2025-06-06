package hung.deptrai.simplesudoku.viewmodel

import android.annotation.SuppressLint
import androidx.core.graphics.component1
import androidx.core.graphics.component2
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.Difficulty
import hung.deptrai.simplesudoku.common.GameStatus
import hung.deptrai.simplesudoku.common.SudokuGame
import hung.deptrai.simplesudoku.model.GameTimer
import hung.deptrai.simplesudoku.model.SudokuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SudokuViewModel @Inject constructor(
    private val repo: SudokuRepository,
    private val timer: GameTimer
) : ViewModel() {

    private val _internalState = MutableStateFlow(SudokuViewInternalState())
    val internalState: StateFlow<SudokuViewInternalState> = _internalState.asStateFlow()

    val uiState: StateFlow<SudokuUiState> = internalState.map { state ->
        SudokuUiState(
            cells = state.gameState.cells,
            errorCount = state.gameState.errorCount,
            maxErrors = state.gameState.maxErrors,
            timeElapsed = state.timerText,
            isGameCompleted = state.gameState.isGameCompleted,
            isGamePaused = state.gameState.isGamePaused,
            isGameFailed = state.gameState.isGameFailed,
            difficulty = state.gameState.difficulty,
            isNoteMode = state.isNoteMode
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SudokuUiState())

    init {
        viewModelScope.launch {
            repo.sudokuGame.collectLatest { game ->
                updateGameState(game)
            }
        }

        viewModelScope.launch {
            timer.accumulated.collectLatest { timeMillis ->
                _internalState.update {
                    it.copy(timerText = formatTimeElapsed(timeMillis / 1000))
                }
            }
        }
    }

    fun onPlayEvent(action: PlayAction) {
        when (action) {
            is PlayAction.CellSelect -> repo.selectCell(action.row, action.col)
            is PlayAction.CellFill -> withSelectedCell { row, col ->
                repo.makeMove(row, col, action.value)
            }
            is PlayAction.CellErase -> withSelectedCell { row, col ->
                repo.cellErase(row, col)
            }
            is PlayAction.RequestHint -> withSelectedCell { row, col ->
                repo.giveHint(row, col)
            }
            is PlayAction.CellNote -> withSelectedCell { row, col ->
                repo.toggleNote(row, col, action.value)
            }
            is PlayAction.RestartGame -> repo.resetGame()
            is PlayAction.PauseGame -> {
                val (pausedTime, _) = timer.pause()
                repo.pauseGame(pausedTime.toLong())
            }
            is PlayAction.ResumeGame -> {
                timer.resume()
                repo.resumeGame()
            }
            is PlayAction.ToggleNoteMode -> {
                toggleNoteMode()
            }
        }
    }

    fun onGameEvent(action: HomeAction) {
        if (action is HomeAction.onPlayGame) {
            startNewGame(action.diff)
        }
    }

    private fun toggleNoteMode() {
        _internalState.update { it.copy(isNoteMode = !it.isNoteMode) }
    }
    private fun startNewGame(difficulty: Difficulty) {
        _internalState.update { it.copy(timerText = "00:00") }
        repo.startNewGame(difficulty)
        timer.start()
    }

    private fun updateGameState(game: SudokuGame) {
        _internalState.update {
            it.copy(
                gameState = SudokuGameState(
                    cells = game.cells,
                    errorCount = game.errorCount,
                    maxErrors = game.maxErrors,
                    isGameCompleted = game.gameStatus == GameStatus.COMPLETED,
                    isGamePaused = game.gameStatus == GameStatus.PAUSED,
                    isGameFailed = game.gameStatus == GameStatus.FINISHED,
                    difficulty = game.difficulty
                ),
                selectedCell = repo.getSelectedCell()
            )
        }
    }

    private inline fun withSelectedCell(block: (Int, Int) -> Unit) {
        val (row, col, _) = _internalState.value.selectedCell
        if (row != -1 && col != -1) block(row, col)
    }
}

// ---------------------------
// Internal State Container
// ---------------------------
data class SudokuViewInternalState(
    val gameState: SudokuGameState = SudokuGameState(),
    val timerText: String = "00:00",
    val selectedCell: Triple<Int, Int, Int> = Triple(-1, -1, 0),
    val isNoteMode: Boolean = false
)

data class SudokuGameState(
    val cells: Array<Array<Cell>> = Array(9) { row ->
        Array(9) { col -> Cell(row, col, 0, false, true, null, false, false) }
    },
    val errorCount: Int = 0,
    val maxErrors: Int = 3,
    val isGameCompleted: Boolean = false,
    val isGamePaused: Boolean = false,
    val isGameFailed: Boolean = false,
    val difficulty: Difficulty = Difficulty.Beginner
)

data class SudokuUiState(
    val cells: Array<Array<Cell>> = Array(9) { row ->
        Array(9) { col -> Cell(row, col, 0, false, true, null, false, false) }
    },
    val errorCount: Int = 0,
    val maxErrors: Int = 3,
    val timeElapsed: String = "00:00",
    val isGameCompleted: Boolean = false,
    val isGamePaused: Boolean = false,
    val isGameFailed: Boolean = false,
    val difficulty: Difficulty = Difficulty.Beginner,
    val isNoteMode: Boolean = false
)

@SuppressLint("DefaultLocale")
fun formatTimeElapsed(timeElapsedSeconds: Long): String {
    val hours = timeElapsedSeconds / 3600
    val minutes = (timeElapsedSeconds % 3600) / 60
    val seconds = timeElapsedSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
