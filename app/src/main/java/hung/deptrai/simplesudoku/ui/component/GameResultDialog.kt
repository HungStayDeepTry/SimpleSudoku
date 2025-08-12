package hung.deptrai.simplesudoku.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import hung.deptrai.simplesudoku.R
import hung.deptrai.simplesudoku.viewmodel.SudokuUiState

@Composable
fun GameResultDialog(
    uiState: SudokuUiState,
    onGameEvent: () -> Unit,
    onDismissRequest: () -> Unit
) {
    if (uiState.isGameCompleted || uiState.isGameFailed) {
        val title =
            if (uiState.isGameCompleted) stringResource(R.string.game_result_win_title) else stringResource(
                R.string.game_result_lose_title
            )
        val message = if (uiState.isGameCompleted) {
            stringResource(R.string.game_result_win_desc)
        } else {
            stringResource(R.string.game_result_lose_desc)
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
                    Text(stringResource(R.string.game_result_confirm))
                }
            }
        )
    }
}