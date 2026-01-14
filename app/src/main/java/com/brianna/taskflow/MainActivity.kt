package com.brianna.taskflow

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var taskInput: EditText
    private lateinit var addButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TaskAdapter

    private val tasks = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskInput = findViewById(R.id.taskInput)
        addButton = findViewById(R.id.addButton)
        recyclerView = findViewById(R.id.taskRecyclerView)

        adapter = TaskAdapter(tasks)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        addButton.setOnClickListener {
            val taskText = taskInput.text.toString().trim()
            if (taskText.isNotEmpty()) {
                tasks.add(taskText)
                adapter.notifyItemInserted(tasks.size - 1)
                taskInput.text.clear()
            }
        }
    }
}