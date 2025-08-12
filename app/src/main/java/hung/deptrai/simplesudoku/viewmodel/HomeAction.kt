package hung.deptrai.simplesudoku.viewmodel

import hung.deptrai.simplesudoku.common.Difficulty

sealed class HomeAction {
    data class onPlayGame(val diff: Difficulty) : HomeAction()
    object onResumeGame : HomeAction()
    object onDeleteExistedGame : HomeAction()
}