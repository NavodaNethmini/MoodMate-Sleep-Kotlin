package com.example.moodmatesleep.ui.profile

import android.Manifest
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moodmatesleep.data.database.MoodMateDatabase
import com.example.moodmatesleep.databinding.FragmentProfileBinding
import com.example.moodmatesleep.reminder.BedtimeReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ProfileFragment : Fragment() {

    private var _binding:
            FragmentProfileBinding? = null

    private val binding
        get() = _binding!!

    private val database by lazy {

        MoodMateDatabase.getDatabase(
            requireContext()
        )
    }

    private val preferences by lazy {

        requireContext().getSharedPreferences(
            "moodmate_preferences",
            Context.MODE_PRIVATE
        )
    }

    private var reminderHour = 22
    private var reminderMinute = 0

    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
                granted ->

            if (granted) {

                enableReminder()

            } else {

                binding
                    .switchBedtimeReminder
                    .isChecked = false

                Toast.makeText(
                    requireContext(),
                    "Notification permission is required for bedtime reminders.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentProfileBinding.inflate(
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

        loadProfile()

        loadReminderSettings()

        setupBackButton()

        setupReminderSwitch()

        setupTimePicker()
    }

    private fun loadProfile() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                val profile =
                    withContext(
                        Dispatchers.IO
                    ) {

                        database
                            .userProfileDao()
                            .getProfile()
                    }

                if (profile != null) {

                    binding.tvProfileName.text =
                        profile.name

                    val hours =
                        profile.sleepGoalMinutes /
                                60

                    val minutes =
                        profile.sleepGoalMinutes %
                                60

                    binding.tvSleepGoal.text =
                        if (minutes == 0) {

                            "$hours hours"

                        } else {

                            "${hours}h ${minutes}m"
                        }

                } else {

                    binding.tvProfileName.text =
                        "MoodMate User"

                    binding.tvSleepGoal.text =
                        "Not set"
                }
            }
    }

    private fun loadReminderSettings() {

        reminderHour =
            preferences.getInt(
                "bedtime_reminder_hour",
                22
            )

        reminderMinute =
            preferences.getInt(
                "bedtime_reminder_minute",
                0
            )

        val enabled =
            preferences.getBoolean(
                "bedtime_reminder_enabled",
                false
            )

        binding
            .switchBedtimeReminder
            .isChecked = enabled

        updateReminderTimeText()
    }

    private fun setupBackButton() {

        binding.btnBackProfile
            .setOnClickListener {

                findNavController()
                    .navigateUp()
            }
    }

    private fun setupReminderSwitch() {

        binding.switchBedtimeReminder
            .setOnCheckedChangeListener {
                    _,
                    isChecked ->

                if (isChecked) {

                    requestNotificationPermissionIfNeeded()

                } else {

                    preferences
                        .edit()
                        .putBoolean(
                            "bedtime_reminder_enabled",
                            false
                        )
                        .apply()

                    BedtimeReminderScheduler.cancel(
                        requireContext()
                    )
                }
            }
    }

    private fun requestNotificationPermissionIfNeeded() {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {

                enableReminder()

            } else {

                notificationPermissionLauncher.launch(
                    Manifest.permission.POST_NOTIFICATIONS
                )
            }

        } else {

            enableReminder()
        }
    }

    private fun enableReminder() {

        preferences
            .edit()
            .putBoolean(
                "bedtime_reminder_enabled",
                true
            )
            .putInt(
                "bedtime_reminder_hour",
                reminderHour
            )
            .putInt(
                "bedtime_reminder_minute",
                reminderMinute
            )
            .apply()

        BedtimeReminderScheduler.schedule(
            requireContext(),
            reminderHour,
            reminderMinute
        )

        Toast.makeText(
            requireContext(),
            "Bedtime reminder enabled.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupTimePicker() {

        binding.layoutReminderTime
            .setOnClickListener {

                TimePickerDialog(
                    requireContext(),
                    {
                            _,
                            hour,
                            minute ->

                        reminderHour =
                            hour

                        reminderMinute =
                            minute

                        preferences
                            .edit()
                            .putInt(
                                "bedtime_reminder_hour",
                                reminderHour
                            )
                            .putInt(
                                "bedtime_reminder_minute",
                                reminderMinute
                            )
                            .apply()

                        updateReminderTimeText()

                        if (
                            binding
                                .switchBedtimeReminder
                                .isChecked
                        ) {

                            BedtimeReminderScheduler.schedule(
                                requireContext(),
                                reminderHour,
                                reminderMinute
                            )

                            Toast.makeText(
                                requireContext(),
                                "Reminder time updated.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    reminderHour,
                    reminderMinute,
                    false
                ).show()
            }
    }

    private fun updateReminderTimeText() {

        val calendar =
            Calendar.getInstance().apply {

                set(
                    Calendar.HOUR_OF_DAY,
                    reminderHour
                )

                set(
                    Calendar.MINUTE,
                    reminderMinute
                )
            }

        binding.tvReminderTime.text =
            SimpleDateFormat(
                "h:mm a",
                Locale.getDefault()
            ).format(
                calendar.time
            )
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}