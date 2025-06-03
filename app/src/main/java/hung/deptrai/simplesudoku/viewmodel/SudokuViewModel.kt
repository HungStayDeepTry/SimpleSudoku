package hung.deptrai.simplesudoku.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
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

    // Triple<row, col, value>
    private val _selectedCell = MutableStateFlow(Triple(-1, -1, 0))
    val selectedCell: StateFlow<Triple<Int, Int, Int>> = _selectedCell.asStateFlow()

    private val _game = MutableStateFlow<SudokuGame?>(null)
    val game: StateFlow<SudokuGame?> = _game.asStateFlow()

    private val _uiState = MutableStateFlow(SudokuUiState())
    val uiState: StateFlow<SudokuUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repo.sudokuGame.collectLatest { game ->
                _game.value = game
                updateUiState(game)
            }
        }
    }

    fun onPlayEvent(action: PlayAction) {
        when (action) {
            is PlayAction.CellSelect -> selectCell(action.row, action.col)
            is PlayAction.CellFill -> fillSelectedCell(action.value)
            is PlayAction.CellErase -> fillSelectedCell(0)
            is PlayAction.RequestHint -> {
                val (row, col, _) = _selectedCell.value
                if (row != -1 && col != -1) repo.giveHint(row, col)
            }
            is PlayAction.RestartGame -> repo.resetGame()
            else -> {}
        }
    }

    fun onCellClick(row: Int, col: Int) {
        selectCell(row, col)
    }

    fun onNumberInput(number: Int) {
        fillSelectedCell(number)
    }

    fun onEraseClick() {
        fillSelectedCell(0)
    }

    private fun selectCell(row: Int, col: Int) {
        val value = _game.value?.cells?.getOrNull(row)?.getOrNull(col)?.value ?: 0
        _selectedCell.value = Triple(row, col, value)
        updateUiState(_game.value ?: return)
    }

    private fun fillSelectedCell(value: Int) {
        val (row, col, _) = _selectedCell.value
        if (row != -1 && col != -1) {
            repo.makeMove(row, col, value)
            _selectedCell.value = Triple(row, col, value)
        }
    }

    private fun updateUiState(game: SudokuGame) {
        val (selectedRow, selectedCol, _) = _selectedCell.value
        val cellStates = Array(9) { row ->
            Array(9) { col ->
                val cell = game.cells[row][col]
                CellUIState(
                    row = row,
                    col = col,
                    value = cell.value,
                    isVisible = cell.isVisible,
                    isEditable = cell.isEditable,
                    isSelected = selectedRow == row && selectedCol == col,
                    isHighlighted = isHighlighted(row, col, selectedRow, selectedCol)
                )
            }
        }

        _uiState.value = _uiState.value.copy(
            cellStates = cellStates,
            errorCount = game.errorCount,
            maxErrors = game.maxErrors,
            timeElapsed = game.timeElapsed
        )
    }

    private fun isHighlighted(row: Int, col: Int, selectedRow: Int, selectedCol: Int): Boolean {
        if (selectedRow == -1 || selectedCol == -1) return false
        if (row == selectedRow && col == selectedCol) return false
        return row == selectedRow || col == selectedCol || (row / 3 == selectedRow / 3 && col / 3 == selectedCol / 3)
    }
}

data class SudokuUiState(
    val cellStates: Array<Array<CellUIState>> = Array(9) { row ->
        Array(9) { col ->
            CellUIState(row, col, 0, false, true, false, false)
        }
    },
    val errorCount: Int = 0,
    val maxErrors: Int = 3,
    val timeElapsed: String = "00:00",
    val isGameCompleted: Boolean = false,
    val isGamePaused: Boolean = false
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SudokuUiState

        if (!cellStates.contentDeepEquals(other.cellStates)) return false
        if (errorCount != other.errorCount) return false
        if (maxErrors != other.maxErrors) return false
        if (timeElapsed != other.timeElapsed) return false
        if (isGameCompleted != other.isGameCompleted) return false
        if (isGamePaused != other.isGamePaused) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cellStates.contentDeepHashCode()
        result = 31 * result + errorCount
        result = 31 * result + maxErrors
        result = 31 * result + timeElapsed.hashCode()
        result = 31 * result + isGameCompleted.hashCode()
        result = 31 * result + isGamePaused.hashCode()
        return result
    }
}

data class CellUIState(
    val row: Int,
    val col: Int,
    val value: Int,
    val isVisible: Boolean,
    val isEditable: Boolean,
    val isSelected: Boolean = false,
    val isHighlighted: Boolean = false
)