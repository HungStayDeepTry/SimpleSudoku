package hung.deptrai.simplesudoku.common

data class Cell(
    val row: Int,
    val col: Int,
    var value: Int,
    var isVisible: Boolean,
    var isEditable: Boolean
)