package hung.deptrai.simplesudoku.ui.component.smaller

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hung.deptrai.simplesudoku.R

@Composable
fun GameHeader(
    errorCount: Int,
    maxErrors: Int,
    timeElapsed: String,
    isPaused: Boolean,
    onPauseClick: () -> Unit,
    isNoteMode: Boolean,
    onToggleNoteMode: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
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

            Card(
                modifier = Modifier
                    .clickable(onClick = onToggleNoteMode),
                colors = CardDefaults.cardColors(
                    containerColor = if (isNoteMode) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = stringResource(R.string.note),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isNoteMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }

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

            IconButton(
                onClick = onPauseClick,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    )
            ) {
                Icon(
                    painter = if (isPaused) painterResource(R.drawable.baseline_play_arrow_24) else painterResource(
                        R.drawable.baseline_pause_24
                    ),
                    contentDescription = if (isPaused) "Resume" else "Pause",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}