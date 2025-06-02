package hung.deptrai.simplesudoku.model

class SudokuBoard(
    val cells: Array<Array<Cell>>
) {

    fun getCell(row: Int, col: Int): Cell {
        return cells[row][col]
    }

    fun isCompleted(): Boolean {
        return cells.all { row ->
            row.all { cell ->
                cell.isVisible // chỉ cần tất cả ô đã hiển thị là hoàn thành
            }
        }
    }

    fun isCorrectInput(row: Int, col: Int, inputValue: Int): Boolean {
        val cell = getCell(row, col)
        return cell.value == inputValue
    }

    fun revealCellIfCorrect(row: Int, col: Int, inputValue: Int): Boolean {
        val cell = getCell(row, col)
        if (cell.isEditable && !cell.isVisible) {
            if (inputValue == cell.value) {
                cell.isVisible = true
                return true
            } else {
                cell.isVisible = true // vẫn hiển thị (tuỳ logic game)
                return false
            }
        }
        return false
    }
}