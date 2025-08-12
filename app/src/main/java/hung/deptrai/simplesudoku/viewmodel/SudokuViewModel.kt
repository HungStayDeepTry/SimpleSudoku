package hung.deptrai.simplesudoku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.Difficulty
import hung.deptrai.simplesudoku.common.GameStatus
import hung.deptrai.simplesudoku.common.SudokuGame
import hung.deptrai.simplesudoku.model.GameTimer
import hung.deptrai.simplesudoku.model.SudokuRepository
import hung.deptrai.simplesudoku.ui.component.util.formatTimeElapsed
import hung.deptrai.simplesudoku.ui.component.util.parseTimeElapsedFromText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SudokuViewModel @Inject constructor(
    private val repo: SudokuRepository,
    private val timer: GameTimer
) : ViewModel() {

    private val _uiState = MutableStateFlow(SudokuUiState())
    val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.sudokuGame.collectLatest { game ->
                updateGameState(game)
                if (game.gameStatus == GameStatus.FINISHED || game.gameStatus == GameStatus.COMPLETED) {
                    saveGame()
                }
            }
        }

        viewModelScope.launch {
            timer.accumulated.collectLatest { timeMillis ->
                _uiState.update {
                    it.copy(timerText = formatTimeElapsed(timeMillis / 1000))
                }
            }
        }

        viewModelScope.launch {
            repo.hasUnfinishedGame.collectLatest { hasGame ->
                _uiState.update { it.copy(hasUnfinishedGame = hasGame) }
            }
        }
    }

    fun onPlayEvent(action: PlayAction) {
        when (action) {
            is PlayAction.CellSelect -> repo.selectCell(action.row, action.col).also {
                _uiState.update { state ->
                    state.copy(
                        selectedCell = Triple(
                            action.row,
                            action.col,
                            0
                        )
                    )
                }
            }

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
                val pausedTime = timer.pause()
                repo.pauseGame(pausedTime.toLong())
                saveGame()
            }

            is PlayAction.ResumeGame -> {
                timer.resume()
                repo.resumeGame()
            }

            is PlayAction.ToggleNoteMode -> {
                _uiState.update { it.copy(isNoteMode = !it.isNoteMode) }
            }
        }
    }

    fun onGameEvent(action: HomeAction) {
        when (action) {
            is HomeAction.onPlayGame -> {
                startNewGame(action.diff)
            }

            HomeAction.onResumeGame -> {
                loadLastGame()
            }

            HomeAction.onDeleteExistedGame -> {
                repo.deleteExistedGame()
            }
        }
    }

    private fun startNewGame(difficulty: Difficulty) {
        _uiState.update {
            it.copy(timerText = "00:00", selectedCell = Triple(-1, -1, 0))
        }
        repo.startNewGame(difficulty)
        timer.start()
    }

    private fun updateGameState(game: SudokuGame) {
        _uiState.update {
            it.copy(
                cells = game.cells,
                errorCount = game.errorCount,
                maxErrors = game.maxErrors,
                isGameCompleted = game.gameStatus == GameStatus.COMPLETED,
                isGamePaused = game.gameStatus == GameStatus.PAUSED,
                isGameFailed = game.gameStatus == GameStatus.FINISHED,
                difficulty = game.difficulty,
                selectedCell = repo.getSelectedCell()
            )
        }
    }

    private inline fun withSelectedCell(block: (Int, Int) -> Unit) {
        val (row, col, _) = _uiState.value.selectedCell
        if (row != -1 && col != -1) block(row, col)
    }

    fun hasActiveInStore() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.hasActiveGameInStore()
        }
    }

    fun saveGame() {
        repo.saveCurrentGame()
    }

    fun loadLastGame() {
        viewModelScope.launch(Dispatchers.IO) {
            repo.loadLastUnfinishedGame()
            timer.startFromInitialTime(parseTimeElapsedFromText(_uiState.value.timerText))
            repo.resumeGame()
        }
    }
}

data class SudokuUiState(
    val cells: Array<Array<Cell>> = Array(9) { row ->
        Array(9) { col -> Cell(row, col, 0, false, true, null, false, false) }
    },
    val errorCount: Int = 0,
    val maxErrors: Int = 3,
    val timerText: String = "00:00",
    val isGameCompleted: Boolean = false,
    val isGamePaused: Boolean = false,
    val isGameFailed: Boolean = false,
    val difficulty: Difficulty = Difficulty.Beginner,
    val selectedCell: Triple<Int, Int, Int> = Triple(-1, -1, 0),
    val isNoteMode: Boolean = false,
    val hasUnfinishedGame: Boolean = false
)