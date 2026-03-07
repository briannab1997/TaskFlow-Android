package com.brianna.taskflow

import android.graphics.Canvas
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.brianna.taskflow.data.Priority
import com.brianna.taskflow.data.Task
import com.brianna.taskflow.viewmodel.TaskViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private val taskViewModel: TaskViewModel by viewModels()

    private lateinit var adapter: TaskAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var emptyState: View
    private lateinit var taskCountText: TextView
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.taskRecyclerView)
        emptyState = findViewById(R.id.emptyState)
        taskCountText = findViewById(R.id.taskCountText)
        fab = findViewById(R.id.fab)

        adapter = TaskAdapter { task -> taskViewModel.toggleTask(task) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        taskViewModel.allTasks.observe(this) { tasks ->
            adapter.submitList(tasks)
            updateTaskCount(tasks)
            val isEmpty = tasks.isEmpty()
            emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            recyclerView.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }

        fab.setOnClickListener { showAddTaskBottomSheet() }

        setupSwipeToDelete()
    }

    private fun updateTaskCount(tasks: List<Task>) {
        val remaining = tasks.count { !it.isCompleted }
        taskCountText.text = when {
            tasks.isEmpty() -> ""
            remaining == 0 -> getString(R.string.all_done)
            remaining == 1 -> "1 task remaining"
            else -> "$remaining tasks remaining"
        }
    }

    private fun showAddTaskBottomSheet() {
        val dialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_task, null)
        dialog.setContentView(view)

        val taskInput = view.findViewById<TextInputEditText>(R.id.taskTitleInput)
        val taskInputLayout = view.findViewById<TextInputLayout>(R.id.taskInputLayout)
        val priorityChipGroup = view.findViewById<ChipGroup>(R.id.priorityChipGroup)
        val addButton = view.findViewById<View>(R.id.btnAddTask)

        // Default: Medium selected
        priorityChipGroup.check(R.id.chipMedium)

        // Focus + show keyboard
        taskInput.requestFocus()

        addButton.setOnClickListener {
            val title = taskInput.text.toString().trim()
            if (title.isNotEmpty()) {
                val priority = when (priorityChipGroup.checkedChipId) {
                    R.id.chipLow -> Priority.LOW
                    R.id.chipHigh -> Priority.HIGH
                    else -> Priority.MEDIUM
                }
                taskViewModel.addTask(title, priority)
                dialog.dismiss()
            } else {
                taskInputLayout.error = "Please enter a task"
            }
        }

        dialog.show()
    }

    private fun setupSwipeToDelete() {
        val swipeBackground = ColorDrawable(ContextCompat.getColor(this, R.color.bloom_red_light))
        val deleteIcon = ContextCompat.getDrawable(this, R.drawable.ic_delete_sweep)

        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                rv: RecyclerView,
                vh: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val task = adapter.currentList[position]
                taskViewModel.deleteTask(task)

                Snackbar.make(recyclerView, getString(R.string.task_deleted), Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.undo)) {
                        taskViewModel.restoreTask(task)
                    }
                    .setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.bloom_coral))
                    .show()
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val iconMargin = (itemView.height - (deleteIcon?.intrinsicHeight ?: 0)) / 2
                val iconTop = itemView.top + iconMargin
                val iconBottom = iconTop + (deleteIcon?.intrinsicHeight ?: 0)

                if (dX < 0) {
                    // Swiping left
                    swipeBackground.setBounds(
                        itemView.right + dX.toInt(), itemView.top,
                        itemView.right, itemView.bottom
                    )
                    swipeBackground.draw(c)
                    val iconLeft = itemView.right - iconMargin - (deleteIcon?.intrinsicWidth ?: 0)
                    val iconRight = itemView.right - iconMargin
                    deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon?.draw(c)
                } else if (dX > 0) {
                    // Swiping right
                    swipeBackground.setBounds(
                        itemView.left, itemView.top,
                        itemView.left + dX.toInt(), itemView.bottom
                    )
                    swipeBackground.draw(c)
                    val iconLeft = itemView.left + iconMargin
                    val iconRight = itemView.left + iconMargin + (deleteIcon?.intrinsicWidth ?: 0)
                    deleteIcon?.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    deleteIcon?.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }

        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }
}
