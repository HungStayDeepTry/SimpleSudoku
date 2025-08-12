package hung.deptrai.simplesudoku.model

import hung.deptrai.simplesudoku.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

class GameTimer @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope
) {
    private var startTime = 0L
    private var running = false
    private var timerJob: Job? = null

    private val _accumulated = MutableStateFlow(0L)
    val accumulated: StateFlow<Long> get() = _accumulated

    fun start() {
        _accumulated.value = 0L
        startTime = System.currentTimeMillis()
        running = true
        startTimer()
    }

    fun resume() {
        startTime = System.currentTimeMillis()
        running = true
        startTimer()
    }

    fun pause(): Long {
        if (running) {
            val now = System.currentTimeMillis()
            _accumulated.value += now - startTime
            running = false
            timerJob?.cancel()
        }
        return _accumulated.value
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && running) {
                val now = System.currentTimeMillis()
                _accumulated.value += (now - startTime)
                startTime = now
                delay(1000L)
            }
        }
    }

    fun startFromInitialTime(initTime: Long = 0L) {
        _accumulated.value = initTime * 1000
        resume()
    }
}
