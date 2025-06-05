package hung.deptrai.simplesudoku.ui.component

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import hung.deptrai.simplesudoku.R
import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.common.Difficulty
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
    var showPauseDialog by remember { mutableStateOf(false) }
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
    if (showPauseDialog) {
        PauseDialog(
            timeElapsed = uiState.timeElapsed,
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
            timeElapsed = uiState.timeElapsed,
            onPauseClick = {
                showPauseDialog = true
                onAction(PlayAction.PauseGame(parseTimeElapsed(uiState.timeElapsed)))
            },
            isPaused = uiState.isGamePaused
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
    timeElapsed: String,
    isPaused: Boolean,
    onPauseClick: () -> Unit
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

        // Timer with Pause Button
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

            // Pause/Resume Button
            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Icon(
                    painter = if (isPaused) painterResource(R.drawable.baseline_play_arrow_24) else painterResource(R.drawable.baseline_pause_24),
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
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
@Composable
fun PauseDialog(
    timeElapsed: String,
    difficulty: Difficulty,
    errorCount: Int,
    maxErrors: Int,
    onContinue: () -> Unit
) {
    Dialog(onDismissRequest = { /* Không cho dismiss bằng cách click ngoài */ }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                Text(
                    text = "Game Paused",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )

                Divider()

                // Game Stats
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Time Elapsed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Time:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = timeElapsed,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Difficulty
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Difficulty:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when(difficulty) {
                                    Difficulty.Beginner -> Color(0xFF4CAF50).copy(alpha = 0.2f)
                                    Difficulty.Intermediate -> Color(0xFFFF9800).copy(alpha = 0.2f)
                                    Difficulty.Advanced -> Color(0xFFF44336).copy(alpha = 0.2f)
                                }
                            )
                        ) {
                            Text(
                                text = difficulty.name,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = when(difficulty) {
                                    Difficulty.Beginner -> Color(0xFF2E7D32)
                                    Difficulty.Intermediate -> Color(0xFFE65100)
                                    Difficulty.Advanced -> Color(0xFFC62828)
                                },
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Errors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Errors:",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = "$errorCount/$maxErrors",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Continue Button
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Continue Game",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
fun parseTimeElapsed(time: String): Long {
    val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
    return when (parts.size) {
        2 -> parts[0] * 60L + parts[1] // mm:ss
        3 -> parts[0] * 3600L + parts[1] * 60L + parts[2] // hh:mm:ss
        else -> 0L
    }
}