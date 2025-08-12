package hung.deptrai.simplesudoku.model.room.entity

import androidx.room.Entity

@Entity(tableName = "cell_table")
data class CellEntity(
    val row: Int,
    val col: Int,
    val value: Int,
    val isVisible: Boolean,
    val isEditable: Boolean,
    val userValue: Int?,
    val isSelected: Boolean,
    val isHighlighted: Boolean,
    val notes: List<Boolean>
)