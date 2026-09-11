package com.example.moodmatesleep.ui.morning

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moodmatesleep.R
import com.example.moodmatesleep.data.database.MoodMateDatabase
import com.example.moodmatesleep.data.entity.SleepSession
import com.example.moodmatesleep.databinding.FragmentMorningCheckInBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MorningCheckInFragment : Fragment() {

    private var _binding: FragmentMorningCheckInBinding? = null

    private val binding get() = _binding!!

    private var currentSession: SleepSession? = null

    private val preferences by lazy {

        requireContext().getSharedPreferences(
            "moodmate_preferences",
            Context.MODE_PRIVATE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentMorningCheckInBinding.inflate(
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

        loadCurrentSession()

        setupEnergySlider()

        binding.btnFinishCheckIn.setOnClickListener {

            saveMorningCheckIn()
        }
    }

    private fun setupEnergySlider() {

        binding.tvEnergyValue.text =
            "${binding.sliderEnergy.value.toInt()}%"

        binding.sliderEnergy.addOnChangeListener {
                _,
                value,
                _ ->

            binding.tvEnergyValue.text =
                "${value.toInt()}%"
        }
    }

    private fun loadCurrentSession() {

        val sessionId =
            preferences.getLong(
                "active_session_id",
                -1L
            )

        if (sessionId == -1L) {

            Toast.makeText(
                requireContext(),
                "No active sleep session found.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val database =
            MoodMateDatabase.getDatabase(
                requireContext()
            )

        viewLifecycleOwner.lifecycleScope.launch {

            currentSession =
                withContext(Dispatchers.IO) {

                    database
                        .sleepSessionDao()
                        .getSessionById(sessionId)
                }

            if (currentSession == null) {

                Toast.makeText(
                    requireContext(),
                    "Sleep session could not be loaded.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun saveMorningCheckIn() {

        val session =
            currentSession ?: run {

                Toast.makeText(
                    requireContext(),
                    "Sleep session is not ready yet.",
                    Toast.LENGTH_SHORT
                ).show()

                return
            }

        val selectedMood =
            when (
                binding
                    .chipGroupMood
                    .checkedChipId
            ) {

                R.id.chipRefreshed ->
                    "Refreshed"

                R.id.chipHappy ->
                    "Happy"

                R.id.chipCalm ->
                    "Calm"

                R.id.chipNeutral ->
                    "Neutral"

                R.id.chipTired ->
                    "Tired"

                R.id.chipStressed ->
                    "Stressed"

                else ->
                    null
            }

        if (selectedMood == null) {

            Toast.makeText(
                requireContext(),
                "Please select your morning mood.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val energyLevel =
            binding.sliderEnergy.value.toInt()

        val moodFactors =
            mutableListOf<String>()

        if (
            binding
                .chipFactorSleep
                .isChecked
        ) {
            moodFactors.add("Sleep")
        }

        if (
            binding
                .chipFactorStudy
                .isChecked
        ) {
            moodFactors.add("Study")
        }

        if (
            binding
                .chipFactorWork
                .isChecked
        ) {
            moodFactors.add("Work")
        }

        if (
            binding
                .chipFactorExercise
                .isChecked
        ) {
            moodFactors.add("Exercise")
        }

        if (
            binding
                .chipFactorScreenTime
                .isChecked
        ) {
            moodFactors.add("Screen Time")
        }

        if (
            binding
                .chipFactorSocial
                .isChecked
        ) {
            moodFactors.add("Social")
        }

        val updatedSession =
            session.copy(

                mood =
                    selectedMood,

                energyLevel =
                    energyLevel,

                moodFactors =
                    moodFactors.joinToString(",")
            )

        updateSessionAndFinish(
            updatedSession
        )
    }

    private fun updateSessionAndFinish(
        updatedSession: SleepSession
    ) {

        binding.btnFinishCheckIn.isEnabled =
            false

        val database =
            MoodMateDatabase.getDatabase(
                requireContext()
            )

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                withContext(Dispatchers.IO) {

                    database
                        .sleepSessionDao()
                        .updateSession(
                            updatedSession
                        )
                }

                /*
                 Sleep + Morning check-in
                 completely finished.
                */
                preferences
                    .edit()
                    .remove(
                        "active_session_id"
                    )
                    .apply()

                Toast.makeText(
                    requireContext(),
                    "Morning check-in saved!",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigate(
                    R.id.action_morningCheckInFragment_to_homeFragment
                )

            } catch (
                exception: Exception
            ) {

                binding
                    .btnFinishCheckIn
                    .isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "Unable to save morning check-in.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}