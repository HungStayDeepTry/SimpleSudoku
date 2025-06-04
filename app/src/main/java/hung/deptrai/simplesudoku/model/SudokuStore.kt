package hung.deptrai.simplesudoku.model

import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.Difficulty
import hung.deptrai.simplesudoku.common.GameStatus
import hung.deptrai.simplesudoku.common.SudokuGame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Stack
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SudokuStore @Inject constructor(
//    private val gameTimer: GameTimer
) {
    private val moveStack = Stack<Move>()
    private var currentGame: SudokuGame? = null
    private var selectedRow = -1
    private var selectedCol = -1

    // Flow để emit elapsed time liên tục
    private val _elapsedTime = MutableSharedFlow<String>()
    val elapsedTime = _elapsedTime.asSharedFlow()

    // Job để theo dõi timer
    private var timeUpdateJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

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
//        gameTimer.start()

        // Bắt đầu emit elapsed time liên tục
        startElapsedTimeUpdates()
    }

    private fun startElapsedTimeUpdates() {
        timeUpdateJob?.cancel()
        timeUpdateJob = scope.launch {
            while (isActive && currentGame?.gameStatus == GameStatus.ONGOING) {
//                val currentTime = gameTimer.getCurrentTimeString()
//                currentGame?.timeElapsed = currentTime
//                _elapsedTime.emit(currentTime)
//                delay(1000L) // Emit mỗi giây
            }
        }
    }

    private fun stopElapsedTimeUpdates() {
        timeUpdateJob?.cancel()
    }

    fun selectCell(row: Int, col: Int) {
        val game = currentGame ?: return
        clearAllCellStates()
        selectedRow = row
        selectedCol = col
        game.cells[row][col].isSelected = true
        updateCellHighlighting(row, col)

        // Cập nhật thời gian ngay lập tức
        updateElapsedTime()
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
                    game.cells[row][col].isHighlighted = isHighlighted(row, col, selectedRow, selectedCol)
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

    fun makeMove(row: Int, col: Int, value: Int): Boolean {
        val game = currentGame ?: return false
        if (game.gameStatus != GameStatus.ONGOING) return false

        val cell = game.cells[row][col]
        if (!cell.isEditable) return false

        // Lưu move vào stack
        moveStack.push(Move(row, col, cell.userValue))

        cell.userValue = value
        updateElapsedTime()

        if (cell.value == value) {
            if (isCompleted(game)) {
                game.gameStatus = GameStatus.COMPLETED
//                game.timeElapsed = gameTimer.pause()
                stopElapsedTimeUpdates()
                // Emit final time
//                scope.launch { _elapsedTime.emit(game.timeElapsed) }
            }
            return true
        } else {
            game.errorCount += 1
            if (game.errorCount > game.maxErrors) {
                game.gameStatus = GameStatus.FINISHED
//                game.timeElapsed = gameTimer.pause()
                stopElapsedTimeUpdates()
                // Emit final time
//                scope.launch { _elapsedTime.emit(game.timeElapsed) }
            }
            return false
        }
    }

    fun giveHint(row: Int, col: Int) {
        val game = currentGame ?: return
        if (game.gameStatus != GameStatus.ONGOING) return

        val cell = game.cells[row][col]
        if (cell.isEditable) {
            cell.userValue = cell.value
            updateElapsedTime()

            if (isCompleted(game)) {
                game.gameStatus = GameStatus.COMPLETED
//                game.timeElapsed = gameTimer.pause()
                stopElapsedTimeUpdates()
//                scope.launch { _elapsedTime.emit(game.timeElapsed) }
            }
        }
    }

    fun undo() {
        val game = currentGame ?: return
        if (game.gameStatus != GameStatus.ONGOING || moveStack.isEmpty()) return

        val lastMove = moveStack.pop()
        val cell = game.cells[lastMove.row][lastMove.col]
        cell.userValue = lastMove.oldUserValue
        updateElapsedTime()
    }

    fun pauseGame() {
        val game = currentGame ?: return
//        game.timeElapsed = gameTimer.pause()
        game.gameStatus = GameStatus.PAUSED
        stopElapsedTimeUpdates()
        // Emit paused time
//        scope.launch { _elapsedTime.emit(game.timeElapsed) }
    }

    fun resumeGame() {
        val game = currentGame ?: return
        if (game.gameStatus == GameStatus.PAUSED) {
//            gameTimer.resume()
            game.gameStatus = GameStatus.ONGOING
            startElapsedTimeUpdates()
        }
    }

    fun resetGame() {
        val game = currentGame ?: return
        stopElapsedTimeUpdates()
        startNewGame(game.difficulty)
    }

    private fun updateElapsedTime() {
//        val currentTime = gameTimer.getCurrentTimeString()
//        currentGame?.timeElapsed = currentTime
//        scope.launch { _elapsedTime.emit(currentTime) }
    }

    fun getCell(row: Int, col: Int): Cell? {
        return currentGame?.cells?.getOrNull(row)?.getOrNull(col)
    }

    fun isCorrectInput(row: Int, col: Int, inputValue: Int): Boolean {
        return getCell(row, col)?.value == inputValue
    }

    fun getRemainingErrors(): Int {
        val game = currentGame ?: return 0
        return game.maxErrors - game.errorCount + 1
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

    // Clean up khi không dùng nữa
    fun cleanup() {
        timeUpdateJob?.cancel()
//        gameTimer.reset()
    }
}