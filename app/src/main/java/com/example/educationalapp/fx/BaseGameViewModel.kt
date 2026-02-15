package com.example.educationalapp.fx

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * A base class for game view models. It encapsulates common game logic such as
 * managing the score, tracking elapsed time, and handling pause/resume state.
 *
 * Games that require timers or scores should extend this class and call
 * [onGameStart] when the game begins. Derived classes can override
 * [onTimerTick] to react to the passage of time. The timer is suspended when
 * the game is paused via [pauseGame] and resumed via [resumeGame].
 */
abstract class BaseGameViewModel : ViewModel() {
    private val _score = MutableStateFlow(0)
    /** Publicly exposed score state. */
    val score: StateFlow<Int> get() = _score

    private val _elapsedMillis = MutableStateFlow(0L)
    /** Time elapsed since the game started in milliseconds. */
    val elapsedMillis: StateFlow<Long> get() = _elapsedMillis

    private val _isPaused = MutableStateFlow(false)
    /** Indicates whether the game is currently paused. */
    val isPaused: StateFlow<Boolean> get() = _isPaused

    private var timerJob: Job? = null

    /** Call this to begin the game timer. It will invoke [onTimerTick] on every second. */
    fun startGame() {
        if (timerJob != null) return
        timerJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(1000L)
                if (!_isPaused.value) {
                    _elapsedMillis.update { it + 1000L }
                    onTimerTick(_elapsedMillis.value)
                }
            }
        }
    }

    /** Adds the given amount to the score. Use negative values to subtract. */
    protected fun addScore(amount: Int) {
        _score.update { it + amount }
    }

    /** Pauses the game timer. */
    fun pauseGame() {
        _isPaused.value = true
    }

    /** Resumes the game timer if it was previously paused. */
    fun resumeGame() {
        _isPaused.value = false
    }

    /** Stops the timer and resets all state. */
    open fun resetGame() {
        timerJob?.cancel()
        timerJob = null
        _elapsedMillis.value = 0L
        _score.value = 0
        _isPaused.value = false
    }

    /** Called every second while the game is running and not paused. */
    protected open fun onTimerTick(elapsed: Long) {}
}