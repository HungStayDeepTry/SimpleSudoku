package hung.deptrai.simplesudoku.common

data class SudokuGame(
    var id: String,
    val cells: Array<Array<Cell>>,
    var timeElapsed: Long = 0L,
    var difficulty: Difficulty,
    var gameStatus: GameStatus = GameStatus.ONGOING,
    var errorCount: Int = 0,
    var maxErrors: Int = 2
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SudokuGame

        if (id != other.id) return false
        if (!cells.contentDeepEquals(other.cells)) return false
        if (timeElapsed != other.timeElapsed) return false
        if (difficulty != other.difficulty) return false
        if (gameStatus != other.gameStatus) return false
        if (errorCount != other.errorCount) return false
        if (maxErrors != other.maxErrors) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + cells.contentDeepHashCode()
        result = 31 * result + timeElapsed.hashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + gameStatus.hashCode()
        result = 31 * result + errorCount
        result = 31 * result + maxErrors
        return result
    }
}