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
class SudokuStore @Inject constructor(
) {
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
        clearAllCellStates()
        selectedRow = row
        selectedCol = col
        game.cells[row][col].isSelected = true
        updateCellHighlighting(row, col)
    }

    private fun clearAllCellStates() {
        val game = currentGame ?: return
        for (row in 0..8) {
            for (col in 0..8) {
                game.cells[row][col].isSelected = false
                game.cells[row][col].isHighlighted = false
            }
        }
    }

    private fun updateCellHighlighting(selectedRow: Int, selectedCol: Int) {
        val game = currentGame ?: return
        for (row in 0..8) {
            for (col in 0..8) {
                if (row != selectedRow || col != selectedCol) {
                    game.cells[row][col].isHighlighted =
                        isHighlighted(row, col, selectedRow, selectedCol)
                }
            }
        }
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
        cell.userValue = null
        cell.notes = MutableList(9) { false }
    }

    fun makeMove(row: Int, col: Int, value: Int?): Boolean {
        val game = currentGame ?: return false
        if (game.gameStatus != GameStatus.ONGOING) return false

        val cell = game.cells[row][col]
        if (!cell.isEditable) return false

        cell.notes = MutableList(9) { false }

        moveStack.push(Move(row, col, cell.userValue))

        cell.userValue = value

        if (cell.value == value) {
            if (isCompleted(game)) {
                game.gameStatus = GameStatus.COMPLETED
            }
            return true
        } else {
            game.errorCount += 1
            if (game.errorCount > game.maxErrors) {
                game.gameStatus = GameStatus.FINISHED
            }
            return false
        }
    }

    fun toggleNote(row: Int, col: Int, number: Int) {
        val cell = getCell(row, col) ?: return
        if (!cell.isEditable || number !in 1..9) return

        if (cell.userValue != null) return

        val index = number - 1
        cell.notes[index] = !cell.notes[index]
    }

    fun giveHint(row: Int, col: Int) {
        val game = currentGame ?: return
        if (game.gameStatus != GameStatus.ONGOING) return

        val cell = game.cells[row][col]
        if (cell.isEditable) {
            cell.userValue = cell.value

            if (isCompleted(game)) {
                game.gameStatus = GameStatus.COMPLETED
            }
        }
    }

    fun pauseGame(elapsedTime: Long) {
        val game = currentGame ?: return
        game.timeElapsed = elapsedTime
        game.gameStatus = GameStatus.PAUSED
    }

    fun resumeGame() {
        val game = currentGame ?: return
        if (game.gameStatus == GameStatus.PAUSED) {
            game.gameStatus = GameStatus.ONGOING
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
                    game.cells[row][col].copy(
                        notes = game.cells[row][col].notes.toMutableList()
                    )
                }
            }
        )
    }
}