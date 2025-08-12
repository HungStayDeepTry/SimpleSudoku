package hung.deptrai.simplesudoku.model.room.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.Difficulty
import hung.deptrai.simplesudoku.common.GameStatus

class SudokuConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromDifficulty(difficulty: Difficulty): String = difficulty.name

    @TypeConverter
    fun toDifficulty(value: String): Difficulty = Difficulty.valueOf(value)

    @TypeConverter
    fun fromGameStatus(status: GameStatus): String = status.name

    @TypeConverter
    fun toGameStatus(value: String): GameStatus = GameStatus.valueOf(value)

    @TypeConverter
    fun fromCells(cells: Array<Array<Cell>>): String {
        return gson.toJson(cells)
    }

    @TypeConverter
    fun toCells(value: String): Array<Array<Cell>> {
        val type = object : TypeToken<Array<Array<Cell>>>() {}.type
        return gson.fromJson(value, type)
    }
}