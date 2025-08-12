package hung.deptrai.simplesudoku.model.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import hung.deptrai.simplesudoku.common.Difficulty
import hung.deptrai.simplesudoku.common.GameStatus

@Entity(tableName = "sudoku_game")
data class SudokuGameEntity(
    @PrimaryKey val id: String,
    val cellsJson: String,
    val timeElapsed: Long,
    val difficulty: Difficulty,
    val gameStatus: GameStatus,
    val errorCount: Int,
    val maxErrors: Int,
    val lastModified: Long
)