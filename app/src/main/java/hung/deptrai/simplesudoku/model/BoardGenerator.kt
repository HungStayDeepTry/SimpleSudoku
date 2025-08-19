package hung.deptrai.simplesudoku.model

import androidx.annotation.VisibleForTesting
import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.Difficulty
import java.util.PriorityQueue

object BoardGenerator {
    private const val BOARD_SIZE = 9

    private enum class SolvingTechnique(val difficultyScore: Int) {
        NAKED_SINGLE(1),        // Chỉ có 1 số có thể điền
        HIDDEN_SINGLE(2),       // Số đó chỉ có thể ở 1 vị trí trong unit
        NAKED_PAIR(5),          // 2 ô chỉ có thể chứa 2 số giống nhau
        HIDDEN_PAIR(6),         // 2 số chỉ có thể ở 2 vị trí trong unit
        POINTING_PAIRS(8),      // Elimination dựa trên box-line interaction
        BOX_LINE_REDUCTION(10), // Elimination ngược lại
        NAKED_TRIPLE(15),       // 3 ô chỉ có thể chứa 3 số
        HIDDEN_TRIPLE(18),      // 3 số chỉ có thể ở 3 vị trí
        X_WING(25),             // Advanced elimination pattern
        SWORDFISH(35),          // Phức tạp hơn X-Wing
        GUESSING(50)            // Cần backtracking/guessing
    }

    private data class DifficultyAnalysis(
        val requiredTechniques: Set<SolvingTechnique>,
        val totalScore: Int,
        val isValid: Boolean
    ) {
        val estimatedDifficulty: Difficulty
            get() = when {
                totalScore <= 15 -> Difficulty.Beginner
                totalScore <= 35 -> Difficulty.Intermediate
                else -> Difficulty.Advanced
            }
    }

    private data class CellPosition(
        val row: Int,
        val col: Int,
        val score: Int
    ) : Comparable<CellPosition> {
        override fun compareTo(other: CellPosition): Int =
            this.score.compareTo(other.score)
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun isSafe(board: Array<IntArray>, row: Int, col: Int, num: Int): Boolean {
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun solveBoard(board: Array<IntArray>): Boolean {
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun countSolutions(board: Array<IntArray>): Int {
        var count = 0

        fun solve(copy: Array<IntArray>) {
            if (count > 1) return
            for (row in 0 until BOARD_SIZE) {
                for (col in 0 until BOARD_SIZE) {
                    if (copy[row][col] == 0) {
                        for (num in 1..9) {
                            if (isSafe(copy, row, col, num)) {
                                copy[row][col] = num
                                solve(copy)
                                copy[row][col] = 0
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

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal fun hasUniqueSolution(board: Array<IntArray>): Boolean {
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
                    userValue = null,
                    notes = List(9) { false }
                )
            }
        }
    }

    private fun analyzeDifficulty(board: Array<IntArray>): DifficultyAnalysis {
        val testBoard = board.map { it.clone() }.toTypedArray()
        val requiredTechniques = mutableSetOf<SolvingTechnique>()

        var progress = true
        var iterations = 0
        val maxIterations = 50

        while (progress && iterations < maxIterations && !isBoardComplete(testBoard)) {
            progress = false
            iterations++

            if (applyNakedSingles(testBoard)) {
                requiredTechniques.add(SolvingTechnique.NAKED_SINGLE)
                progress = true
                continue
            }

            if (applyHiddenSingles(testBoard)) {
                requiredTechniques.add(SolvingTechnique.HIDDEN_SINGLE)
                progress = true
                continue
            }

            if (applyNakedPairs(testBoard)) {
                requiredTechniques.add(SolvingTechnique.NAKED_PAIR)
                progress = true
                continue
            }

            if (applyPointingPairs(testBoard)) {
                requiredTechniques.add(SolvingTechnique.POINTING_PAIRS)
                progress = true
                continue
            }

            if (!progress) {
                requiredTechniques.add(SolvingTechnique.GUESSING)
                progress = true
            }
        }

        val totalScore = requiredTechniques.sumOf { it.difficultyScore }
        val isValid = isBoardComplete(testBoard) ||
                requiredTechniques.contains(SolvingTechnique.GUESSING)

        return DifficultyAnalysis(requiredTechniques, totalScore, isValid)
    }

    private fun isBoardComplete(board: Array<IntArray>): Boolean {
        return board.all { row -> row.all { it != 0 } }
    }

    private fun getCandidates(board: Array<IntArray>, row: Int, col: Int): Set<Int> {
        if (board[row][col] != 0) return emptySet()

        val used = mutableSetOf<Int>()

        for (c in 0 until BOARD_SIZE) {
            if (board[row][c] != 0) used.add(board[row][c])
        }
        for (r in 0 until BOARD_SIZE) {
            if (board[r][col] != 0) used.add(board[r][col])
        }

        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                if (board[r][c] != 0) used.add(board[r][c])
            }
        }

        return (1..9).toSet() - used
    }

    private fun applyNakedSingles(board: Array<IntArray>): Boolean {
        var changed = false
        for (row in 0 until BOARD_SIZE) {
            for (col in 0 until BOARD_SIZE) {
                if (board[row][col] == 0) {
                    val candidates = getCandidates(board, row, col)
                    if (candidates.size == 1) {
                        board[row][col] = candidates.first()
                        changed = true
                    }
                }
            }
        }
        return changed
    }

    private fun applyHiddenSingles(board: Array<IntArray>): Boolean {
        var changed = false
        for (row in 0 until BOARD_SIZE) {
            for (num in 1..9) {
                val possibleCols = mutableListOf<Int>()
                for (col in 0 until BOARD_SIZE) {
                    if (board[row][col] == 0 &&
                        getCandidates(board, row, col).contains(num)
                    ) {
                        possibleCols.add(col)
                    }
                }
                if (possibleCols.size == 1) {
                    board[row][possibleCols[0]] = num
                    changed = true
                }
            }
        }
        for (col in 0 until BOARD_SIZE) {
            for (num in 1..9) {
                val possibleRows = mutableListOf<Int>()
                for (row in 0 until BOARD_SIZE) {
                    if (board[row][col] == 0 &&
                        getCandidates(board, row, col).contains(num)
                    ) {
                        possibleRows.add(row)
                    }
                }
                if (possibleRows.size == 1) {
                    board[possibleRows[0]][col] = num
                    changed = true
                }
            }
        }
        for (boxRow in 0 until 3) {
            for (boxCol in 0 until 3) {
                for (num in 1..9) {
                    val possibleCells = mutableListOf<Pair<Int, Int>>()
                    for (r in 0 until 3) {
                        for (c in 0 until 3) {
                            val actualRow = boxRow * 3 + r
                            val actualCol = boxCol * 3 + c
                            if (board[actualRow][actualCol] == 0 &&
                                getCandidates(board, actualRow, actualCol).contains(num)
                            ) {
                                possibleCells.add(actualRow to actualCol)
                            }
                        }
                    }
                    if (possibleCells.size == 1) {
                        val (r, c) = possibleCells[0]
                        board[r][c] = num
                        changed = true
                    }
                }
            }
        }
        return changed
    }

    private fun applyNakedPairs(board: Array<IntArray>): Boolean {
        return false
    }

    private fun applyPointingPairs(board: Array<IntArray>): Boolean {
        return false
    }


    fun generatePuzzle(cells: Array<Array<Cell>>, diff: Difficulty): Array<Array<Cell>> {
        val boardInt = Array(BOARD_SIZE) { row ->
            IntArray(BOARD_SIZE) { col -> cells[row][col].value }
        }

        val targetEmptyCells = when (diff) {
            Difficulty.Beginner -> (40..45).random()
            Difficulty.Intermediate -> (50..55).random()
            Difficulty.Advanced -> (55..60).random()
        }

        var bestPuzzle: Array<Array<Cell>>? = null
        var bestScore = Int.MAX_VALUE
        var attempts = 0
        val maxAttempts = when (diff) {
            Difficulty.Beginner -> 8
            Difficulty.Intermediate -> 10
            Difficulty.Advanced -> 15
        }

        while (attempts < maxAttempts) {
            val testBoard = boardInt.map { it.clone() }.toTypedArray()
            val cellsToHide = mutableSetOf<Pair<Int, Int>>()

            val actualEmpty = generateWithStrategy(testBoard, cellsToHide, targetEmptyCells)

            if (actualEmpty >= targetEmptyCells) {
                val analysis = analyzeDifficulty(testBoard)
                if (analysis.isValid) {
                    val scoreDiff = kotlin.math.abs(
                        analysis.totalScore - getDifficultyTargetScore(diff)
                    )
                    if (analysis.estimatedDifficulty == diff || scoreDiff < bestScore) {
                        bestScore = scoreDiff
                        bestPuzzle = Array(BOARD_SIZE) { row ->
                            Array(BOARD_SIZE) { col ->
                                val isHidden = cellsToHide.contains(row to col)
                                Cell(
                                    row = row,
                                    col = col,
                                    value = cells[row][col].value,
                                    isVisible = !isHidden,
                                    isEditable = isHidden,
                                    userValue = null,
                                    notes = List(9) { false }
                                )
                            }
                        }
                        if (analysis.estimatedDifficulty == diff) {
                            return bestPuzzle
                        }
                    }
                }
            } else if (diff == Difficulty.Advanced && actualEmpty >= 55) {
                val analysis = analyzeDifficulty(testBoard)
                if (analysis.isValid && analysis.estimatedDifficulty != Difficulty.Beginner) {
                    val scoreDiff = kotlin.math.abs(
                        analysis.totalScore - getDifficultyTargetScore(diff)
                    )
                    if (scoreDiff < bestScore) {
                        bestScore = scoreDiff
                        bestPuzzle = Array(BOARD_SIZE) { row ->
                            Array(BOARD_SIZE) { col ->
                                val isHidden = cellsToHide.contains(row to col)
                                Cell(
                                    row = row,
                                    col = col,
                                    value = cells[row][col].value,
                                    isVisible = !isHidden,
                                    isEditable = isHidden,
                                    userValue = null,
                                    notes = List(9) { false }
                                )
                            }
                        }
                    }
                }
            }
            attempts++
        }

        return bestPuzzle ?: generateFallbackPuzzle(cells, diff)
    }

    private fun getDifficultyTargetScore(diff: Difficulty): Int =
        when (diff) {
            Difficulty.Beginner -> 8
            Difficulty.Intermediate -> 20
            Difficulty.Advanced -> 30
        }

    private fun generateWithStrategy(
        board: Array<IntArray>,
        cellsToHide: MutableSet<Pair<Int, Int>>,
        target: Int
    ): Int {
        var removed = 0
        val heuristicTarget = (target * 0.7).toInt()
        removed = removeWithHeuristic(board, cellsToHide, heuristicTarget)

        if (removed < target) {
            removed = removeWithRandom(board, cellsToHide, target - removed, removed)
        }
        if (removed < target) {
            removed = aggressiveRandomRemoval(board, cellsToHide, target, removed)
        }
        return removed
    }

    private fun generateFallbackPuzzle(
        cells: Array<Array<Cell>>,
        diff: Difficulty
    ): Array<Array<Cell>> {
        val boardInt = Array(BOARD_SIZE) { row ->
            IntArray(BOARD_SIZE) { col -> cells[row][col].value }
        }

        val fallbackTarget = when (diff) {
            Difficulty.Beginner -> 35
            Difficulty.Intermediate -> 45
            Difficulty.Advanced -> 50
        }

        val positions = mutableListOf<Pair<Int, Int>>().apply {
            for (i in 0 until BOARD_SIZE) {
                for (j in 0 until BOARD_SIZE) {
                    add(i to j)
                }
            }
        }
        positions.shuffle()

        val cellsToHide = mutableSetOf<Pair<Int, Int>>()
        var removed = 0

        for ((row, col) in positions) {
            if (removed >= fallbackTarget) break

            val backup = boardInt[row][col]
            boardInt[row][col] = 0

            if (hasUniqueSolution(boardInt)) {
                cellsToHide.add(row to col)
                removed++
            } else {
                boardInt[row][col] = backup
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
                    userValue = null,
                    notes = List(9) { false }
                )
            }
        }
    }


    private fun removeWithHeuristic(
        board: Array<IntArray>,
        cellsToHide: MutableSet<Pair<Int, Int>>,
        target: Int
    ): Int {
        val priorityQueue = PriorityQueue<CellPosition>()
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                val score = calculateRemovalScore(board, i, j)
                priorityQueue.offer(CellPosition(i, j, score))
            }
        }

        var removed = 0
        while (priorityQueue.isNotEmpty() && removed < target) {
            val (row, col, _) = priorityQueue.poll()
            if (cellsToHide.contains(row to col)) continue

            val backup = board[row][col]
            board[row][col] = 0

            if (hasUniqueSolution(board)) {
                cellsToHide.add(row to col)
                removed++
            } else {
                board[row][col] = backup
            }
        }
        return removed
    }

    private fun removeWithRandom(
        board: Array<IntArray>,
        cellsToHide: MutableSet<Pair<Int, Int>>,
        additionalTarget: Int,
        currentRemoved: Int
    ): Int {
        val availablePositions = mutableListOf<Pair<Int, Int>>()
        for (i in 0 until BOARD_SIZE) {
            for (j in 0 until BOARD_SIZE) {
                if (!cellsToHide.contains(i to j)) {
                    availablePositions.add(i to j)
                }
            }
        }

        availablePositions.shuffle()
        var additionalRemoved = 0

        for ((row, col) in availablePositions) {
            if (additionalRemoved >= additionalTarget) break

            val backup = board[row][col]
            board[row][col] = 0

            if (hasUniqueSolution(board)) {
                cellsToHide.add(row to col)
                additionalRemoved++
            } else {
                board[row][col] = backup
            }
        }

        return currentRemoved + additionalRemoved
    }

    private fun aggressiveRandomRemoval(
        board: Array<IntArray>,
        cellsToHide: MutableSet<Pair<Int, Int>>,
        originalTarget: Int,
        currentRemoved: Int
    ): Int {
        val relaxedTarget = minOf(originalTarget, currentRemoved + 15)
        val availablePositions = mutableListOf<Pair<Int, Int>>().apply {
            for (i in 0 until BOARD_SIZE) {
                for (j in 0 until BOARD_SIZE) {
                    if (!cellsToHide.contains(i to j)) add(i to j)
                }
            }
        }

        var attempt = 0
        while (attempt < 3) {
            availablePositions.shuffle()
            var tempRemoved = currentRemoved

            for ((row, col) in availablePositions) {
                if (tempRemoved >= relaxedTarget) return tempRemoved
                if (cellsToHide.contains(row to col)) continue

                val backup = board[row][col]
                board[row][col] = 0

                if (hasUniqueSolution(board)) {
                    cellsToHide.add(row to col)
                    tempRemoved++
                } else {
                    board[row][col] = backup
                }
            }

            if (tempRemoved >= relaxedTarget) return tempRemoved
            attempt++
        }
        return cellsToHide.size
    }

    private fun calculateRemovalScore(board: Array<IntArray>, row: Int, col: Int): Int {
        val value = board[row][col]
        var score = 0

        for (c in 0 until BOARD_SIZE) {
            if (c != col && board[row][c] == value) score++
        }
        for (r in 0 until BOARD_SIZE) {
            if (r != row && board[r][col] == value) score++
        }

        val startRow = (row / 3) * 3
        val startCol = (col / 3) * 3
        for (r in startRow until startRow + 3) {
            for (c in startCol until startCol + 3) {
                if ((r != row || c != col) && board[r][c] == value) score++
            }
        }

        val isCorner = (row == 0 || row == 8) && (col == 0 || col == 8)
        val isEdge = row == 0 || row == 8 || col == 0 || col == 8
        if (isCorner) score -= 2
        else if (isEdge) score -= 1

        if (row in 3..5 && col in 3..5) score += 1

        return maxOf(0, score)
    }
}