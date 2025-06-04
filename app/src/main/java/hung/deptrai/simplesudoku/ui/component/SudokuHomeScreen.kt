package hung.deptrai.simplesudoku.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import hung.deptrai.simplesudoku.common.Difficulty
import hung.deptrai.simplesudoku.viewmodel.HomeAction

@Composable
fun HomeScreen(
    onGameEvent: (HomeAction) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF0C9), Color(0xFFFFE6AA))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "Sudoku",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFF57370D)
            )

            Button(
                onClick = {
                    showDialog = true
                },
                modifier = Modifier
                    .height(50.dp)
                    .width(200.dp)
            ) {
                Text("Trò chơi mới")
            }

            Button(
                onClick = {  },
                modifier = Modifier
                    .height(50.dp)
                    .width(200.dp)
            ) {
                Text("Tiếp tục chơi")
            }
        }

        DifficultyLauncher(
            triggerDialog = showDialog,
            onDismiss = { showDialog = false },
            onDifficultySelected = { difficulty ->
                onGameEvent(HomeAction.onPlayGame(difficulty))
            }
        )
    }
}
