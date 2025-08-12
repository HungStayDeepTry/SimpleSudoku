package hung.deptrai.simplesudoku.ui.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import hung.deptrai.simplesudoku.R

@Composable
fun ConfirmNewGameDialog(
    onDismissRequest: () -> Unit,
    onConfirmNewGame: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = stringResource(R.string.alert_continuous_label),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Text(
                text = stringResource(R.string.alert_continuous_title),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = {
                onConfirmNewGame()
                onDismissRequest()
            }) {
                Text(stringResource(R.string.alert_continuous_confirm))
            }
        },
        dismissButton = {
            Button(onClick = onDismissRequest) {
                Text(stringResource(R.string.alert_continuous_cancel))
            }
        }
    )
}
