package hung.deptrai.simplesudoku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.Difficulty
import hung.deptrai.simplesudoku.common.GameStatus
import hung.deptrai.simplesudoku.common.SudokuGame
import hung.deptrai.simplesudoku.model.SudokuRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SudokuViewModel @Inject constructor(
    private val repo: SudokuRepository
) : ViewModel() {

    private val _selectedCell = MutableStateFlow(Triple(-1, -1, 0))
    val selectedCell: StateFlow<Triple<Int, Int, Int>> = _selectedCell.asStateFlow()

    private val _uiState = MutableStateFlow(SudokuUiState())
    val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.sudokuGame.collectLatest { game ->
                updateUiState(game)
                // Cập nhật selected cell từ repo
                _selectedCell.value = repo.getSelectedCell()
            }
        }
    }

    fun onPlayEvent(action: PlayAction) {
        when (action) {
            is PlayAction.CellSelect -> {
                repo.selectCell(action.row, action.col)
            }
            is PlayAction.CellFill -> {
                val (row, col, _) = _selectedCell.value
                if (row != -1 && col != -1) {
                    repo.makeMove(row, col, action.value)
                }
            }
            is PlayAction.CellErase -> {
                val (row, col, _) = _selectedCell.value
                if (row != -1 && col != -1) {
                    repo.makeMove(row, col, 0)
                }
            }
            is PlayAction.RequestHint -> {
                val (row, col, _) = _selectedCell.value
                if (row != -1 && col != -1) repo.giveHint(row, col)
            }
            is PlayAction.RestartGame -> repo.resetGame()
            else -> {}
        }
    }

    fun onGameEvent(action: HomeAction){
        when(action){
            is HomeAction.onPlayGame -> startNewGame(action.diff)
        }
    }

    private fun startNewGame(difficulty: Difficulty){
        repo.startNewGame(difficulty)
    }

    fun onCellClick(row: Int, col: Int) {
        repo.selectCell(row, col)
    }

    fun onNumberInput(number: Int) {
        val (row, col, _) = _selectedCell.value
        if (row != -1 && col != -1) {
            repo.makeMove(row, col, number)
        }
    }

    fun onEraseClick() {
        val (row, col, _) = _selectedCell.value
        if (row != -1 && col != -1) {
            repo.makeMove(row, col, 0)
        }
    }

    private fun updateUiState(game: SudokuGame) {
        _uiState.value = _uiState.value.copy(
            cells = game.cells,
            errorCount = game.errorCount,
            maxErrors = game.maxErrors,
            timeElapsed = formatTimeElapsed(game.timeElapsed),
            isGameCompleted = game.gameStatus == GameStatus.COMPLETED,
            isGamePaused = game.gameStatus == GameStatus.PAUSED,
            isGameFailed = game.gameStatus == GameStatus.FINISHED
        )
    }
}

// SudokuUiState giữ nguyên
data class SudokuUiState(
    val cells: Array<Array<Cell>> = Array(9) { row ->
        Array(9) { col ->
            Cell(row, col, 0, false, true, null, false, false)
        }
    },
    val errorCount: Int = 0,
    val maxErrors: Int = 3,
    val timeElapsed: String = "00:00",
    val isGameCompleted: Boolean = false,
    val isGamePaused: Boolean = false,
    val isGameFailed: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SudokuUiState

        if (!cells.contentDeepEquals(other.cells)) return false
        if (errorCount != other.errorCount) return false
        if (maxErrors != other.maxErrors) return false
        if (timeElapsed != other.timeElapsed) return false
        if (isGameCompleted != other.isGameCompleted) return false
        if (isGamePaused != other.isGamePaused) return false
        if (isGameFailed != other.isGameFailed) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cells.contentDeepHashCode()
        result = 31 * result + errorCount
        result = 31 * result + maxErrors
        result = 31 * result + timeElapsed.hashCode()
        result = 31 * result + isGameCompleted.hashCode()
        result = 31 * result + isGamePaused.hashCode()
        result = 31 * result + isGameFailed.hashCode()
        return result
    }
}
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