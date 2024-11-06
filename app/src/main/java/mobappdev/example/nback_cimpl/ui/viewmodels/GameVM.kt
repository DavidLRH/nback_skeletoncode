package mobappdev.example.nback_cimpl.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mobappdev.example.nback_cimpl.GameApplication
import mobappdev.example.nback_cimpl.NBackHelper
import mobappdev.example.nback_cimpl.data.UserPreferencesRepository
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.cancel
import mobappdev.example.nback_cimpl.R
import kotlin.random.Random

/**
 * This is the GameViewModel.
 *
 * It is good practice to first make an interface, which acts as the blueprint
 * for your implementation. With this interface we can create fake versions
 * of the viewmodel, which we can use to test other parts of our app that depend on the VM.
 *
 * Our viewmodel itself has functions to start a game, to specify a gametype,
 * and to check if we are having a match
 *
 * Date: 25-08-2023
 * Version: Version 1.0
 * Author: Yeetivity
 *
 */


interface GameViewModel {
    val gameState: StateFlow<GameState>
    val score: StateFlow<Int>
    val highscore: StateFlow<Int>
    val nBack: Int

    fun setGameType(gameType: GameType)
    fun startGame()

    fun checkMatch()
    fun goToHomeScreen()
}

class GameVM(
    private val userPreferencesRepository: UserPreferencesRepository
) : GameViewModel, ViewModel() {


    private val _gameState = MutableStateFlow(GameState())
    override val gameState: StateFlow<GameState>
        get() = _gameState.asStateFlow()

    private val _score = MutableStateFlow(0)
    override val score: StateFlow<Int>
        get() = _score

    private val _highscore = MutableStateFlow(0)
    override val highscore: StateFlow<Int>
        get() = _highscore

    // nBack is currently hardcoded
    override val nBack: Int = 2

    private var job: Job? = null  // coroutine job for the game event
    private val eventInterval: Long = 2000L  // 2000 ms (2s)

    private val nBackHelper = NBackHelper()  // Helper that generate the event array
    private var events = emptyArray<Int>()  // Array with all events

    private lateinit var soundPool: SoundPool
    private val soundMap = mutableMapOf<Int, Int>()


    private val _gameOn = MutableStateFlow(false)
    val gameOn: StateFlow<Boolean> = _gameOn.asStateFlow()

    init {
        // Initialize SoundPool
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(3) // Set the maximum number of simultaneous streams
            .setAudioAttributes(audioAttributes)
            .build()

        loadSounds() // Load sounds when the VM is initialized

        // Load high score from repository
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }

    }

    private fun playSound(eventValue: Int) {
        // Play the corresponding sound based on eventValue
        soundMap[eventValue]?.let {
            soundPool.play(it, 1f, 1f, 1, 0, 1f) // Play the sound
        }
    }

    override fun setGameType(gameType: GameType) {
        // update the gametype in the gamestate
        _gameState.value = _gameState.value.copy(gameType = gameType)
    }

    override fun startGame() {


        job = viewModelScope.launch {
            when (gameState.value.gameType) {
                GameType.Audio -> runAudioGame()
                GameType.AudioVisual -> runAudioVisualGame()
                GameType.Visual -> runVisualGame(events)
            }
            // Todo: update the highscore
            updateHighScore()
            goToHomeScreen()
        }
    }

    override fun goToHomeScreen() {
        _gameOn.value = false
        _gameState.value = _gameState.value.copy(gameEnded = true)
        job?.cancel()
    }

    private var matchChecked : Boolean = false  // Reset for each new event



    override fun checkMatch() {

    }

    private fun playRandomSound() {
        // Generate a random index between 0 and 2 to select one of the sounds
        val randomIndex = Random.nextInt(0, 3) // Random number between 0 and 2
        soundMap[randomIndex]?.let {
            soundPool.play(it, 1f, 1f, 1, 0, 1f) // Play the selected random sound
        }
    }

    private suspend fun runAudioGame() {

    }

    private suspend fun runVisualGame(events: Array<Int>) {
        for ((index, value) in events.withIndex()) {
            matchChecked = false
            _gameState.value = _gameState.value.copy(eventValue = value, eventNumber = index)
            delay(eventInterval)
            if (!_gameOn.value) break
        }
        goToHomeScreen()
    }

    private fun updateHighScore() {
        if (_score.value > _highscore.value) {
            _highscore.value = _score.value
            Log.d("GameVM", "New High Score: ${_highscore.value}") // Log new high score
            viewModelScope.launch {
                userPreferencesRepository.saveHighScore(_highscore.value)
            }
        }
    }

    private fun runAudioVisualGame() {
        // Todo: Make work for Higher grade
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as GameApplication)
                GameVM(application.userPreferencesRespository)
            }
        }
    }

    init {
        // Code that runs during creation of the vm
        viewModelScope.launch {
            userPreferencesRepository.highscore.collect {
                _highscore.value = it
            }
        }
    }
}

// Class with the different game types
enum class GameType {
    Audio,
    Visual,
    AudioVisual
}

data class GameState(
    // You can use this state to push values from the VM to your UI.
    val gameType: GameType = GameType.Visual,  // Type of the game
    val eventValue: Int = -1,  // The value of the array string
    val eventNumber: Int = 0,
    val gameEnded: Boolean = false,
    val isMatchButtonError: Boolean = false
)

class FakeVM : GameViewModel {
    override val gameState: StateFlow<GameState>
        get() = MutableStateFlow(GameState()).asStateFlow()
    override val score: StateFlow<Int>
        get() = MutableStateFlow(2).asStateFlow()
    override val highscore: StateFlow<Int>
        get() = MutableStateFlow(42).asStateFlow()
    override val nBack: Int
        get() = 2

    override fun setGameType(gameType: GameType) {
    }

    override fun startGame() {
    }

    override fun goToHomeScreen() {}
    override fun checkMatch() {
    }
}