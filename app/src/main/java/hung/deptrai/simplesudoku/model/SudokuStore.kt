package hung.deptrai.simplesudoku.model

import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.Difficulty
import hung.deptrai.simplesudoku.common.GameStatus
import hung.deptrai.simplesudoku.common.SudokuGame
import java.util.Stack
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SudokuStore @Inject constructor() {
    private val moveStack = Stack<Move>()
    private var currentGame: SudokuGame? = null
    private var selectedRow = -1
    private var selectedCol = -1

    data class Move(val row: Int, val col: Int, val oldUserValue: Int?)

    fun getGame(): SudokuGame? = currentGame

    fun startNewGame(difficulty: Difficulty) {
        val newCells = BoardGenerator.generatePuzzle(
            BoardGenerator.generateFullBoard(),
            difficulty
        )

        currentGame = SudokuGame(
            id = UUID.randomUUID().toString(),
            cells = newCells,
            difficulty = difficulty,
            gameStatus = GameStatus.ONGOING,
            errorCount = 0
        )

        moveStack.clear()
        selectedRow = -1
        selectedCol = -1
    }

    fun selectCell(row: Int, col: Int) {
        val game = currentGame ?: return
        selectedRow = row
        selectedCol = col

        val updatedCells = game.cells.mapIndexed { r, rowCells ->
            rowCells.mapIndexed { c, cell ->
                cell.copy(
                    isSelected = r == row && c == col,
                    isHighlighted = if (r == row && c == col) false
                    else isHighlighted(r, c, row, col)
                )
            }.toTypedArray()
        }.toTypedArray()

        currentGame = game.copy(cells = updatedCells)
    }

    private fun isHighlighted(row: Int, col: Int, selectedRow: Int, selectedCol: Int): Boolean {
        if (selectedRow == -1 || selectedCol == -1) return false
        if (row == selectedRow && col == selectedCol) return false
        return row == selectedRow || col == selectedCol || (row / 3 == selectedRow / 3 && col / 3 == selectedCol / 3)
    }

    fun getSelectedCell(): Triple<Int, Int, Int> {
        val game = currentGame ?: return Triple(-1, -1, 0)
        if (selectedRow == -1 || selectedCol == -1) return Triple(-1, -1, 0)
        val value = game.cells[selectedRow][selectedCol].value
        return Triple(selectedRow, selectedCol, value)
    }

    fun cellErase(row: Int, col: Int) {
        val game = currentGame ?: return
        if (game.gameStatus != GameStatus.ONGOING) return

        val cell = game.cells[row][col]
        if (!cell.isEditable) return

        val updatedCell = cell.copy(
            userValue = null,
            notes = List(9) { false }
        )

        val updatedCells = game.cells.mapIndexed { r, rowCells ->
            rowCells.mapIndexed { c, currentCell ->
                if (r == row && c == col) updatedCell else currentCell
            }.toTypedArray()
        }.toTypedArray()

        currentGame = game.copy(cells = updatedCells)
    }

    fun makeMove(row: Int, col: Int, value: Int?): Boolean {
        val game = currentGame ?: return false
        if (game.gameStatus != GameStatus.ONGOING) return false

        val cell = game.cells[row][col]
        if (!cell.isEditable) return false

        moveStack.push(Move(row, col, cell.userValue))

        val updatedCell = cell.copy(
            userValue = value,
            notes = List(9) { false }
        )

        val updatedCells = game.cells.mapIndexed { r, rowCells ->
            rowCells.mapIndexed { c, currentCell ->
                if (r == row && c == col) updatedCell else currentCell
            }.toTypedArray()
        }.toTypedArray()

        val isCorrect = cell.value == value
        val newErrorCount = if (isCorrect) game.errorCount else game.errorCount + 1
        val newGameStatus = when {
            isCorrect && isCompleted(game.copy(cells = updatedCells)) -> GameStatus.COMPLETED
            newErrorCount > game.maxErrors -> GameStatus.FINISHED
            else -> game.gameStatus
        }

        currentGame = game.copy(
            cells = updatedCells,
            errorCount = newErrorCount,
            gameStatus = newGameStatus
        )

        return isCorrect
    }

    fun toggleNote(row: Int, col: Int, number: Int) {
        val game = currentGame ?: return
        val cell = game.cells.getOrNull(row)?.getOrNull(col) ?: return
        if (!cell.isEditable || number !in 1..9 || cell.userValue != null) return

        val index = number - 1
        val updatedNotes = cell.notes.toMutableList().apply {
            this[index] = !this[index]
        }

        val updatedCell = cell.copy(notes = updatedNotes)

        val updatedCells = game.cells.mapIndexed { r, rowCells ->
            rowCells.mapIndexed { c, currentCell ->
                if (r == row && c == col) updatedCell else currentCell
            }.toTypedArray()
        }.toTypedArray()

        currentGame = game.copy(cells = updatedCells)
    }

    fun giveHint(row: Int, col: Int) {
        val game = currentGame ?: return
        if (game.gameStatus != GameStatus.ONGOING) return

        val cell = game.cells[row][col]
        if (!cell.isEditable) return

        val updatedCell = cell.copy(userValue = cell.value)

        val updatedCells = game.cells.mapIndexed { r, rowCells ->
            rowCells.mapIndexed { c, currentCell ->
                if (r == row && c == col) updatedCell else currentCell
            }.toTypedArray()
        }.toTypedArray()

        val newGameStatus = if (isCompleted(game.copy(cells = updatedCells))) {
            GameStatus.COMPLETED
        } else {
            game.gameStatus
        }

        currentGame = game.copy(
            cells = updatedCells,
            gameStatus = newGameStatus
        )
    }

    fun pauseGame(elapsedTime: Long) {
        val game = currentGame ?: return
        currentGame = game.copy(
            timeElapsed = elapsedTime,
            gameStatus = GameStatus.PAUSED
        )
    }

    fun resumeGame() {
        val game = currentGame ?: return
        if (game.gameStatus == GameStatus.PAUSED) {
            currentGame = game.copy(gameStatus = GameStatus.ONGOING)
        }
    }

    fun resetGame() {
        val game = currentGame ?: return
        startNewGame(game.difficulty)
    }

    fun getCell(row: Int, col: Int): Cell? {
        return currentGame?.cells?.getOrNull(row)?.getOrNull(col)
    }

    private fun isCompleted(game: SudokuGame): Boolean {
        return game.cells.all { row ->
            row.all { cell ->
                if (cell.isEditable) {
                    cell.userValue != null && cell.userValue == cell.value
                } else {
                    true
                }
            }
        }
    }

    fun loadGame(game: SudokuGame?) {
        moveStack.clear()
        selectedRow = -1
        selectedCol = -1

        currentGame = game?.copy(
            cells = Array(9) { row ->
                Array(9) { col ->
                    game.cells[row][col].copy()
                }
            }
        )
    }
}