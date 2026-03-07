package com.brianna.taskflow

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.brianna.taskflow.data.Priority
import com.brianna.taskflow.data.Task

class TaskAdapter(
    private val onToggle: (Task) -> Unit
) : ListAdapter<Task, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    inner class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkbox: CheckBox = view.findViewById(R.id.taskCheckbox)
        val taskTitle: TextView = view.findViewById(R.id.taskTitle)
        val priorityDot: View = view.findViewById(R.id.priorityDot)

        fun bind(task: Task) {
            taskTitle.text = task.title

            // Strikethrough + fade for completed tasks
            if (task.isCompleted) {
                taskTitle.paintFlags = taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                taskTitle.alpha = 0.45f
                checkbox.alpha = 0.45f
            } else {
                taskTitle.paintFlags = taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                taskTitle.alpha = 1.0f
                checkbox.alpha = 1.0f
            }

            // Priority dot color
            val dotColor = when (Priority.fromInt(task.priority)) {
                Priority.HIGH -> ContextCompat.getColor(itemView.context, R.color.bloom_red)
                Priority.MEDIUM -> ContextCompat.getColor(itemView.context, R.color.bloom_yellow)
                Priority.LOW -> ContextCompat.getColor(itemView.context, R.color.bloom_green)
            }
            val dotDrawable = ContextCompat.getDrawable(itemView.context, R.drawable.shape_circle)
                ?.mutate()
            dotDrawable?.setTint(dotColor)
            priorityDot.background = dotDrawable

            // Checkbox without re-triggering listener on bind
            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = task.isCompleted
            checkbox.setOnCheckedChangeListener { _, _ -> onToggle(task) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
    override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem
}
