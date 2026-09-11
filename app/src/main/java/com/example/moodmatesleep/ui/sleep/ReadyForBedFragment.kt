package com.example.moodmatesleep.ui.sleep

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moodmatesleep.R
import com.example.moodmatesleep.data.database.MoodMateDatabase
import com.example.moodmatesleep.data.entity.SleepSession
import com.example.moodmatesleep.databinding.FragmentReadyForBedBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReadyForBedFragment : Fragment() {

    private var _binding: FragmentReadyForBedBinding? = null
    private val binding get() = _binding!!

    private val preferences by lazy {
        requireContext().getSharedPreferences(
            "moodmate_preferences",
            android.content.Context.MODE_PRIVATE
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentReadyForBedBinding.inflate(
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

        showCurrentTime()

        binding.btnStartSleep.setOnClickListener {
            startSleepSession()
        }
    }

    private fun showCurrentTime() {

        val formatter = SimpleDateFormat(
            "h:mm a",
            Locale.getDefault()
        )

        binding.tvCurrentTime.text =
            formatter.format(Date())
    }

    private fun startSleepSession() {

        binding.btnStartSleep.isEnabled = false

        val bedtimeMillis =
            System.currentTimeMillis()

        val dateFormatter =
            SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            )

        val sleepDate =
            dateFormatter.format(
                Date(bedtimeMillis)
            )

        val newSession =
            SleepSession(
                sleepDate = sleepDate,
                bedtimeMillis = bedtimeMillis
            )

        val database =
            MoodMateDatabase.getDatabase(
                requireContext()
            )

        viewLifecycleOwner.lifecycleScope.launch {

            try {

                val sessionId =
                    withContext(Dispatchers.IO) {

                        database
                            .sleepSessionDao()
                            .insertSession(newSession)
                    }

                preferences.edit()
                    .putLong(
                        "active_session_id",
                        sessionId
                    )
                    .apply()

                findNavController().navigate(
                    R.id.action_readyForBedFragment_to_sleepStartedFragment
                )

            } catch (e: Exception) {

                binding.btnStartSleep.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}