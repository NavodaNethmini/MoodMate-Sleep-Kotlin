package com.example.moodmatesleep.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.moodmatesleep.R
import com.example.moodmatesleep.data.database.MoodMateDatabase
import com.example.moodmatesleep.data.entity.Habit
import com.example.moodmatesleep.data.entity.HabitCompletion
import com.example.moodmatesleep.databinding.DialogAddEditHabitBinding
import com.example.moodmatesleep.databinding.FragmentHabitsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HabitsFragment : Fragment() {

    private var _binding:
            FragmentHabitsBinding? = null

    private val binding
        get() = _binding!!

    private lateinit var bedtimeAdapter:
            HabitAdapter

    private lateinit var morningAdapter:
            HabitAdapter

    private val database by lazy {

        MoodMateDatabase.getDatabase(
            requireContext()
        )
    }

    private val today: String
        get() {

            return SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(
                Date()
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding =
            FragmentHabitsBinding.inflate(
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

        setupRecyclerViews()

        binding.btnAddHabit
            .setOnClickListener {

                showHabitDialog(
                    habit = null
                )
            }

        loadHabits()
    }

    override fun onResume() {
        super.onResume()

        if (_binding != null) {
            loadHabits()
        }
    }

    private fun setupRecyclerViews() {

        bedtimeAdapter =
            HabitAdapter(
                onCheckedChanged = {
                        habit,
                        checked ->

                    changeHabitCompletion(
                        habit,
                        checked
                    )
                },

                onEdit = {
                        habit ->

                    showHabitDialog(
                        habit
                    )
                },

                onDelete = {
                        habit ->

                    confirmDeleteHabit(
                        habit
                    )
                }
            )

        morningAdapter =
            HabitAdapter(
                onCheckedChanged = {
                        habit,
                        checked ->

                    changeHabitCompletion(
                        habit,
                        checked
                    )
                },

                onEdit = {
                        habit ->

                    showHabitDialog(
                        habit
                    )
                },

                onDelete = {
                        habit ->

                    confirmDeleteHabit(
                        habit
                    )
                }
            )

        binding.recyclerBedtimeHabits.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                bedtimeAdapter

            isNestedScrollingEnabled =
                false
        }

        binding.recyclerMorningHabits.apply {

            layoutManager =
                LinearLayoutManager(
                    requireContext()
                )

            adapter =
                morningAdapter

            isNestedScrollingEnabled =
                false
        }
    }

    private fun loadHabits() {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                val result =
                    withContext(
                        Dispatchers.IO
                    ) {

                        val bedtime =
                            database
                                .habitDao()
                                .getHabitsByCategory(
                                    "BEDTIME"
                                )
                                .first()

                        val morning =
                            database
                                .habitDao()
                                .getHabitsByCategory(
                                    "MORNING"
                                )
                                .first()

                        val completedIds =
                            database
                                .habitCompletionDao()
                                .getCompletedHabitIdsForDate(
                                    today
                                )
                                .toSet()

                        Triple(
                            bedtime,
                            morning,
                            completedIds
                        )
                    }

                val bedtimeHabits =
                    result.first

                val morningHabits =
                    result.second

                val completedIds =
                    result.third

                bedtimeAdapter.submitData(
                    bedtimeHabits,
                    completedIds
                )

                morningAdapter.submitData(
                    morningHabits,
                    completedIds
                )

                updateProgress(
                    bedtimeHabits +
                            morningHabits,
                    completedIds
                )
            }
    }

    private fun updateProgress(
        habits: List<Habit>,
        completedIds: Set<Long>
    ) {

        val total =
            habits.size

        val completed =
            habits.count {
                completedIds.contains(
                    it.id
                )
            }

        binding.tvHabitProgress.text =
            "$completed of $total habits completed"

        val percentage =
            if (total == 0) {
                0
            } else {
                (
                        completed.toFloat() /
                                total.toFloat() *
                                100
                        ).toInt()
            }

        binding.progressHabits
            .setProgressCompat(
                percentage,
                true
            )
    }

    private fun changeHabitCompletion(
        habit: Habit,
        isChecked: Boolean
    ) {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                withContext(
                    Dispatchers.IO
                ) {

                    if (isChecked) {

                        val existing =
                            database
                                .habitCompletionDao()
                                .getCompletion(
                                    habit.id,
                                    today
                                )

                        if (existing == null) {

                            database
                                .habitCompletionDao()
                                .insertCompletion(
                                    HabitCompletion(
                                        habitId =
                                            habit.id,

                                        completionDate =
                                            today,

                                        completed =
                                            true
                                    )
                                )
                        }

                    } else {

                        database
                            .habitCompletionDao()
                            .removeCompletion(
                                habit.id,
                                today
                            )
                    }
                }

                loadHabits()
            }
    }

    private fun showHabitDialog(
        habit: Habit?
    ) {

        val dialogBinding =
            DialogAddEditHabitBinding.inflate(
                layoutInflater
            )

        val editing =
            habit != null

        if (editing) {

            dialogBinding
                .etHabitName
                .setText(
                    habit?.name
                )

            if (
                habit?.category ==
                "MORNING"
            ) {

                dialogBinding
                    .radioMorning
                    .isChecked = true

            } else {

                dialogBinding
                    .radioBedtime
                    .isChecked = true
            }
        }

        val dialog =
            MaterialAlertDialogBuilder(
                requireContext()
            )
                .setTitle(
                    if (editing) {
                        "Edit Habit"
                    } else {
                        "Add New Habit"
                    }
                )
                .setView(
                    dialogBinding.root
                )
                .setNegativeButton(
                    "Cancel",
                    null
                )
                .setPositiveButton(
                    if (editing) {
                        "Save"
                    } else {
                        "Add"
                    },
                    null
                )
                .create()

        dialog.setOnShowListener {

            dialog
                .getButton(
                    android.app.AlertDialog
                        .BUTTON_POSITIVE
                )
                .setOnClickListener {

                    val habitName =
                        dialogBinding
                            .etHabitName
                            .text
                            ?.toString()
                            ?.trim()
                            .orEmpty()

                    if (
                        habitName.isEmpty()
                    ) {

                        dialogBinding
                            .habitNameInputLayout
                            .error =
                            "Please enter a habit name"

                        return@setOnClickListener
                    }

                    dialogBinding
                        .habitNameInputLayout
                        .error = null

                    val category =
                        if (
                            dialogBinding
                                .radioMorning
                                .isChecked
                        ) {
                            "MORNING"
                        } else {
                            "BEDTIME"
                        }

                    saveHabit(
                        oldHabit =
                            habit,

                        name =
                            habitName,

                        category =
                            category,

                        onDone = {
                            dialog.dismiss()
                        }
                    )
                }
        }

        dialog.show()
    }

    private fun saveHabit(
        oldHabit: Habit?,
        name: String,
        category: String,
        onDone: () -> Unit
    ) {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                withContext(
                    Dispatchers.IO
                ) {

                    if (
                        oldHabit == null
                    ) {

                        database
                            .habitDao()
                            .insertHabit(
                                Habit(
                                    name =
                                        name,

                                    category =
                                        category
                                )
                            )

                    } else {

                        database
                            .habitDao()
                            .updateHabit(
                                oldHabit.copy(
                                    name =
                                        name,

                                    category =
                                        category
                                )
                            )
                    }
                }

                onDone()

                loadHabits()

                Toast.makeText(
                    requireContext(),
                    if (
                        oldHabit == null
                    ) {
                        "Habit added"
                    } else {
                        "Habit updated"
                    },
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun confirmDeleteHabit(
        habit: Habit
    ) {

        MaterialAlertDialogBuilder(
            requireContext()
        )
            .setTitle(
                "Delete Habit"
            )
            .setMessage(
                "Remove \"${habit.name}\" from your habits?"
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Delete"
            ) {
                    _,
                    _ ->

                deleteHabit(
                    habit
                )
            }
            .show()
    }

    private fun deleteHabit(
        habit: Habit
    ) {

        viewLifecycleOwner
            .lifecycleScope
            .launch {

                withContext(
                    Dispatchers.IO
                ) {

                    database
                        .habitCompletionDao()
                        .deleteCompletionsForHabit(
                            habit.id
                        )

                    database
                        .habitDao()
                        .deleteHabit(
                            habit
                        )
                }

                loadHabits()

                Toast.makeText(
                    requireContext(),
                    "Habit deleted",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {

        super.onDestroyView()

        _binding = null
    }
}