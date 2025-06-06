package hung.deptrai.simplesudoku.ui.component.smaller

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import hung.deptrai.simplesudoku.common.Cell
import hung.deptrai.simplesudoku.ui.component.SudokuBox

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