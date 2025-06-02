package hung.deptrai.simplesudoku.model

import java.util.Stack

class SudokuGame(
    var id: String,
    var board: SudokuBoard,
    var timer: GameTimer,
    var difficult: Difficulty,
    var gameStatus: GameStatus = GameStatus.ONGOING
) {
    private val moveStack = Stack<Move>()

    data class Move(val row: Int, val col: Int, val oldValue: Int, val newValue: Int)

    fun startNewGame() {
        board = BoardGenerator.generatePuzzle(BoardGenerator.generateFullBoard(), difficult)
        timer.start()
        gameStatus = GameStatus.ONGOING
        moveStack.clear()
    }

    fun checkVictory(): Boolean {
        return board.isCompleted()
    }

    fun giveHint(row: Int, col: Int) {
        val cell = board.getCell(row, col)
        if (cell.isEditable && !cell.isVisible) {
            cell.isVisible = true
            // Có thể trừ điểm hoặc cập nhật trạng thái hint ở đây
        }
    }

    fun makeMove(row: Int, col: Int, value: Int): Boolean {
        val cell = board.getCell(row, col)
        if (cell.isEditable) {
            val oldValue = cell.value
            if (board.revealCellIfCorrect(row, col, value)) {
                moveStack.push(Move(row, col, oldValue, value))
                cell.value = value
                return true
            }
        }
        return false
    }

    fun undo() {
        if (moveStack.isNotEmpty()) {
            val lastMove = moveStack.pop()
            val cell = board.getCell(lastMove.row, lastMove.col)
            cell.value = lastMove.oldValue
            cell.isVisible = false
        }
    }

    fun resetGame() {
        startNewGame()
    }

    fun saveGame() {
        // TODO: serialize 'this' hoặc board + timer + gameStatus thành dữ liệu lưu vào Room
        // Ví dụ: chuyển board.cells thành JSON hoặc dạng chuỗi, lưu timer elapsed, status, id, difficult...
    }

    fun loadGame() {
        // TODO: đọc dữ liệu từ Room, deserialize thành đối tượng SudokuGame, gán lại các thuộc tính
    }
}
