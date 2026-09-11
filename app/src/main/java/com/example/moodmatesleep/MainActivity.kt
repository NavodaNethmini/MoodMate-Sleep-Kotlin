package com.example.moodmatesleep

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.moodmatesleep.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(
                R.id.navHostFragment
            ) as NavHostFragment

        val navController = navHostFragment.navController

        binding.bottomNavigation.setupWithNavController(
            navController
        )

        navController.addOnDestinationChangedListener { _, destination, _ ->

            when (destination.id) {

                R.id.splashFragment,
                R.id.welcomeFragment -> {
                    binding.bottomNavigation.visibility = View.GONE
                }

                else -> {
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
            when (destination.id) {

            R.id.splashFragment,
            R.id.welcomeFragment,
            R.id.readyForBedFragment,
            R.id.sleepStartedFragment -> {

                binding.bottomNavigation.visibility =
                    View.GONE
            }

            else -> {

                binding.bottomNavigation.visibility =
                    View.VISIBLE
            }
        }
        }
    }
}