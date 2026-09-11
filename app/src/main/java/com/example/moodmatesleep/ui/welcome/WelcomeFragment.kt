package com.example.moodmatesleep.ui.welcome

import android.app.TimePickerDialog
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
import com.example.moodmatesleep.data.entity.Habit
import com.example.moodmatesleep.data.entity.UserProfile
import com.example.moodmatesleep.databinding.FragmentWelcomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class WelcomeFragment : Fragment() {

    private var _binding: FragmentWelcomeBinding? = null

    private val binding get() = _binding!!

    private var selectedBedtimeHour = 22
    private var selectedBedtimeMinute = 30

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentWelcomeBinding.inflate(
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
        super.onViewCreated(view, savedInstanceState)

        setupBedtimePicker()

        binding.btnStartJourney.setOnClickListener {
            saveUserSetup()
        }
    }

    private fun setupBedtimePicker() {

        binding.bedtimeContainer.setOnClickListener {

            val dialog = TimePickerDialog(
                requireContext(),
                { _, hourOfDay, minute ->

                    selectedBedtimeHour = hourOfDay
                    selectedBedtimeMinute = minute

                    binding.tvBedtime.text =
                        formatTime(hourOfDay, minute)
                },
                selectedBedtimeHour,
                selectedBedtimeMinute,
                false
            )

            dialog.show()
        }
    }

    private fun saveUserSetup() {

        val name =
            binding.etName.text
                ?.toString()
                ?.trim()
                .orEmpty()

        if (name.isEmpty()) {

            binding.nameInputLayout.error =
                "Please enter your name"

            binding.etName.requestFocus()

            return
        }

        binding.nameInputLayout.error = null

        val sleepGoalMinutes =
            when (binding.chipGroupSleepGoal.checkedChipId) {

                R.id.chip6Hours -> 6 * 60

                R.id.chip7Hours -> 7 * 60

                R.id.chip8Hours -> 8 * 60

                R.id.chip9Hours -> 9 * 60

                else -> 8 * 60
            }

        val bedtime =
            binding.tvBedtime.text.toString()

        val profile = UserProfile(
            id = 1,
            name = name,
            sleepGoalMinutes = sleepGoalMinutes,
            preferredBedtime = bedtime,
            bedtimeReminderEnabled = true,
            morningReminderEnabled = true,
            defaultSoundTimerMinutes = 30
        )

        saveProfileToDatabase(profile)
    }

    private fun saveProfileToDatabase(
        profile: UserProfile
    ) {

        binding.btnStartJourney.isEnabled = false

        val database =
            MoodMateDatabase.getDatabase(requireContext())

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                withContext(Dispatchers.IO) {

                    database.userProfileDao()
                        .insertProfile(profile)

                    insertDefaultHabitsIfNeeded(database)
                }

                if (!isAdded) {
                    return@launch
                }

                Toast.makeText(
                    requireContext(),
                    "Welcome to MoodMate Sleep!",
                    Toast.LENGTH_SHORT
                ).show()

                findNavController().navigate(
                    R.id.action_welcomeFragment_to_homeFragment
                )

            } catch (exception: Exception) {

                binding.btnStartJourney.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "Unable to save setup. Please try again.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private suspend fun insertDefaultHabitsIfNeeded(
        database: MoodMateDatabase
    ) {

        val habitCount =
            database.habitDao().getHabitCount()

        if (habitCount > 0) {
            return
        }

        val defaultHabits = listOf(

            Habit(
                name = "Put phone away before bed",
                category = "BEDTIME"
            ),

            Habit(
                name = "Drink water",
                category = "BEDTIME"
            ),

            Habit(
                name = "Read for 10 minutes",
                category = "BEDTIME"
            ),

            Habit(
                name = "2-minute breathing exercise",
                category = "BEDTIME"
            ),

            Habit(
                name = "Prepare for tomorrow",
                category = "BEDTIME"
            ),

            Habit(
                name = "Drink water",
                category = "MORNING"
            ),

            Habit(
                name = "Stretch",
                category = "MORNING"
            ),

            Habit(
                name = "Eat breakfast",
                category = "MORNING"
            )
        )

        database.habitDao()
            .insertHabits(defaultHabits)
    }

    private fun formatTime(
        hour: Int,
        minute: Int
    ): String {

        val calendar =
            Calendar.getInstance()

        calendar.set(
            Calendar.HOUR_OF_DAY,
            hour
        )

        calendar.set(
            Calendar.MINUTE,
            minute
        )

        val formatter =
            SimpleDateFormat(
                "h:mm a",
                Locale.getDefault()
            )

        return formatter.format(
            calendar.time
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}