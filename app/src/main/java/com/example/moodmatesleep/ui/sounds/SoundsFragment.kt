package com.example.moodmatesleep.ui.sounds

import android.media.MediaPlayer
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.moodmatesleep.R
import com.example.moodmatesleep.databinding.FragmentSoundsBinding

class SoundsFragment : Fragment() {

    private var _binding: FragmentSoundsBinding? = null
    private val binding get() = _binding!!

    private var mediaPlayer: MediaPlayer? = null

    private var selectedSound: String? = null
    private var selectedResourceId: Int? = null

    private var sleepTimer: CountDownTimer? = null
    private var selectedTimerMinutes: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentSoundsBinding.inflate(
                inflater,
                container,
                false
            )

        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(
            view,
            savedInstanceState
        )

        setupSoundCards()
        setupPlayerButton()
        setupTimer()
        setupStopButton()
    }

    private fun setupSoundCards() {

        binding.cardRain.setOnClickListener {

            selectSound(
                name = "Gentle Rain",
                resourceId = R.raw.rain_sound
            )
        }

        binding.cardFireplace.setOnClickListener {

            selectSound(
                name = "Fireplace",
                resourceId = R.raw.fireplace_sound
            )
        }
    }

    private fun selectSound(
        name: String,
        resourceId: Int
    ) {

        stopCurrentPlayer(
            resetSelection = false
        )

        selectedSound = name
        selectedResourceId = resourceId

        binding.tvNowPlaying.text =
            name

        binding.tvPlayerStatus.text =
            "Ready to play"

        binding.btnPlayPause.isEnabled =
            true

        binding.btnPlayPause.text =
            "Play"

        updateSoundStatuses()
    }

    private fun setupPlayerButton() {

        binding.btnPlayPause
            .setOnClickListener {

                val player =
                    mediaPlayer

                if (
                    player != null &&
                    player.isPlaying
                ) {

                    player.pause()

                    binding.btnPlayPause.text =
                        "Play"

                    binding.tvPlayerStatus.text =
                        "Paused"

                    updateSoundStatuses()

                } else {

                    playSelectedSound()
                }
            }
    }

    private fun playSelectedSound() {

        val resourceId =
            selectedResourceId

        if (resourceId == null) {

            Toast.makeText(
                requireContext(),
                "Please choose a sleep sound.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        if (mediaPlayer == null) {

            mediaPlayer =
                MediaPlayer.create(
                    requireContext(),
                    resourceId
                )

            mediaPlayer?.apply {

                isLooping = true

                // Maximum MediaPlayer volume
                setVolume(
                    1.0f,
                    1.0f
                )
            }
        }

        mediaPlayer?.start()

        binding.btnPlayPause.text =
            "Pause"

        binding.tvPlayerStatus.text =
            "Playing"

        updateSoundStatuses()

        startTimerIfNeeded()
    }

    private fun setupTimer() {

        binding.chipGroupTimer
            .setOnCheckedStateChangeListener {
                    _,
                    checkedIds ->

                val checkedId =
                    checkedIds.firstOrNull()
                        ?: R.id.chipTimerOff

                selectedTimerMinutes =
                    when (checkedId) {

                        R.id.chipTimer15 ->
                            15

                        R.id.chipTimer30 ->
                            30

                        R.id.chipTimer60 ->
                            60

                        else ->
                            0
                    }

                sleepTimer?.cancel()
                sleepTimer = null

                if (
                    selectedTimerMinutes == 0
                ) {

                    binding.tvTimerStatus.text =
                        "Timer off"

                } else {

                    binding.tvTimerStatus.text =
                        "$selectedTimerMinutes minute timer selected"

                    if (
                        mediaPlayer?.isPlaying ==
                        true
                    ) {

                        startTimerIfNeeded()
                    }
                }
            }
    }

    private fun startTimerIfNeeded() {

        sleepTimer?.cancel()

        if (
            selectedTimerMinutes <= 0
        ) {

            binding.tvTimerStatus.text =
                "Timer off"

            return
        }

        val totalMillis =
            selectedTimerMinutes *
                    60L *
                    1000L

        sleepTimer =
            object :
                CountDownTimer(
                    totalMillis,
                    1000L
                ) {

                override fun onTick(
                    millisUntilFinished: Long
                ) {

                    val totalSeconds =
                        millisUntilFinished /
                                1000L

                    val minutes =
                        totalSeconds / 60L

                    val seconds =
                        totalSeconds % 60L

                    binding.tvTimerStatus.text =
                        String.format(
                            "%02d:%02d remaining",
                            minutes,
                            seconds
                        )
                }

                override fun onFinish() {

                    stopCurrentPlayer(
                        resetSelection = false
                    )

                    binding.tvTimerStatus.text =
                        "Timer finished"

                    Toast.makeText(
                        requireContext(),
                        "Sleep sound stopped.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.start()
    }

    private fun setupStopButton() {

        binding.btnStopSound
            .setOnClickListener {

                stopCurrentPlayer(
                    resetSelection = false
                )

                sleepTimer?.cancel()
                sleepTimer = null

                binding.tvTimerStatus.text =
                    if (
                        selectedTimerMinutes == 0
                    ) {
                        "Timer off"
                    } else {
                        "$selectedTimerMinutes minute timer selected"
                    }
            }
    }

    private fun stopCurrentPlayer(
        resetSelection: Boolean
    ) {

        mediaPlayer?.let {

            if (it.isPlaying) {
                it.stop()
            }

            it.release()
        }

        mediaPlayer = null

        binding.btnPlayPause.text =
            "Play"

        binding.tvPlayerStatus.text =
            if (
                selectedSound == null
            ) {
                "Choose a sound above"
            } else {
                "Stopped"
            }

        if (resetSelection) {

            selectedSound = null
            selectedResourceId = null

            binding.tvNowPlaying.text =
                "Nothing Playing"

            binding.btnPlayPause.isEnabled =
                false
        }

        updateSoundStatuses()
    }

    private fun updateSoundStatuses() {

        val currentlyPlaying =
            mediaPlayer?.isPlaying ==
                    true

        binding.tvRainStatus.text =
            when {

                selectedSound !=
                        "Gentle Rain" ->
                    "Play"

                currentlyPlaying ->
                    "Playing"

                else ->
                    "Selected"
            }

        binding.tvFireplaceStatus.text =
            when {

                selectedSound !=
                        "Fireplace" ->
                    "Play"

                currentlyPlaying ->
                    "Playing"

                else ->
                    "Selected"
            }
    }

    override fun onDestroyView() {

        sleepTimer?.cancel()
        sleepTimer = null

        mediaPlayer?.release()
        mediaPlayer = null

        _binding = null

        super.onDestroyView()
    }
}