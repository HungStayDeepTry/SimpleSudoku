package hung.deptrai.simplesudoku.model

import hung.deptrai.simplesudoku.di.ApplicationScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GameTimer @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope
) {
    private var startTime: Long = 0L
    private var accumulated: Long = 0L
    private var timerJob: Job? = null
    private var running: Boolean = false

    private val _currentTime = MutableStateFlow(0L)
    val currentTime: StateFlow<Long> get() = _currentTime

    fun start() {
        startTime = System.currentTimeMillis()
        accumulated = 0L
        running = true
        startTimer()
    }

    fun resume(startFrom: Long, accumulatedMillis: Long) {
        startTime = startFrom
        accumulated = accumulatedMillis
        running = true
        startTimer()
    }

    fun pause(): Pair<Long, String> {
        if (running) {
            accumulated += System.currentTimeMillis() - startTime
            running = false
            timerJob?.cancel()
            timerJob = null
        }
        val millis = accumulated
        return millis to formatTime(millis)
    }

    fun getElapsedMillis(): Long =
        if (running) accumulated + (System.currentTimeMillis() - startTime) else accumulated

    fun getFormattedElapsed(): String = formatTime(getElapsedMillis())

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && running) {
                _currentTime.value = getElapsedMillis()
                delay(1000L)
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }
}
