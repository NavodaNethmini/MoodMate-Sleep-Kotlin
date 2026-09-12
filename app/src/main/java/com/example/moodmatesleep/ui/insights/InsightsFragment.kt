package com.example.moodmatesleep.ui.insights

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.moodmatesleep.R
import com.example.moodmatesleep.data.database.MoodMateDatabase
import com.example.moodmatesleep.data.entity.SleepSession
import com.example.moodmatesleep.databinding.FragmentInsightsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null

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
            FragmentInsightsBinding.inflate(
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

        loadInsights()
    }

    override fun onResume() {
        super.onResume()

        if (_binding != null) {
            loadInsights()
        }
    }

    private fun loadInsights() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                val result =
                    withContext(
                        Dispatchers.IO
                    ) {

                        val allSessions =
                            database
                                .sleepSessionDao()
                                .getAllSessions()
                                .first()

                        val profile =
                            database
                                .userProfileDao()
                                .getProfile()

                        Pair(
                            allSessions,
                            profile
                        )
                    }

                val completedSessions =
                    result.first
                        .filter {

                            it.wakeTimeMillis != null &&
                                    it.durationMinutes != null

                        }
                        .sortedByDescending {
                            it.bedtimeMillis
                        }

                val sleepGoalMinutes =
                    result.second
                        ?.sleepGoalMinutes
                        ?: 480

                updateSummary(
                    completedSessions
                )

                updateCommonMood(
                    completedSessions
                )

                updateRecentSleep(
                    completedSessions
                )

                generateSleepSyncInsight(
                    sessions =
                        completedSessions,

                    sleepGoalMinutes =
                        sleepGoalMinutes
                )
            }
    }

    private fun updateSummary(
        sessions: List<SleepSession>
    ) {

        val recentSessions =
            sessions.take(7)

        if (recentSessions.isEmpty()) {

            binding.tvAverageSleep.text =
                "--"

            binding.tvAverageQuality.text =
                "--"

            binding.tvAverageEnergy.text =
                "--"

            return
        }

        /*
         Average Sleep
        */

        val durationSessions =
            recentSessions.filter {
                it.durationMinutes != null
            }

        if (durationSessions.isNotEmpty()) {

            val averageMinutes =
                durationSessions
                    .mapNotNull {
                        it.durationMinutes
                    }
                    .average()
                    .roundToInt()

            binding.tvAverageSleep.text =
                formatDuration(
                    averageMinutes
                )

        } else {

            binding.tvAverageSleep.text =
                "--"
        }


        /*
         Average Quality
        */

        val qualityValues =
            recentSessions
                .mapNotNull {
                    it.sleepQuality
                }

        binding.tvAverageQuality.text =
            if (
                qualityValues.isNotEmpty()
            ) {

                String.format(
                    Locale.getDefault(),
                    "%.1f/5",
                    qualityValues.average()
                )

            } else {

                "--"
            }


        /*
         Average Energy
        */

        val energyValues =
            recentSessions
                .mapNotNull {
                    it.energyLevel
                }

        binding.tvAverageEnergy.text =
            if (
                energyValues.isNotEmpty()
            ) {

                "${energyValues.average().roundToInt()}%"

            } else {

                "--"
            }
    }

    private fun updateCommonMood(
        sessions: List<SleepSession>
    ) {

        val moods =
            sessions
                .take(7)
                .mapNotNull {
                    it.mood
                }

        if (moods.isEmpty()) {

            binding.tvCommonMood.text =
                "No data yet"

            return
        }

        val mostCommonMood =
            moods
                .groupingBy {
                    it
                }
                .eachCount()
                .maxByOrNull {
                    it.value
                }
                ?.key

        binding.tvCommonMood.text =
            mostCommonMood
                ?: "No data yet"
    }

    private fun generateSleepSyncInsight(
        sessions: List<SleepSession>,
        sleepGoalMinutes: Int
    ) {

        /*
         For the special feature we need
         completed sleep + morning energy data.
        */

        val usableSessions =
            sessions.filter {

                it.durationMinutes != null &&
                        it.energyLevel != null
            }

        if (usableSessions.size < 2) {

            binding.tvSleepSyncInsight.text =
                "Complete at least 2 full sleep check-ins to discover your SleepSync pattern."

            binding.tvSleepSyncTip.text =
                "MoodMate compares your sleep duration with your morning energy."

            return
        }

        val goalReached =
            usableSessions.filter {

                (it.durationMinutes ?: 0) >=
                        sleepGoalMinutes
            }

        val belowGoal =
            usableSessions.filter {

                (it.durationMinutes ?: 0) <
                        sleepGoalMinutes
            }

        /*
         Best case:
         We have both goal and below-goal sessions.
        */

        if (
            goalReached.isNotEmpty() &&
            belowGoal.isNotEmpty()
        ) {

            val goalEnergy =
                goalReached
                    .mapNotNull {
                        it.energyLevel
                    }
                    .average()

            val belowGoalEnergy =
                belowGoal
                    .mapNotNull {
                        it.energyLevel
                    }
                    .average()

            val difference =
                (
                        goalEnergy -
                                belowGoalEnergy
                        ).roundToInt()

            when {

                difference >= 5 -> {

                    binding
                        .tvSleepSyncInsight
                        .text =
                        "When you reach your sleep goal, your morning energy is about $difference% higher."

                    binding
                        .tvSleepSyncTip
                        .text =
                        "Keeping a consistent sleep duration may help you feel more energetic in the morning."
                }

                difference <= -5 -> {

                    binding
                        .tvSleepSyncInsight
                        .text =
                        "Your current records do not show higher energy on longer-sleep nights yet."

                    binding
                        .tvSleepSyncTip
                        .text =
                        "Keep tracking your sleep. Stress, routine and sleep quality can also affect morning energy."
                }

                else -> {

                    binding
                        .tvSleepSyncInsight
                        .text =
                        "Your morning energy is currently similar whether or not you reach your sleep goal."

                    binding
                        .tvSleepSyncTip
                        .text =
                        "Continue tracking to build a clearer personal pattern."
                }
            }

            return
        }


        /*
         Not enough variation yet.
        */

        val averageEnergy =
            usableSessions
                .mapNotNull {
                    it.energyLevel
                }
                .average()
                .roundToInt()

        if (
            goalReached.size ==
            usableSessions.size
        ) {

            binding
                .tvSleepSyncInsight
                .text =
                "You have reached your sleep goal in all recent tracked sessions."

            binding
                .tvSleepSyncTip
                .text =
                "Your average morning energy is $averageEnergy%. Keep tracking to discover a stronger sleep–mood pattern."

        } else {

            binding
                .tvSleepSyncInsight
                .text =
                "Your recent sleep sessions are mostly below your sleep goal."

            binding
                .tvSleepSyncTip
                .text =
                "Your average morning energy is $averageEnergy%. Try reaching your sleep goal and compare how you feel."
        }
    }

    private fun updateRecentSleep(
        sessions: List<SleepSession>
    ) {

        binding
            .recentSleepContainer
            .removeAllViews()

        val recentSessions =
            sessions.take(5)

        if (
            recentSessions.isEmpty()
        ) {

            val emptyText =
                TextView(
                    requireContext()
                )

            emptyText.text =
                "No completed sleep sessions yet."

            emptyText.textSize =
                13f

            emptyText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.moodmate_text_secondary
                )
            )

            emptyText.setPadding(
                0,
                dpToPx(12),
                0,
                dpToPx(12)
            )

            binding
                .recentSleepContainer
                .addView(
                    emptyText
                )

            return
        }

        recentSessions.forEach {
                session ->

            addSleepRow(
                session
            )
        }
    }

    private fun addSleepRow(
        session: SleepSession
    ) {

        val row =
            LinearLayout(
                requireContext()
            )

        row.orientation =
            LinearLayout.VERTICAL

        row.setPadding(
            dpToPx(16),
            dpToPx(12),
            dpToPx(16),
            dpToPx(12)
        )

        val rowParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        rowParams.bottomMargin =
            dpToPx(10)

        row.layoutParams =
            rowParams

        row.setBackgroundResource(
            R.drawable.bg_insight_row
        )


        /*
         First line
        */

        val topRow =
            LinearLayout(
                requireContext()
            )

        topRow.orientation =
            LinearLayout.HORIZONTAL

        topRow.gravity =
            Gravity.CENTER_VERTICAL


        val dateText =
            TextView(
                requireContext()
            )

        val dateParams =
            LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )

        dateText.layoutParams =
            dateParams

        dateText.text =
            formatDate(
                session.bedtimeMillis
            )

        dateText.textSize =
            13f

        dateText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.moodmate_text_primary
            )
        )


        val durationText =
            TextView(
                requireContext()
            )

        durationText.text =
            formatDuration(
                session.durationMinutes
                    ?: 0
            )

        durationText.textSize =
            14f

        durationText.setTextColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.moodmate_primary
            )
        )


        topRow.addView(
            dateText
        )

        topRow.addView(
            durationText
        )


        /*
         Sleep duration progress
        */

        val progress =
            ProgressBar(
                requireContext(),
                null,
                android.R.attr.progressBarStyleHorizontal
            )

        val progressParams =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(7)
            )

        progressParams.topMargin =
            dpToPx(10)

        progress.layoutParams =
            progressParams

        progress.max =
            600

        progress.progress =
            (session.durationMinutes ?: 0)
                .coerceAtMost(600)

        progress.progressTintList =
            ContextCompat.getColorStateList(
                requireContext(),
                R.color.moodmate_teal
            )


        row.addView(
            topRow
        )

        row.addView(
            progress
        )

        binding
            .recentSleepContainer
            .addView(
                row
            )
    }

    private fun formatDuration(
        minutes: Int
    ): String {

        val hours =
            minutes / 60

        val remainingMinutes =
            minutes % 60

        return if (
            hours > 0
        ) {

            "${hours}h ${remainingMinutes}m"

        } else {

            "${remainingMinutes}m"
        }
    }

    private fun formatDate(
        millis: Long
    ): String {

        val formatter =
            SimpleDateFormat(
                "EEE, MMM d",
                Locale.getDefault()
            )

        return formatter.format(
            Date(millis)
        )
    }

    private fun dpToPx(
        dp: Int
    ): Int {

        return (
                dp *
                        resources
                            .displayMetrics
                            .density
                ).toInt()
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}