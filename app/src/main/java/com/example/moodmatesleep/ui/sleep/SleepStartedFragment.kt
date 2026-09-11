package com.example.moodmatesleep.ui.sleep

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
import com.example.moodmatesleep.databinding.FragmentSleepStartedBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepStartedFragment : Fragment() {

    private var _binding: FragmentSleepStartedBinding? = null
    private val binding get() = _binding!!

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
            FragmentSleepStartedBinding.inflate(
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

        loadSleepStartTime()

        binding.btnImAwake.setOnClickListener {

            finishSleepSession()
        }
    }

    private fun loadSleepStartTime() {

        val sessionId =
            preferences.getLong(
                "active_session_id",
                -1L
            )

        if (sessionId == -1L) {
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

            session?.let {

                val formatter =
                    SimpleDateFormat(
                        "h:mm a",
                        Locale.getDefault()
                    )

                binding.tvSleepStartTime.text =
                    formatter.format(
                        Date(it.bedtimeMillis)
                    )
            }
        }
    }

    private fun finishSleepSession() {

        binding.btnImAwake.isEnabled = false

        val sessionId =
            preferences.getLong(
                "active_session_id",
                -1L
            )

        if (sessionId == -1L) {

            binding.btnImAwake.isEnabled = true

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

            try {

                val session =
                    withContext(Dispatchers.IO) {

                        database
                            .sleepSessionDao()
                            .getSessionById(sessionId)
                    }

                if (session == null) {

                    binding.btnImAwake.isEnabled = true
                    return@launch
                }

                val wakeTimeMillis =
                    System.currentTimeMillis()

                val durationMinutes =
                    (
                            (wakeTimeMillis -
                                    session.bedtimeMillis) /
                                    60000L
                            ).toInt()

                val updatedSession =
                    session.copy(
                        wakeTimeMillis = wakeTimeMillis,
                        durationMinutes = durationMinutes
                    )

                withContext(Dispatchers.IO) {

                    database
                        .sleepSessionDao()
                        .updateSession(updatedSession)
                }

                findNavController().navigate(
                    R.id.action_sleepStartedFragment_to_sleepCheckInFragment
                )

            } catch (e: Exception) {

                binding.btnImAwake.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "Unable to finish sleep session.",
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