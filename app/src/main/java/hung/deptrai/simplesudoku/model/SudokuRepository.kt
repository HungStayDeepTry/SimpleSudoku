package hung.deptrai.simplesudoku.model

import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.Difficulty
import hung.deptrai.simplesudoku.common.GameStatus
import hung.deptrai.simplesudoku.common.SudokuGame
import hung.deptrai.simplesudoku.model.SudokuMapper.toEntity
import hung.deptrai.simplesudoku.model.SudokuMapper.toModel
import hung.deptrai.simplesudoku.model.room.dao.SudokuDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class SudokuRepository @Inject constructor(
    private val store: SudokuStore,
    private val dao: SudokuDao
) {
    private val _sudokuGame = MutableSharedFlow<SudokuGame>()
    val sudokuGame = _sudokuGame.asSharedFlow()

    private val _hasUnfinishedGame = MutableStateFlow(false)
    val hasUnfinishedGame = _hasUnfinishedGame.asStateFlow()

    private val cells: Array<Array<Cell>> = Array(9) { row ->
        Array(9) { col ->
            Cell(
                row = row,
                col = col,
                value = 0,
                isVisible = false,
                isEditable = false,
                userValue = null,
                isSelected = false,
                isHighlighted = false
            )
        }
    }
    private val game = SudokuGame(
        id = "",
        gameStatus = GameStatus.ONGOING,
        cells = cells,
        maxErrors = 0,
        errorCount = 0,
        difficulty = Difficulty.Advanced,
        timeElapsed = 0L
    )

    private val scope = CoroutineScope(Dispatchers.Default)


    fun startNewGame(difficulty: Difficulty) {
        store.startNewGame(difficulty)
        scope.launch {
            updateGameFromStore()
        }
    }

    fun selectCell(row: Int, col: Int) {
        store.selectCell(row, col)
        scope.launch {
            updateGameFromStore()
        }
    }

    fun getSelectedCell(): Triple<Int, Int, Int> {
        return store.getSelectedCell()
    }

    fun cellErase(row: Int, col: Int) {
        store.cellErase(row, col)
        scope.launch {
            updateGameFromStore()
        }
    }

    fun makeMove(row: Int, col: Int, value: Int?) {
        store.makeMove(row, col, value)
        scope.launch {
            updateGameFromStore()
        }
    }

    fun giveHint(row: Int, col: Int) {
        store.giveHint(row, col)
        scope.launch {
            updateGameFromStore()
        }
    }

    fun pauseGame(elapsedTime: Long) {
        store.pauseGame(elapsedTime)
        scope.launch {
            updateGameFromStore()
        }
    }

    fun resumeGame() {
        store.resumeGame()
        scope.launch {
            updateGameFromStore()
        }
    }

    fun resetGame() {
        store.resetGame()
        scope.launch {
            updateGameFromStore()
        }
    }

    fun toggleNote(row: Int, col: Int, value: Int) {
        store.toggleNote(row, col, value)
        scope.launch {
            updateGameFromStore()
        }
    }

    suspend fun loadLastUnfinishedGame() {
        val entity = dao.getLastUnfinishedGame() ?: return
        store.loadGame(entity.toModel())
        updateGameFromStore()
    }


    private suspend fun updateGameFromStore() {
        val current = store.getGame() ?: return
        copyGameData(current)
        emitGame()
    }

    private fun copyGameData(source: SudokuGame) {
        game.id = source.id
        game.gameStatus = source.gameStatus
        game.errorCount = source.errorCount
        game.maxErrors = source.maxErrors
        game.difficulty = source.difficulty
        game.timeElapsed = source.timeElapsed

        for (row in 0..8) {
            for (col in 0..8) {
                val sourceCell = source.cells[row][col]
                val targetCell = game.cells[row][col]
                targetCell.value = sourceCell.value
                targetCell.isVisible = sourceCell.isVisible
                targetCell.isEditable = sourceCell.isEditable
                targetCell.userValue = sourceCell.userValue
                targetCell.isSelected = sourceCell.isSelected
                targetCell.isHighlighted = sourceCell.isHighlighted
                targetCell.notes = sourceCell.notes
            }
        }
    }

    private suspend fun emitGame() {
        val copiedCells = Array(9) { row ->
            Array(9) { col ->
                val original = game.cells[row][col]
                original.copy()
            }
        }
        _sudokuGame.emit(game.copy(cells = copiedCells))
    }

    fun saveCurrentGame() {
        store.getGame()?.let { currentGame ->
            scope.launch(Dispatchers.IO) {
                dao.saveGame(currentGame.toEntity())
            }
        }
    }

    fun deleteExistedGame() {
        scope.launch(Dispatchers.IO) {
            val entity = dao.getLastUnfinishedGame()
            if (entity != null)
                dao.saveGame(entity.copy(gameStatus = GameStatus.FINISHED))
        }
    }

    suspend fun hasActiveGameInStore() {
        val count = dao.countUnfinishedGames()
        _hasUnfinishedGame.update { count > 0 }
    }
}
