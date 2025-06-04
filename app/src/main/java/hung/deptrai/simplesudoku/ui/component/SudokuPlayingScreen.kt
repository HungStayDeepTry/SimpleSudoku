package hung.deptrai.simplesudoku.ui.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.viewmodel.HomeAction
import hung.deptrai.simplesudoku.viewmodel.PlayAction
import hung.deptrai.simplesudoku.viewmodel.SudokuUiState

@Composable
fun SudokuGameScreen(
    uiState: SudokuUiState,
    selectedCell: Triple<Int, Int, Int>,
    onAction: (PlayAction) -> Unit,
    modifier: Modifier = Modifier,
    onGameEvent: (HomeAction) -> Unit
) {
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var hasHandledResult by remember { mutableStateOf(false) }

    // Khi trạng thái game thay đổi, reset lại flag
    LaunchedEffect(uiState.isGameCompleted, uiState.isGameFailed) {
        if (!uiState.isGameCompleted && !uiState.isGameFailed) {
            hasHandledResult = false
        }
    }

    // Khi người chơi thắng hoặc thua và chưa xử lý
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


    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DifficultyLauncher(
            triggerDialog = showDifficultyDialog,
            onDismiss = { showDifficultyDialog = false },
            onDifficultySelected = { difficulty ->
                onAction(PlayAction.RestartGame)
                onGameEvent(HomeAction.onPlayGame(difficulty))
            }
        )
        // Header với Error Count và Timer
        GameHeader(
            errorCount = uiState.errorCount,
            maxErrors = uiState.maxErrors,
            timeElapsed = uiState.timeElapsed
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Sudoku Board
        SudokuBoard(
            cells = uiState.cells,
            onCellClick = { row, col ->
                onAction(PlayAction.CellSelect(row, col))
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Number Input Panel
        NumberInputPanel(
            onNumberClick = { number ->
                if (selectedCell.first != -1 && selectedCell.second != -1) {
                    onAction(PlayAction.CellFill(selectedCell.first, selectedCell.second, number))
                }
            },
            onEraseClick = {
                if (selectedCell.first != -1 && selectedCell.second != -1) {
                    onAction(PlayAction.CellErase(selectedCell.first, selectedCell.second))
                }
            }
        )
    }
}

@Composable
private fun GameHeader(
    errorCount: Int,
    maxErrors: Int,
    timeElapsed: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Error Count
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Text(
                text = "$errorCount/$maxErrors",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        // Timer
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = timeElapsed,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun SudokuBoard(
    cells: Array<Array<Cell>>, // Changed from cellStates to cells
    onCellClick: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            for (boxRow in 0..2) {
                Row {
                    for (boxCol in 0..2) {
                        SudokuBox(
                            cells = cells, // Changed parameter name
                            boxRow = boxRow,
                            boxCol = boxCol,
                            onCellClick = onCellClick
                        )
                        if (boxCol < 2) {
                            Divider(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(120.dp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                if (boxRow < 2) {
                    Divider(
                        modifier = Modifier
                            .height(2.dp)
                            .fillMaxWidth(),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}

@Composable
fun SudokuBox(
    cells: Array<Array<Cell>>,
    boxRow: Int,
    boxCol: Int,
    onCellClick: (Int, Int) -> Unit
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
                        onClick = { onCellClick(actualRow, actualCol) }
                    )
                }
            }
        }
    }
}

@Composable
fun SudokuCell(
    cell: Cell, // Now uses Cell directly instead of CellUIState
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .border(
                width = 1.dp,
                color = if (cell.isSelected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.outline
            )
            .background(
                color = when {
                    cell.isSelected -> MaterialTheme.colorScheme.primaryContainer
                    cell.isHighlighted -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    !cell.isEditable -> MaterialTheme.colorScheme.surfaceVariant
                    else -> MaterialTheme.colorScheme.surface
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (!cell.isEditable && cell.userValue == null) {
            Text(
                text = cell.value.toString() ?: "",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        } else {
            Text(
                text = if(cell.userValue == null){
                    ""
                } else cell.userValue.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = if(cell.userValue == cell.value){
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )
        }
    }
}

@Composable
fun NumberInputPanel(
    onNumberClick: (Int) -> Unit,
    onEraseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(5),
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Numbers 1-9
        items(9) { index ->
            val number = index + 1
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onNumberClick(number) },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = number.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        // Erase button
        item {
            Card(
                modifier = Modifier
                    .aspectRatio(1f)
                    .clickable { onEraseClick() },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Erase",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}