package hung.deptrai.simplesudoku.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.ui.component.smaller.CustomNavBottom
import hung.deptrai.simplesudoku.ui.component.smaller.GameHeader
import hung.deptrai.simplesudoku.ui.component.smaller.NumberInputPanel
import hung.deptrai.simplesudoku.ui.component.smaller.PauseDialog
import hung.deptrai.simplesudoku.ui.component.smaller.SudokuBoard
import hung.deptrai.simplesudoku.ui.component.smaller.SudokuCell
import hung.deptrai.simplesudoku.ui.component.util.parseTimeElapsed
import hung.deptrai.simplesudoku.viewmodel.HomeAction
import hung.deptrai.simplesudoku.viewmodel.PlayAction
import hung.deptrai.simplesudoku.viewmodel.SudokuUiState

@Composable
fun SudokuGameScreen(
    uiState: SudokuUiState,
    selectedCell: Triple<Int, Int, Int>,
    onAction: (PlayAction) -> Unit,
    modifier: Modifier = Modifier,
    onGameEvent: (HomeAction) -> Unit,
    onExit: () -> Unit
) {
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }
    var hasHandledResult by remember { mutableStateOf(false) }

    var size by remember { mutableStateOf(IntSize.Zero) }

    LaunchedEffect(uiState.isGameCompleted, uiState.isGameFailed) {
        if (!uiState.isGameCompleted && !uiState.isGameFailed) {
            hasHandledResult = false
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            onExit
        }
    }

    if ((uiState.isGameCompleted || uiState.isGameFailed) && !hasHandledResult) {
        GameResultDialog(
            uiState = uiState,
            onGameEvent = {
                showDifficultyDialog = true
                hasHandledResult = true
            },
            onDismissRequest = {
                showDifficultyDialog = true
                hasHandledResult = true
            }
        )
    }
    if (showPauseDialog) {
        PauseDialog(
            timeElapsed = uiState.timerText,
            difficulty = uiState.difficulty,
            errorCount = uiState.errorCount,
            maxErrors = uiState.maxErrors,
            onContinue = {
                showPauseDialog = false
                onAction(PlayAction.ResumeGame)
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .onGloballyPositioned { coordinates ->
                size = coordinates.size
            }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DifficultyLauncher(
            triggerDialog = showDifficultyDialog,
            onDismiss = {
                if (!uiState.isGameFailed) {
                    showDifficultyDialog = false
                }
            },
            onDifficultySelected = { difficulty ->
                onAction(PlayAction.RestartGame)
                onGameEvent(HomeAction.onPlayGame(difficulty))
            }
        )

        GameHeader(
            errorCount = uiState.errorCount,
            maxErrors = uiState.maxErrors,
            timeElapsed = uiState.timerText,
            onPauseClick = {
                showPauseDialog = true
                onAction(PlayAction.PauseGame(parseTimeElapsed(uiState.timerText)))
            },
            isPaused = uiState.isGamePaused,
            onToggleNoteMode = {
                onAction(PlayAction.ToggleNoteMode)
            },
            isNoteMode = uiState.isNoteMode
        )

        Spacer(modifier = Modifier.height(16.dp))

        SudokuBoard(
            cells = uiState.cells,
            onCellClick = { row, col ->
                onAction(PlayAction.CellSelect(row, col))
            },
            screenSize = size
        )

        Spacer(modifier = Modifier.height(24.dp))

        NumberInputPanel(
            onNumberClick = { number ->
                if (selectedCell.first != -1 && selectedCell.second != -1) {
                    if (uiState.isNoteMode) {
                        onAction(
                            PlayAction.CellNote(
                                selectedCell.first,
                                selectedCell.second,
                                number
                            )
                        )
                    } else {
                        onAction(
                            PlayAction.CellFill(
                                selectedCell.first,
                                selectedCell.second,
                                number
                            )
                        )
                    }
                }
            },
            onEraseClick = {
                if (selectedCell.first != -1 && selectedCell.second != -1) {
                    onAction(PlayAction.CellErase(selectedCell.first, selectedCell.second))
                }
            }
        )
        Spacer(Modifier.height(8.dp))
        CustomNavBottom(
            height = 48.dp,
            text = "New Game",
            onClick = {
                showDifficultyDialog = true
            }
        )
    }
}

@Composable
fun SudokuBox(
    cells: Array<Array<Cell>>,
    boxRow: Int,
    boxCol: Int,
    onCellClick: (Int, Int) -> Unit,
    cellSize: Dp
) {
    Column {
        for (cellRow in 0..2) {
            Row {
                for (cellCol in 0..2) {
                    val actualRow = boxRow * 3 + cellRow
                    val actualCol = boxCol * 3 + cellCol
                    val cell = cells[actualRow][actualCol]

                    SudokuCell(
                        cell = cell,
                        size = cellSize,
                        onClick = { onCellClick(actualRow, actualCol) }
                    )
                }
            }
        }
    }
}
