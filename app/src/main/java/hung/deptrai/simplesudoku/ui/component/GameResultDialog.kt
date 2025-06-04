package hung.deptrai.simplesudoku.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import hung.deptrai.simplesudoku.viewmodel.HomeAction
import hung.deptrai.simplesudoku.viewmodel.SudokuUiState

@Composable
fun GameResultDialog(
    uiState: SudokuUiState,
    onGameEvent: () -> Unit,
    onDismissRequest: () -> Unit
) {
    if (uiState.isGameCompleted || uiState.isGameFailed) {
        val title = if (uiState.isGameCompleted) "🎉 Chiến thắng!" else "😢 Thất bại!"
        val message = if (uiState.isGameCompleted) {
            "Bạn đã hoàn thành bảng Sudoku một cách chính xác!"
        } else {
            "Bạn đã vượt quá số lần sai cho phép. Hãy thử lại nhé!"

        }

        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissRequest()
                        onGameEvent()
                    }
                ) {
                    Text("Chơi game mới")
                }
            }
        )
    }
}