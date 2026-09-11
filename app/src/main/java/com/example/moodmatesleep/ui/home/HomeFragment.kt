package com.example.moodmatesleep.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.moodmatesleep.data.database.MoodMateDatabase
import com.example.moodmatesleep.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.navigation.fragment.findNavController
import com.example.moodmatesleep.R

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentHomeBinding.inflate(
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

        loadUserProfile()
        binding.btnReadyForBed.setOnClickListener {

            findNavController().navigate(
                R.id.action_homeFragment_to_readyForBedFragment
            )
        }
    }

    private fun loadUserProfile() {

        val database =
            MoodMateDatabase.getDatabase(
                requireContext()
            )

        viewLifecycleOwner.lifecycleScope.launch {

            val profile =
                withContext(Dispatchers.IO) {

                    database.userProfileDao()
                        .getProfile()
                }

            profile?.let {

                binding.tvHomeTitle.text =
                    "Good morning, ${it.name}"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}