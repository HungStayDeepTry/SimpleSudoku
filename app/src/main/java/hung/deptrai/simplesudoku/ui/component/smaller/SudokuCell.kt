package hung.deptrai.simplesudoku.ui.component.smaller

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import hung.deptrai.simplesudoku.common.Cell

@Composable
fun SudokuCell(
    cell: Cell,
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
        when {
            !cell.isEditable && cell.userValue == null -> {
                Text(
                    text = cell.value.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            cell.userValue != null -> {
                Text(
                    text = cell.userValue.toString(),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = if (cell.userValue == cell.value) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    }
                )
            }

            cell.notes.any { it } -> {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    for (row in 0..2) {
                        Row(
                            modifier = Modifier.weight(1f)
                        ) {
                            for (col in 0..2) {
                                val noteNumber = row * 3 + col + 1
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (cell.notes[noteNumber - 1]) {
                                        Text(
                                            text = noteNumber.toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}