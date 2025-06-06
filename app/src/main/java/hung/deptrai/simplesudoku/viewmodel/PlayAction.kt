package hung.deptrai.simplesudoku.viewmodel

sealed class PlayAction {
    data class CellSelect(val row: Int, val col: Int): PlayAction()
    data class CellFill(val row: Int, val col: Int, val value: Int): PlayAction()
    data class CellErase(val row: Int, val col: Int): PlayAction()
    data class CellNote(val row: Int, val col: Int, val value: Int) : PlayAction()
    object ResumeGame : PlayAction()
    data class PauseGame(val elapsedTime: Long) : PlayAction()
    object ToggleNoteMode : PlayAction()
    object RequestHint : PlayAction()
    object RestartGame : PlayAction()
}