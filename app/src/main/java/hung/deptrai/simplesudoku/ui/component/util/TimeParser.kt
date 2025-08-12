package hung.deptrai.simplesudoku.ui.component.util

import android.annotation.SuppressLint

fun parseTimeElapsed(time: String): Long {
    val parts = time.split(":").map { it.toIntOrNull() ?: 0 }
    return when (parts.size) {
        2 -> parts[0] * 60L + parts[1]
        3 -> parts[0] * 3600L + parts[1] * 60L + parts[2]
        else -> 0L
    }
}


@SuppressLint("DefaultLocale")
fun formatTimeElapsed(timeElapsedSeconds: Long): String {
    val hours = timeElapsedSeconds / 3600
    val minutes = (timeElapsedSeconds % 3600) / 60
    val seconds = timeElapsedSeconds % 60
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

fun parseTimeElapsedFromText(timeString: String): Long {
    val parts = timeString.split(":")
    return when (parts.size) {
        3 -> {
            val hours = parts[0].toLongOrNull() ?: 0L
            val minutes = parts[1].toLongOrNull() ?: 0L
            val seconds = parts[2].toLongOrNull() ?: 0L
            hours * 3600 + minutes * 60 + seconds
        }

        2 -> {
            val minutes = parts[0].toLongOrNull() ?: 0L
            val seconds = parts[1].toLongOrNull() ?: 0L
            minutes * 60 + seconds
        }

        else -> 0L
    }
}