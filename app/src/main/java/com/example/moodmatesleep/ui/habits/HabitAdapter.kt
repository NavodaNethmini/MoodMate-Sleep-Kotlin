package com.example.moodmatesleep.ui.habits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.moodmatesleep.data.entity.Habit
import com.example.moodmatesleep.databinding.ItemHabitBinding

class HabitAdapter(
    private val onCheckedChanged: (
        Habit,
        Boolean
    ) -> Unit,

    private val onEdit: (
        Habit
    ) -> Unit,

    private val onDelete: (
        Habit
    ) -> Unit
) : RecyclerView.Adapter<HabitAdapter.HabitViewHolder>() {

    private var habits:
            List<Habit> = emptyList()

    private var completedIds:
            Set<Long> = emptySet()

    fun submitData(
        newHabits: List<Habit>,
        newCompletedIds: Set<Long>
    ) {

        habits = newHabits

        completedIds =
            newCompletedIds

        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HabitViewHolder {

        val binding =
            ItemHabitBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )

        return HabitViewHolder(
            binding
        )
    }

    override fun onBindViewHolder(
        holder: HabitViewHolder,
        position: Int
    ) {

        holder.bind(
            habits[position]
        )
    }

    override fun getItemCount(): Int {
        return habits.size
    }

    inner class HabitViewHolder(
        private val binding:
        ItemHabitBinding
    ) : RecyclerView.ViewHolder(
        binding.root
    ) {

        fun bind(
            habit: Habit
        ) {

            binding.tvHabitName.text =
                habit.name

            /*
             Remove old listener first so RecyclerView
             does not trigger database changes while binding.
            */
            binding.checkHabit
                .setOnCheckedChangeListener(
                    null
                )

            binding.checkHabit.isChecked =
                completedIds.contains(
                    habit.id
                )

            binding.checkHabit
                .setOnCheckedChangeListener {
                        _,
                        isChecked ->

                    onCheckedChanged(
                        habit,
                        isChecked
                    )
                }

            binding.btnEditHabit
                .setOnClickListener {

                    onEdit(
                        habit
                    )
                }

            binding.btnDeleteHabit
                .setOnClickListener {

                    onDelete(
                        habit
                    )
                }
        }
    }
}