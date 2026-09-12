package com.example.moodmatesleep.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moodmatesleep.R
import com.example.moodmatesleep.data.database.MoodMateDatabase
import com.example.moodmatesleep.databinding.FragmentSleepHistoryBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SleepHistoryFragment : Fragment() {

    private var _binding:
            FragmentSleepHistoryBinding? = null

    private val binding
        get() = _binding!!

    private lateinit var historyAdapter:
            SleepHistoryAdapter

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
            FragmentSleepHistoryBinding.inflate(
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

        setupRecyclerView()

        binding.btnBackHistory
            .setOnClickListener {

                findNavController()
                    .navigateUp()
            }

        loadHistory()
    }

    private fun setupRecyclerView() {

        historyAdapter =
            SleepHistoryAdapter {
                    session ->

                findNavController()
                    .navigate(
                        R.id.action_sleepHistoryFragment_to_sleepDetailFragment,
                        bundleOf(
                            "sessionId" to
                                    session.id
                        )
                    )
            }

        binding.recyclerSleepHistory.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                historyAdapter
        }
    }

    private fun loadHistory() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                val sessions =
                    withContext(
                        Dispatchers.IO
                    ) {

                        database
                            .sleepSessionDao()
                            .getAllSessions()
                            .first()
                            .filter {

                                it.wakeTimeMillis != null &&
                                        it.durationMinutes != null

                            }
                            .sortedByDescending {

                                it.bedtimeMillis
                            }
                    }

                historyAdapter
                    .submitList(
                        sessions
                    )

                binding.tvHistoryEmpty.visibility =
                    if (
                        sessions.isEmpty()
                    ) {
                        View.VISIBLE
                    } else {
                        View.GONE
                    }

                binding.recyclerSleepHistory.visibility =
                    if (
                        sessions.isEmpty()
                    ) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
            }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}