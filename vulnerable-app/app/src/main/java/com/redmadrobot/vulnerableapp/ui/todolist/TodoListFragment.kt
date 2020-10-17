package com.redmadrobot.vulnerableapp.ui.todolist

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.redmadrobot.vulnerableapp.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.dialog_add_todo.view.*
import kotlinx.android.synthetic.main.fragment_todo_list.*


@AndroidEntryPoint
class TodoListFragment : Fragment(), ItemClickListener {

    private val viewModel: TodoListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_todo_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val todosAdapter = TodosAdapter(this)

        viewModel.todoList.observe(viewLifecycleOwner) { list ->
            todosAdapter.submitList(list)
            loading.visibility = View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
        }

        with(todo_list) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(
                DividerItemDecoration(
                    todo_list.context,
                    LinearLayoutManager.VERTICAL
                )
            )
            adapter = todosAdapter
        }

        viewModel.loadTodos().also { loading.visibility = View.VISIBLE }
    }

    override fun onItemClick(todo: Todo, position: Int) {
        Toast.makeText(context, todo.text, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                val inflater = requireActivity().layoutInflater
                val view = inflater.inflate(R.layout.dialog_add_todo, null)

                AlertDialog.Builder(requireActivity())
                    .setView(view)
                    .setPositiveButton(android.R.string.ok) { _, _ -> viewModel.createTodo(view.username.text.toString()) }
                    .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                    .create()
                    .show()

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
