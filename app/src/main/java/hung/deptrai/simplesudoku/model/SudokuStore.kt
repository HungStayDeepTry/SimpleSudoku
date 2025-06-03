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
    private val gameTimer: GameTimer
) {
    private val moveStack = Stack<Move>()
    private var currentGame: SudokuGame? = null

    data class Move(val row: Int, val col: Int, val oldVisible: Boolean)

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
        gameTimer.start()
    }

    fun makeMove(row: Int, col: Int, value: Int): Boolean {
        val game = currentGame ?: return false
        if (game.gameStatus != GameStatus.ONGOING) return false

        val cell = game.cells[row][col]
        if (!cell.isEditable || cell.isVisible) return false

        return if (cell.value == value) {
            moveStack.push(Move(row, col, cell.isVisible))
            cell.isVisible = true

            game.timeElapsed = gameTimer.getCurrentTimeString()

            if (isCompleted(game)) {
                game.gameStatus = GameStatus.COMPLETED
                game.timeElapsed = gameTimer.pause()
            }

            true
        } else {
            game.errorCount += 1
            game.timeElapsed = gameTimer.getCurrentTimeString()

            if (game.errorCount > game.maxErrors) {
                game.gameStatus = GameStatus.FINISHED
                game.timeElapsed = gameTimer.pause()
            }

            false
        }
    }

    fun giveHint(row: Int, col: Int) {
        val game = currentGame ?: return
        if (game.gameStatus != GameStatus.ONGOING) return

        val cell = game.cells[row][col]
        if (cell.isEditable && !cell.isVisible) {
            moveStack.push(Move(row, col, cell.isVisible))
            cell.isVisible = true
            game.timeElapsed = gameTimer.getCurrentTimeString()
        }
    }

    fun undo() {
        val game = currentGame ?: return
        if (game.gameStatus != GameStatus.ONGOING || moveStack.isEmpty()) return

        val lastMove = moveStack.pop()
        val cell = game.cells[lastMove.row][lastMove.col]
        cell.isVisible = lastMove.oldVisible
        game.timeElapsed = gameTimer.getCurrentTimeString()
    }

    fun pauseGame() {
        val game = currentGame ?: return
        game.timeElapsed = gameTimer.pause()
        game.gameStatus = GameStatus.PAUSED
    }

    fun resumeGame() {
        val game = currentGame ?: return
        if (game.gameStatus == GameStatus.PAUSED) {
            gameTimer.resume()
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

    fun isCorrectInput(row: Int, col: Int, inputValue: Int): Boolean {
        return getCell(row, col)?.value == inputValue
    }

    fun canMakeMove(): Boolean {
        return currentGame?.gameStatus == GameStatus.ONGOING
    }

    fun getRemainingErrors(): Int {
        val game = currentGame ?: return 0
        return game.maxErrors - game.errorCount + 1
    }

    private fun isCompleted(game: SudokuGame): Boolean {
        return game.cells.all { row -> row.all { cell -> cell.isVisible } }
    }

    fun saveGame(game: SudokuGame) {
        // TODO: Lưu vào Room
    }

    fun loadGame(gameId: String): SudokuGame? {
        // TODO: Load từ Room
        return null
    }

    fun getAllGames(): List<SudokuGame> {
        // TODO: Lấy toàn bộ game từ Room
        return emptyList()
    }
}