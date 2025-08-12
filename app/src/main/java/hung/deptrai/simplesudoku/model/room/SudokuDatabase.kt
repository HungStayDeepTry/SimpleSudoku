package hung.deptrai.simplesudoku.model.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import hung.deptrai.simplesudoku.model.room.converter.SudokuConverters
import hung.deptrai.simplesudoku.model.room.dao.SudokuDao
import hung.deptrai.simplesudoku.model.room.entity.SudokuGameEntity

@Database(entities = [SudokuGameEntity::class], version = 1, exportSchema = false)
@TypeConverters(SudokuConverters::class)
abstract class SudokuDatabase : RoomDatabase() {
    abstract fun sudokuDao(): SudokuDao
}