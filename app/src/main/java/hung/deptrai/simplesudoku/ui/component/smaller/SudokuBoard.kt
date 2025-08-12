package hung.deptrai.simplesudoku.ui.component.smaller

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.ui.component.SudokuBox

@Composable
fun SudokuBoard(
    cells: Array<Array<Cell>>,
    onCellClick: (Int, Int) -> Unit,
    screenSize: IntSize
) {
    val density = LocalDensity.current
    val screenWidth = with(density) { screenSize.width.toDp() } - 48.dp
    val boardWidth = if (screenWidth <= 450.dp) screenWidth else 450.dp
    val cellSize = boardWidth / 9

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .width(boardWidth)
        ) {
            for (boxRow in 0..2) {
                Row {
                    for (boxCol in 0..2) {
                        SudokuBox(
                            cells = cells,
                            boxRow = boxRow,
                            boxCol = boxCol,
                            onCellClick = onCellClick,
                            cellSize = cellSize
                        )
                        if (boxCol < 2) {
                            Divider(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(cellSize * 3),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                if (boxRow < 2) {
                    Divider(
                        modifier = Modifier
                            .height(2.dp)
                            .width(boardWidth),
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}