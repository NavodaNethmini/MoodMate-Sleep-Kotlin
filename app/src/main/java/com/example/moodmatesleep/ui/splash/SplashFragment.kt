package com.example.moodmatesleep.ui.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.moodmatesleep.R
import com.example.moodmatesleep.data.database.MoodMateDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(
            R.layout.fragment_splash,
            container,
            false
        )
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {

            // Keep splash visible for a short time
            delay(1800)

            val database =
                MoodMateDatabase.getDatabase(requireContext())

            val profile = withContext(Dispatchers.IO) {
                database.userProfileDao().getProfile()
            }

            if (!isAdded) {
                return@launch
            }

            if (profile == null) {

                // First time user
                findNavController().navigate(
                    R.id.action_splashFragment_to_welcomeFragment
                )

            } else {

                // Returning user
                findNavController().navigate(
                    R.id.action_splashFragment_to_homeFragment
                )
            }
        }
    }
}