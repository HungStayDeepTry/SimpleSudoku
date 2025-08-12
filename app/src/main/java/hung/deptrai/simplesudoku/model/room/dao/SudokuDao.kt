package hung.deptrai.simplesudoku.model.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import hung.deptrai.simplesudoku.model.room.entity.SudokuGameEntity
@Dao
interface SudokuDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGame(game: SudokuGameEntity)

    @Query("SELECT * FROM sudoku_game WHERE id = :id LIMIT 1")
    suspend fun getGameById(id: String): SudokuGameEntity?

    @Query("DELETE FROM sudoku_game WHERE id = :id")
    suspend fun deleteGame(id: String)

    @Query("DELETE FROM sudoku_game")
    suspend fun clearAll()

    @Query("SELECT * FROM sudoku_game ORDER BY lastmodified DESC LIMIT 1")
    suspend fun getLastGame(): SudokuGameEntity?

    @Query("SELECT COUNT(*) FROM sudoku_game WHERE gameStatus IN ('ONGOING','PAUSED')")
    suspend fun countUnfinishedGames(): Int

    @Query("SELECT * FROM sudoku_game WHERE gameStatus IN ('ONGOING','PAUSED') ORDER BY lastModified DESC LIMIT 1")
    suspend fun getLastUnfinishedGame(): SudokuGameEntity?
}