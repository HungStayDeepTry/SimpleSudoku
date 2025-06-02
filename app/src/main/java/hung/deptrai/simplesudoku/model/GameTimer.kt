package hung.deptrai.simplesudoku.model

import hung.deptrai.simplesudoku.di.ApplicationScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class GameTimer @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope
){

    private var startTime: Long = 0L
    private var accumulated: Long = 0L
    private var timerJob: Job? = null

    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> get() = _elapsedTime

    private var running: Boolean = false

    fun start() {
        startTime = System.currentTimeMillis()
        accumulated = 0L
        running = true
        startTimer()
    }

    fun pause() {
        if (running) {
            accumulated += System.currentTimeMillis() - startTime
            running = false
            timerJob?.cancel()
        }
    }

    fun resume() {
        if (!running) {
            startTime = System.currentTimeMillis()
            running = true
            startTimer()
        }
    }

    fun getElapsedTime(): Long {
        return if (running) {
            accumulated + (System.currentTimeMillis() - startTime)
        } else {
            accumulated
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                _elapsedTime.value = getElapsedTime()
                delay(1000L)
            }
        }
    }
}
