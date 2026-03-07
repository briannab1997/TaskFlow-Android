package com.brianna.taskflow.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val priority: Int = Priority.MEDIUM.value,
    val createdAt: Long = System.currentTimeMillis()
)

enum class Priority(val value: Int, val label: String) {
    LOW(0, "Low"),
    MEDIUM(1, "Medium"),
    HIGH(2, "High");

    companion object {
        fun fromInt(value: Int): Priority =
            entries.firstOrNull { it.value == value } ?: MEDIUM
    }
}
