package hung.deptrai.simplesudoku.model

data class Cell(
    val row: Int,
    val col: Int,
    var value: Int,
    var isVisible: Boolean,
    var isEditable: Boolean
)