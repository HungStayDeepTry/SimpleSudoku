package hung.deptrai.simplesudoku.model

import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.Difficulty

object BoardGenerator {
    private const val BOARD_SIZE = 9

    // Kiểm tra an toàn để đặt num tại vị trí (row, col)
    private fun isSafe(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
        // Kiểm tra hàng
        for (x in 0 until BOARD_SIZE) {
            if (board[row][x] == num) return false
        }
        // Kiểm tra cột
        for (x in 0 until BOARD_SIZE) {
            if (board[x][col] == num) return false
        }
        // Kiểm tra vùng 3x3
        val startRow = row - row % 3
        val startCol = col - col % 3
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[startRow + i][startCol + j] == num) return false
            }
        }
        return true
    }

    // Giải board Sudoku bằng backtracking
    private fun solveBoard(board: Array<IntArray>): Boolean {
        for (row in 0 until BOARD_SIZE) {
            for (col in 0 until BOARD_SIZE) {
                if (board[row][col] == 0) {
                    val numbers = (1..9).shuffled() // random thứ tự để board đa dạng hơn
                    for (num in numbers) {
                        if (isSafe(board, row, col, num)) {
                            board[row][col] = num
                            if (solveBoard(board)) return true
                            board[row][col] = 0
                        }
                    }
                    return false
                }
            }
        }
        return true
    }

    // Đếm số lời giải có thể có cho board (dùng để kiểm tra unique solution)
    private fun countSolutions(board: Array<IntArray>): Int {
        var count = 0

        fun solve(board: Array<IntArray>) {
            if (count > 1) return

            for (row in 0 until BOARD_SIZE) {
                for (col in 0 until BOARD_SIZE) {
                    if (board[row][col] == 0) {
                        for (num in 1..9) {
                            if (isSafe(board, row, col, num)) {
                                board[row][col] = num
                                solve(board)
                                board[row][col] = 0
                                if (count > 1) return
                            }
                        }
                        return
                    }
                }
            }
            count++ // Tìm thấy 1 lời giải
        }

        val copyBoard = board.map { it.clone() }.toTypedArray()
        solve(copyBoard)
        return count
    }

    // Kiểm tra board có duy nhất 1 lời giải không
    private fun hasUniqueSolution(board: Array<IntArray>): Boolean {
        return countSolutions(board) == 1
    }

    // Sinh một bảng Sudoku đầy đủ (solution)
    fun generateFullBoard(): Array<Array<Cell>> {
        val board = Array(BOARD_SIZE) { IntArray(BOARD_SIZE) { 0 } }
        solveBoard(board)

        return Array(BOARD_SIZE) { row ->
            Array(BOARD_SIZE) { col ->
                Cell(
                    row = row,
                    col = col,
                    value = board[row][col],
                    isVisible = true,     // full board: tất cả visible
                    isEditable = false,   // không cho chỉnh sửa
                    userValue = null
                )
            }
        }
    }

    // Tạo puzzle từ full board theo độ khó (số ô trống)
    fun generatePuzzle(cells: Array<Array<Cell>>, diff: Difficulty): Array<Array<Cell>> {
        // Copy giá trị từ fullBoard ra mảng IntArray để xử lý
        val boardInt = Array(BOARD_SIZE) { row ->
            IntArray(BOARD_SIZE) { col -> cells[row][col].value }
        }

        val emptyCells = when (diff) {
            Difficulty.Beginner -> (40..45).random()
            Difficulty.Intermediate -> (50..55).random()
            Difficulty.Advanced -> (60..65).random()
        }

        // Danh sách vị trí để xóa dần ô
        val positions = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                positions.add(i to j)
            }
        }
        positions.shuffle()

        var removed = 0
        val cellsToHide = mutableSetOf<Pair<Int, Int>>()

        for ((row, col) in positions) {
            if (removed >= emptyCells) break

            val backup = boardInt[row][col]
            boardInt[row][col] = 0

            if (!hasUniqueSolution(boardInt)) {
                boardInt[row][col] = backup // hoàn tác nếu không unique solution
            } else {
                cellsToHide.add(row to col)
                removed++
            }
        }

        // Tạo lại mảng cells - giữ nguyên value, chỉ thay đổi isEditable và isVisible
        return Array(BOARD_SIZE) { row ->
            Array(BOARD_SIZE) { col ->
                val isHidden = cellsToHide.contains(row to col)
                Cell(
                    row = row,
                    col = col,
                    value = cells[row][col].value, // Giữ nguyên value từ fullBoard
                    isVisible = !isHidden,         // ô bị hide thì không visible
                    isEditable = isHidden,         // ô bị hide thì cho phép edit
                    userValue = null
                )
            }
        }
    }
}