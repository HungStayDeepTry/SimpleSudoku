package hung.deptrai.simplesudoku.common

data class Cell(
    val row: Int,
    val col: Int,
    val value: Int,
    val isVisible: Boolean,
    val isEditable: Boolean,
    val userValue: Int?,
    val isSelected: Boolean = false,
    val isHighlighted: Boolean = false,
    val notes: List<Boolean> = List(9) { false }
)