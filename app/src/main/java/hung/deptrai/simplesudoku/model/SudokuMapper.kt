package hung.deptrai.simplesudoku.model

import com.google.gson.Gson
import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.SudokuGame
import hung.deptrai.simplesudoku.model.room.entity.CellEntity
import hung.deptrai.simplesudoku.model.room.entity.SudokuGameEntity

object SudokuMapper {
    private val gson = Gson()

    fun Cell.toEntity(): CellEntity {
        return CellEntity(
            row = row,
            col = col,
            value = value,
            isVisible = isVisible,
            isEditable = isEditable,
            userValue = userValue,
            isSelected = isSelected,
            isHighlighted = isHighlighted,
            notes = notes.toList()
        )
    }

    fun CellEntity.toModel(): Cell {
        return Cell(
            row = row,
            col = col,
            value = value,
            isVisible = isVisible,
            isEditable = isEditable,
            userValue = userValue,
            isSelected = isSelected,
            isHighlighted = isHighlighted,
            notes = notes.toMutableList()
        )
    }

    fun SudokuGame.toEntity(): SudokuGameEntity {
        return SudokuGameEntity(
            id = id,
            cellsJson = gson.toJson(cells),
            timeElapsed = timeElapsed,
            difficulty = difficulty,
            gameStatus = gameStatus,
            errorCount = errorCount,
            maxErrors = maxErrors,
            lastModified = System.currentTimeMillis()
        )
    }

    fun SudokuGameEntity.toModel(): SudokuGame {
        val type = Array<Array<Cell>>::class.java
        val cellsArray: Array<Array<Cell>> = gson.fromJson(cellsJson, type)

        return SudokuGame(
            id = id,
            cells = cellsArray,
            timeElapsed = timeElapsed,
            difficulty = difficulty,
            gameStatus = gameStatus,
            errorCount = errorCount,
            maxErrors = maxErrors
        )
    }
}