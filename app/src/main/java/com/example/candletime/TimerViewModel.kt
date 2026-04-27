// TimerViewModel.kt
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import android.os.CountDownTimer
import com.example.candletime.database.CandleDatabase
import com.example.candletime.database.SessionRepository
import kotlinx.coroutines.launch
import android.media.MediaPlayer
import com.example.candletime.R
import com.example.candletime.models.CandleSession
import kotlinx.coroutines.delay


enum class TimerState { IDLE, RUNNING, FINISHED }

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = SessionRepository(
        CandleDatabase.getInstance(application).sessionDao()
    )


    val allSessions = repo.allSessions
    val totalBurned = repo.totalBurned
    val completedCount = repo.completedCount
    val totalCount = repo.totalCount

    private var countDownTimer: CountDownTimer? = null

    var totalTimeSeconds by mutableStateOf(0L)
    var remainingTimeSeconds by mutableStateOf(0L)
    var timerState by mutableStateOf(TimerState.IDLE)
    var isSoundEnabled by mutableStateOf(true)
        private set

    private var sessionStartMs = 0L

    val isRunning get() = timerState == TimerState.RUNNING

    val candleProgress: Float
        get() = if (totalTimeSeconds > 0)
            (remainingTimeSeconds.toFloat() / totalTimeSeconds.toFloat()).coerceIn(0f, 1f)
        else 1f


    private var mediaPlayer: MediaPlayer? = null
    private var mediaPlayer_2: MediaPlayer? = null


    fun startTimer(durationSeconds: Long) {
        countDownTimer?.cancel()
        totalTimeSeconds = durationSeconds
        remainingTimeSeconds = durationSeconds
        sessionStartMs = System.currentTimeMillis()
        timerState = TimerState.RUNNING
        viewModelScope.launch {
            playfreshort()
            delay(1000L)
            playsound()
        }

        countDownTimer = object : CountDownTimer(durationSeconds * 1000L, 1000L) {
            override fun onTick(ms: Long) {
                remainingTimeSeconds = ms / 1000L
            }

            override fun onFinish() {
                remainingTimeSeconds = 0
                timerState = TimerState.FINISHED
                saveSession(completed = true)
            }
        }.start()
    }

    fun reset(savePartial: Boolean = false) {
        countDownTimer?.cancel()
        if (savePartial && totalTimeSeconds > 0) saveSession(completed = false)

        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null

        mediaPlayer_2?.stop()
        mediaPlayer_2?.release()
        mediaPlayer_2 = null

        totalTimeSeconds = 0L
        remainingTimeSeconds = 0L
        timerState = TimerState.IDLE
    }

    private fun saveSession(completed: Boolean) {
        val elapsed = totalTimeSeconds - remainingTimeSeconds
        viewModelScope.launch {
            repo.save(
                CandleSession(
                    plannedSeconds = totalTimeSeconds,
                    actualSeconds = if (completed) totalTimeSeconds else elapsed,
                    startedAt = sessionStartMs,
                    completed = completed
                )
            )
        }
    }


    fun deleteSession(session: CandleSession) {
        viewModelScope.launch { repo.delete(session) }
    }

    fun deleteSessions(ids: List<Long>) {
        viewModelScope.launch { repo.deleteMultiple(ids) }
    }

    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
    fun toggleSound(enabled: Boolean) {
        isSoundEnabled = enabled
        if (!enabled) {
            mediaPlayer?.pause()
            mediaPlayer_2?.pause()
        } else {
            if (timerState == TimerState.RUNNING) {
                mediaPlayer?.start()
            }
        }
    }

    private fun playsound() {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer.create(getApplication(), R.raw.sound)

        if (isSoundEnabled) {
            mediaPlayer?.start()
            mediaPlayer?.isLooping = true
        }

        mediaPlayer?.setOnCompletionListener {
            if (!it.isLooping) {
                it.release()
                mediaPlayer = null
            }
        }
    }
    private fun playfreshort() {
        mediaPlayer_2?.release()

        mediaPlayer_2 = MediaPlayer.create(getApplication(), R.raw.fireshort)
        mediaPlayer_2?.start()

        mediaPlayer_2?.setOnCompletionListener {
            it.release()
            mediaPlayer_2 = null
        }
    }

}