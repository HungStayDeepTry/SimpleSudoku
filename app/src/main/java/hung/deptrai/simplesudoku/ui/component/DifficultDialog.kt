package hung.deptrai.simplesudoku.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import hung.deptrai.simplesudoku.R
import hung.deptrai.simplesudoku.common.Difficulty

@Composable
fun DifficultyDialog(
    onDismiss: () -> Unit,
    onConfirm: (Difficulty) -> Unit
) {
    val options = Difficulty.entries

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.onBackground)
                .padding(24.dp)
                .clickable(enabled = false) {},
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.difficulty_dialog_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.background
                )

                Spacer(modifier = Modifier.height(16.dp))

                options.forEach { option ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7F7F7))
                            .clickable { onConfirm(option) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (option) {
                                Difficulty.Beginner -> stringResource(R.string.difficulty_easy)
                                Difficulty.Intermediate -> stringResource(R.string.difficulty_medium)
                                Difficulty.Advanced -> stringResource(R.string.difficulty_hard)
                            },
                            color = Color(0xFF57370D),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }
}

