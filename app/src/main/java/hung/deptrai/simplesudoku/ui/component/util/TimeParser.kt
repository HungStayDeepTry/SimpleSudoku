package hung.deptrai.simplesudoku.ui.component.util

fun parseTimeElapsed(time: String): Long {
    val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
    return when (parts.size) {
        2 -> parts[0] * 60L + parts[1] // mm:ss
        3 -> parts[0] * 3600L + parts[1] * 60L + parts[2] // hh:mm:ss
        else -> 0L
    }
}