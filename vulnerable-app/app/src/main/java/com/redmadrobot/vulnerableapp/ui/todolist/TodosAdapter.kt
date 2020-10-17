package com.redmadrobot.vulnerableapp.ui.todolist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.redmadrobot.vulnerableapp.R
import kotlinx.android.synthetic.main.item_todo.view.*

class TodosAdapter(private val itemCLickListener: ItemClickListener) :
    ListAdapter<Todo, TodosAdapter.TodoViewHolder>(TodosDiffCallback()) {

    class TodosDiffCallback : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Todo, newItem: Todo): Boolean {
            return oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.bindTo(getItem(position), itemCLickListener)
    }

    class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindTo(todo: Todo, itemCLickListener: ItemClickListener) {
            itemView.text.text = todo.text
            itemView.owner.text = todo.owner

            itemView.setOnClickListener {
                itemCLickListener.onItemClick(todo, adapterPosition)
            }
        }
    }
}


