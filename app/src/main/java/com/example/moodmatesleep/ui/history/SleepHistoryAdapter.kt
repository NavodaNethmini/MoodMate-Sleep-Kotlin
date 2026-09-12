package com.example.moodmatesleep.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodmatesleep.data.entity.SleepSession
import com.example.moodmatesleep.databinding.ItemSleepHistoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SleepHistoryAdapter(
    private val onItemClick: (SleepSession) -> Unit
) : RecyclerView.Adapter<SleepHistoryAdapter.SleepHistoryViewHolder>() {

    private var sessions:
            List<SleepSession> = emptyList()

    fun submitList(
        newSessions: List<SleepSession>
    ) {
        sessions = newSessions
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SleepHistoryViewHolder {

        val binding =
            ItemSleepHistoryBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return SleepHistoryViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: SleepHistoryViewHolder,
        position: Int
    ) {
        holder.bind(
            sessions[position]
        )
    }

    override fun getItemCount(): Int {
        return sessions.size
    }

    inner class SleepHistoryViewHolder(
        private val binding:
        ItemSleepHistoryBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            session: SleepSession
        ) {

            binding.tvHistoryDate.text =
                formatDate(
                    session.bedtimeMillis
                )

            binding.tvHistoryDuration.text =
                formatDuration(
                    session.durationMinutes ?: 0
                )

            binding.tvHistoryQuality.text =
                session.sleepQuality
                    ?.let {
                        "$it / 5"
                    }
                    ?: "--"

            binding.tvHistoryEnergy.text =
                session.energyLevel
                    ?.let {
                        "$it%"
                    }
                    ?: "--"

            binding.tvHistoryMood.text =
                session.mood
                    ?: "Not checked"

            binding.root.setOnClickListener {

                onItemClick(
                    session
                )
            }
        }
    }

    private fun formatDuration(
        minutes: Int
    ): String {

        val hours =
            minutes / 60

        val remaining =
            minutes % 60

        return if (hours > 0) {
            "${hours}h ${remaining}m"
        } else {
            "${remaining}m"
        }
    }

    private fun formatDate(
        millis: Long
    ): String {

        return SimpleDateFormat(
            "EEE, MMM d",
            Locale.getDefault()
        ).format(
            Date(millis)
        )
    }
}