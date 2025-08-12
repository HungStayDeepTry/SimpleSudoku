package hung.deptrai.simplesudoku.model

import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.Difficulty

object BoardGenerator {
    private const val BOARD_SIZE = 9

    private fun isSafe(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {

        for (x in 0 until BOARD_SIZE) {
            if (board[row][x] == num) return false
        }

        for (x in 0 until BOARD_SIZE) {
            if (board[x][col] == num) return false
        }

        val startRow = row - row % 3
        val startCol = col - col % 3
        for (i in 0 until 3) {
            for (j in 0 until 3) {
                if (board[startRow + i][startCol + j] == num) return false
            }
        }
        return true
    }

    private fun solveBoard(board: Array<IntArray>): Boolean {
        for (row in 0 until BOARD_SIZE) {
            for (col in 0 until BOARD_SIZE) {
                if (board[row][col] == 0) {
                    val numbers = (1..9).shuffled()
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
            count++
        }

        val copyBoard = board.map { it.clone() }.toTypedArray()
        solve(copyBoard)
        return count
    }

    private fun hasUniqueSolution(board: Array<IntArray>): Boolean {
        return countSolutions(board) == 1
    }

    fun generateFullBoard(): Array<Array<Cell>> {
        val board = Array(BOARD_SIZE) { IntArray(BOARD_SIZE) { 0 } }
        solveBoard(board)

        return Array(BOARD_SIZE) { row ->
            Array(BOARD_SIZE) { col ->
                Cell(
                    row = row,
                    col = col,
                    value = board[row][col],
                    isVisible = true,
                    isEditable = false,
                    userValue = null
                )
            }
        }
    }

    fun generatePuzzle(cells: Array<Array<Cell>>, diff: Difficulty): Array<Array<Cell>> {

        val boardInt = Array(BOARD_SIZE) { row ->
            IntArray(BOARD_SIZE) { col -> cells[row][col].value }
        }

        val emptyCells = when (diff) {
            Difficulty.Beginner -> (40..45).random()
            Difficulty.Intermediate -> (50..55).random()
            Difficulty.Advanced -> (60..65).random()
        }


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
                boardInt[row][col] = backup
            } else {
                cellsToHide.add(row to col)
                removed++
            }
        }

        return Array(BOARD_SIZE) { row ->
            Array(BOARD_SIZE) { col ->
                val isHidden = cellsToHide.contains(row to col)
                Cell(
                    row = row,
                    col = col,
                    value = cells[row][col].value,
                    isVisible = !isHidden,
                    isEditable = isHidden,
                    userValue = null
                )
            }
        }
    }
}