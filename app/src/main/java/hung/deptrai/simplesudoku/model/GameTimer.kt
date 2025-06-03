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

    private val _currentTimeDisplay = MutableStateFlow("00:00")
    val currentTimeDisplay: StateFlow<String> get() = _currentTimeDisplay

    fun start() {
        startTime = System.currentTimeMillis()
        accumulated = 0L
        running = true
        startTimer()
    }

    fun pause(): String {
        if (running) {
            accumulated += System.currentTimeMillis() - startTime
            running = false
            timerJob?.cancel()
        }
        return formatTime(getTotalElapsed())
    }

    fun resume() {
        if (!running) {
            startTime = System.currentTimeMillis()
            running = true
            startTimer()
        }
    }

    fun stop(): String {
        pause()
        val finalTime = formatTime(getTotalElapsed())
        reset()
        return finalTime
    }

    fun reset() {
        timerJob?.cancel()
        startTime = 0L
        accumulated = 0L
        running = false
        _currentTimeDisplay.value = "00:00"
    }

    fun getCurrentTimeString(): String {
        return formatTime(getTotalElapsed())
    }

    private fun getTotalElapsed(): Long {
        return if (running) {
            accumulated + (System.currentTimeMillis() - startTime)
        } else {
            accumulated
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && running) {
                _currentTimeDisplay.value = formatTime(getTotalElapsed())
                delay(1000L)
            }
        }
    }

    private fun formatTime(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }
}