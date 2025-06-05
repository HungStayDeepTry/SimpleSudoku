package hung.deptrai.simplesudoku.viewmodel

import hung.deptrai.simplesudoku.common.SudokuGame

sealed class PlayAction {
    data class CellSelect(val row: Int, val col: Int): PlayAction()
    data class CellFill(val row: Int, val col: Int, val value: Int): PlayAction()
    data class CellErase(val row: Int, val col: Int): PlayAction()
    object ResumeGame : PlayAction()
    data class PauseGame(val elapsedTime: Long) : PlayAction()
    object RequestHint : PlayAction()
    object RestartGame : PlayAction()
    object SaveGame : PlayAction()
    object ExitGame : PlayAction()
}