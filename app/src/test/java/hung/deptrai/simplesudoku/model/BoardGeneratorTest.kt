package hung.deptrai.simplesudoku.model

import hung.deptrai.simplesudoku.common.Difficulty
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.system.measureTimeMillis

class BoardGeneratorTest {

    // -------------------------
    // 1. Test isSafe
    // -------------------------
    @Test
    fun testIsSafe_rowConflict_returnsFalse() {
        val board = Array(9) { IntArray(9) }
        board[0][0] = 5
        assertFalse(BoardGenerator.isSafe(board, 0, 1, 5))
    }

    @Test
    fun testIsSafe_colConflict_returnsFalse() {
        val board = Array(9) { IntArray(9) }
        board[0][0] = 7
        assertFalse(BoardGenerator.isSafe(board, 1, 0, 7))
    }

    @Test
    fun testIsSafe_boxConflict_returnsFalse() {
        val board = Array(9) { IntArray(9) }
        board[0][0] = 9
        assertFalse(BoardGenerator.isSafe(board, 1, 1, 9))
    }

    @Test
    fun testIsSafe_validPlacement_returnsTrue() {
        val board = Array(9) { IntArray(9) }
        assertTrue(BoardGenerator.isSafe(board, 0, 0, 4))
    }

    // -------------------------
    // 2. Test solveBoard
    // -------------------------
    @Test
    fun testSolveBoard_canSolveValidBoard() {
        val board = Array(9) { IntArray(9) }
        val solved = BoardGenerator.solveBoard(board)
        assertTrue(solved)
        assertTrue(board.all { row -> row.all { it in 1..9 } })
    }

    // -------------------------
    // 3. Test countSolutions
    // -------------------------
    @Test
    fun testCountSolutions_fullBoard_returnsOne() {
        val cells = BoardGenerator.generateFullBoard()
        val board = Array(9) { row -> IntArray(9) { col -> cells[row][col].value } }
        val solutions = BoardGenerator.countSolutions(board)
        assertEquals(1, solutions)
    }

    // -------------------------
    // 4. Test hasUniqueSolution
    // -------------------------
    @Test
    fun testHasUniqueSolution_fullBoard_returnsTrue() {
        val cells = BoardGenerator.generateFullBoard()
        val board = Array(9) { row -> IntArray(9) { col -> cells[row][col].value } }
        assertTrue(BoardGenerator.hasUniqueSolution(board))
    }

    // -------------------------
    // 5. Test generateFullBoard
    // -------------------------
    @Test
    fun testGenerateFullBoard_noZeros() {
        val cells = BoardGenerator.generateFullBoard()
        assertTrue(cells.all { row -> row.all { it.value in 1..9 } })
    }

    // -------------------------
    // 6. Test generatePuzzle
    // -------------------------
    @Test
    fun testGeneratePuzzle_uniqueSolutionAndHiddenCount() {
        val difficulties = listOf(Difficulty.Beginner, Difficulty.Intermediate, Difficulty.Advanced)

        for (diff in difficulties) {
            val fullBoard = BoardGenerator.generateFullBoard()

            val timeTaken = measureTimeMillis {
                val puzzle = BoardGenerator.generatePuzzle(fullBoard, diff)

                val hiddenCount = puzzle.sumOf { row -> row.count { !it.isVisible } }

                println("[$diff] Hidden empty cells: $hiddenCount")

                val expectedRange = when (diff) {
                    Difficulty.Beginner -> 40..45
                    Difficulty.Intermediate -> 50..55
                    Difficulty.Advanced -> 55..64
                }

                assertTrue(
                    "Hidden cells for $diff not in range $expectedRange, actual: $hiddenCount"
                    , hiddenCount in expectedRange
                )

                // Kiểm tra puzzle có solution duy nhất
                val boardInt = Array(9) { row ->
                    IntArray(9) { col ->
                        if (puzzle[row][col].isVisible) puzzle[row][col].value else 0
                    }
                }
                assertTrue(BoardGenerator.hasUniqueSolution(boardInt))
            }

            println("[$diff] Generate puzzle time: ${timeTaken}ms")
        }
    }
}