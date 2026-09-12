package com.example.moodmatesleep.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moodmatesleep.data.database.MoodMateDatabase
import com.example.moodmatesleep.databinding.FragmentSleepDetailBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepDetailFragment : Fragment() {

    private var _binding:
            FragmentSleepDetailBinding? = null

    private val binding
        get() = _binding!!

    private val database by lazy {

        MoodMateDatabase.getDatabase(
            requireContext()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentSleepDetailBinding.inflate(
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

        binding.btnBackDetail
            .setOnClickListener {

                findNavController()
                    .navigateUp()
            }

        val sessionId =
            arguments
                ?.getLong(
                    "sessionId",
                    -1L
                )
                ?: -1L

        if (sessionId != -1L) {

            loadSession(
                sessionId
            )
        }
    }

    private fun loadSession(
        sessionId: Long
    ) {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                val session =
                    withContext(
                        Dispatchers.IO
                    ) {

                        database
                            .sleepSessionDao()
                            .getSessionById(
                                sessionId
                            )
                    }

                session ?: return@launch


                binding.tvDetailDate.text =
                    SimpleDateFormat(
                        "EEEE, MMMM d",
                        Locale.getDefault()
                    ).format(
                        Date(
                            session.bedtimeMillis
                        )
                    )


                binding.tvDetailBedtime.text =
                    formatTime(
                        session.bedtimeMillis
                    )


                binding.tvDetailWake.text =
                    session.wakeTimeMillis
                        ?.let {
                            formatTime(it)
                        }
                        ?: "--"


                binding.tvDetailDuration.text =
                    formatDuration(
                        session.durationMinutes
                            ?: 0
                    )


                binding.tvDetailQuality.text =
                    "Quality: ${
                        qualityText(
                            session.sleepQuality
                        )
                    }"


                binding.tvDetailDisturbances.text =
                    if (
                        session.disturbances
                            .isNullOrBlank()
                    ) {

                        "Sleep factors: None selected"

                    } else {

                        "Sleep factors: ${session.disturbances}"
                    }


                binding.tvDetailNote.text =
                    if (
                        session.note
                            .isNullOrBlank()
                    ) {

                        "Note: No note added"

                    } else {

                        "Note: ${session.note}"
                    }


                binding.tvDetailMood.text =
                    "Mood: ${
                        session.mood
                            ?: "Not recorded"
                    }"


                binding.tvDetailEnergy.text =
                    session.energyLevel
                        ?.let {
                            "Energy: $it%"
                        }
                        ?: "Energy: Not recorded"


                binding.tvDetailMoodFactors.text =
                    if (
                        session.moodFactors
                            .isNullOrBlank()
                    ) {

                        "Mood factors: None selected"

                    } else {

                        "Mood factors: ${session.moodFactors}"
                    }
            }
    }

    private fun qualityText(
        quality: Int?
    ): String {

        return when (quality) {

            1 -> "Very Poor (1/5)"
            2 -> "Poor (2/5)"
            3 -> "Okay (3/5)"
            4 -> "Good (4/5)"
            5 -> "Great (5/5)"

            else ->
                "Not recorded"
        }
    }

    private fun formatTime(
        millis: Long
    ): String {

        return SimpleDateFormat(
            "h:mm a",
            Locale.getDefault()
        ).format(
            Date(millis)
        )
    }

    private fun formatDuration(
        minutes: Int
    ): String {

        val hours =
            minutes / 60

        val remaining =
            minutes % 60

        return if (
            hours > 0
        ) {

            "${hours}h ${remaining}m"

        } else {

            "${remaining}m"
        }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}