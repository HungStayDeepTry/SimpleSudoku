package hung.deptrai.simplesudoku.model

import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.Difficulty
import hung.deptrai.simplesudoku.common.GameStatus
import hung.deptrai.simplesudoku.common.SudokuGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class SudokuRepository @Inject constructor(
    private val store: SudokuStore
){
    private val _sudokuGame = MutableSharedFlow<SudokuGame>()
    val sudokuGame = _sudokuGame.asSharedFlow()

    private val cells: Array<Array<Cell>> = Array(9) { row ->
        Array(9) { col ->
            Cell(
                row = row,
                col = col,
                value = 0, // hoặc có thể random/đặt sẵn 1 số
                isVisible = false,
                isEditable = false
            )
        }
    }
    private val game = SudokuGame(id = "", gameStatus = GameStatus.ONGOING, cells = cells, maxErrors = 0, errorCount = 0, difficulty = Difficulty.Advanced, timeElapsed = "")

    private val scope = CoroutineScope(Dispatchers.Default)

    fun startNewGame(difficulty: Difficulty) {
        store.startNewGame(difficulty)
        updateGameFromStore()
    }

    fun makeMove(row: Int, col: Int, value: Int) {
        store.makeMove(row, col, value)
        updateGameFromStore()
    }

    fun giveHint(row: Int, col: Int) {
        store.giveHint(row, col)
        updateGameFromStore()
    }

    fun undo() {
        store.undo()
        updateGameFromStore()
    }

    fun pauseGame() {
        store.pauseGame()
        updateGameFromStore()
    }

    fun resumeGame() {
        store.resumeGame()
        updateGameFromStore()
    }

    fun resetGame() {
        store.resetGame()
        updateGameFromStore()
    }

    fun loadGame(gameId: String) {
        val loadedGame = store.loadGame(gameId)
        if (loadedGame != null) {
            copyGameData(loadedGame)
            emitGame()
        }
    }

    fun getAllGames(): List<SudokuGame> {
        return store.getAllGames()
    }

    private fun updateGameFromStore() {
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
            }
        }
    }

    private fun emitGame() {
        scope.launch {
            _sudokuGame.emit(game.copy(cells = game.cells.map { it.copyOf() }.toTypedArray()))
        }
    }
}