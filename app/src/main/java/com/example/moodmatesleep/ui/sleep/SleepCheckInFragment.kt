package com.example.moodmatesleep.ui.sleep

import android.app.TimePickerDialog
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
import com.example.moodmatesleep.databinding.FragmentSleepCheckInBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class SleepCheckInFragment : Fragment() {

    private var _binding: FragmentSleepCheckInBinding? = null
    private val binding get() = _binding!!

    private var currentSession: SleepSession? = null

    private var editedBedtimeMillis: Long = 0L
    private var editedWakeTimeMillis: Long = 0L

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
            FragmentSleepCheckInBinding.inflate(
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

        loadCurrentSession()

        binding.bedtimeBox.setOnClickListener {
            showBedtimePicker()
        }

        binding.wakeTimeBox.setOnClickListener {
            showWakeTimePicker()
        }

        binding.btnContinue.setOnClickListener {
            saveSleepCheckIn()
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

            val session =
                withContext(Dispatchers.IO) {

                    database
                        .sleepSessionDao()
                        .getSessionById(sessionId)
                }

            if (session == null) {

                Toast.makeText(
                    requireContext(),
                    "Sleep session could not be loaded.",
                    Toast.LENGTH_SHORT
                ).show()

                return@launch
            }

            currentSession = session

            editedBedtimeMillis =
                session.bedtimeMillis

            editedWakeTimeMillis =
                session.wakeTimeMillis
                    ?: System.currentTimeMillis()

            displaySleepSummary()
        }
    }

    private fun displaySleepSummary() {

        binding.tvBedtimeValue.text =
            formatTime(
                editedBedtimeMillis
            )

        binding.tvWakeTimeValue.text =
            formatTime(
                editedWakeTimeMillis
            )

        val duration =
            calculateDurationMinutes(
                editedBedtimeMillis,
                editedWakeTimeMillis
            )

        binding.tvDurationValue.text =
            formatDuration(duration)
    }

    private fun showBedtimePicker() {

        if (editedBedtimeMillis == 0L) {
            return
        }

        val calendar =
            Calendar.getInstance().apply {
                timeInMillis =
                    editedBedtimeMillis
            }

        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->

                calendar.set(
                    Calendar.HOUR_OF_DAY,
                    hour
                )

                calendar.set(
                    Calendar.MINUTE,
                    minute
                )

                calendar.set(
                    Calendar.SECOND,
                    0
                )

                editedBedtimeMillis =
                    calendar.timeInMillis

                displaySleepSummary()
            },
            calendar.get(
                Calendar.HOUR_OF_DAY
            ),
            calendar.get(
                Calendar.MINUTE
            ),
            false
        ).show()
    }

    private fun showWakeTimePicker() {

        if (editedWakeTimeMillis == 0L) {
            return
        }

        val calendar =
            Calendar.getInstance().apply {
                timeInMillis =
                    editedWakeTimeMillis
            }

        TimePickerDialog(
            requireContext(),
            { _, hour, minute ->

                calendar.set(
                    Calendar.HOUR_OF_DAY,
                    hour
                )

                calendar.set(
                    Calendar.MINUTE,
                    minute
                )

                calendar.set(
                    Calendar.SECOND,
                    0
                )

                var newWakeTime =
                    calendar.timeInMillis

                /*
                 If wake clock time becomes earlier than
                 bedtime, assume wake-up happened next day.
                */
                if (
                    newWakeTime <=
                    editedBedtimeMillis
                ) {

                    newWakeTime +=
                        24 * 60 * 60 * 1000L
                }

                editedWakeTimeMillis =
                    newWakeTime

                displaySleepSummary()
            },
            calendar.get(
                Calendar.HOUR_OF_DAY
            ),
            calendar.get(
                Calendar.MINUTE
            ),
            false
        ).show()
    }

    private fun saveSleepCheckIn() {

        val session =
            currentSession ?: return

        val sleepQuality =
            when (
                binding
                    .chipGroupQuality
                    .checkedChipId
            ) {

                R.id.chipVeryPoor -> 1

                R.id.chipPoor -> 2

                R.id.chipOkay -> 3

                R.id.chipGood -> 4

                R.id.chipGreat -> 5

                else -> null
            }

        if (sleepQuality == null) {

            Toast.makeText(
                requireContext(),
                "Please select your sleep quality.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val disturbances =
            mutableListOf<String>()

        if (binding.chipStress.isChecked) {
            disturbances.add("Stress")
        }

        if (binding.chipPhone.isChecked) {
            disturbances.add("Phone")
        }

        if (binding.chipNoise.isChecked) {
            disturbances.add("Noise")
        }

        if (binding.chipCaffeine.isChecked) {
            disturbances.add("Caffeine")
        }

        if (binding.chipStudy.isChecked) {
            disturbances.add("Study")
        }

        if (
            binding
                .chipTemperature
                .isChecked
        ) {
            disturbances.add(
                "Temperature"
            )
        }

        val note =
            binding.etSleepNote.text
                ?.toString()
                ?.trim()
                .orEmpty()

        val duration =
            calculateDurationMinutes(
                editedBedtimeMillis,
                editedWakeTimeMillis
            )

        if (duration < 0) {

            Toast.makeText(
                requireContext(),
                "Please check your sleep times.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val dateFormatter =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )

        val updatedSession =
            session.copy(
                sleepDate =
                    dateFormatter.format(
                        Date(
                            editedBedtimeMillis
                        )
                    ),

                bedtimeMillis =
                    editedBedtimeMillis,

                wakeTimeMillis =
                    editedWakeTimeMillis,

                durationMinutes =
                    duration,

                sleepQuality =
                    sleepQuality,

                disturbances =
                    disturbances.joinToString(
                        ","
                    ),

                note =
                    note.ifEmpty {
                        null
                    }
            )

        updateDatabase(
            updatedSession
        )
    }

    private fun updateDatabase(
        updatedSession: SleepSession
    ) {

        binding.btnContinue.isEnabled =
            false

        val database =
            MoodMateDatabase.getDatabase(
                requireContext()
            )

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                withContext(
                    Dispatchers.IO
                ) {

                    database
                        .sleepSessionDao()
                        .updateSession(
                            updatedSession
                        )
                }

                currentSession =
                    updatedSession

                /*
                 Stage 10 destination.
                */
                findNavController().navigate(
                    R.id.action_sleepCheckInFragment_to_morningCheckInFragment
                )

            } catch (
                exception: Exception
            ) {

                binding.btnContinue.isEnabled =
                    true

                Toast.makeText(
                    requireContext(),
                    "Unable to save sleep check-in.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun calculateDurationMinutes(
        bedtime: Long,
        wakeTime: Long
    ): Int {

        return (
                (wakeTime - bedtime) /
                        60000L
                ).toInt()
    }

    private fun formatDuration(
        minutes: Int
    ): String {

        val hours =
            minutes / 60

        val remainingMinutes =
            minutes % 60

        return if (hours > 0) {

            "${hours}h ${remainingMinutes}m"

        } else {

            "${remainingMinutes}m"
        }
    }

    private fun formatTime(
        timeMillis: Long
    ): String {

        val formatter =
            SimpleDateFormat(
                "h:mm a",
                Locale.getDefault()
            )

        return formatter.format(
            Date(timeMillis)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}