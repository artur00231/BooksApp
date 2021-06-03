package com.booksapp.lists

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.booksapp.App
import com.booksapp.data.Book
import com.booksapp.data.UserBook
import com.booksapp.data.UserBookType
import com.booksapp.databinding.FragmentUserBookListBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserBookList : Fragment() {
    lateinit var binding: FragmentUserBookListBinding
    lateinit var adapter: UserBookListAdapter

    private var books: ArrayList<UserBook> = arrayListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = UserBookListAdapter(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUserBookListBinding.inflate(inflater)
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.bookList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        binding.bookList.adapter = adapter

        GlobalScope.launch {
            loadBooks()

            val set1 = ArrayList(books.filter { it.type == UserBookType.ToRead })
            val set2 = ArrayList(books.filter { it.type == UserBookType.CurrentlyRead })
            val set3 =  ArrayList(books.filter { it.type == UserBookType.Read })

            withContext(Dispatchers.Main) {
                adapter.setData(set1, set2, set3)
            }
        }

        val callback = UserBooksItemTouchHelperCallback(requireContext())
        callback.setOnSwipeListener { viewHolder: RecyclerView.ViewHolder, direction: Int ->
            if (direction == 4) {
                adapter.moveBook(viewHolder.adapterPosition, UserBookListAdapter.MoveDirection.Up)
            } else if (direction == 8) {
                adapter.moveBook(viewHolder.adapterPosition, UserBookListAdapter.MoveDirection.Down)
            }
        }

        callback.setIsSwappable {
            adapter.getPossibleActions(it)
        }
        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(binding.bookList)
    }

    override fun onResume() {
        super.onResume()

        GlobalScope.launch {
            loadBooks()

            val set1 = ArrayList(books.filter { it.type == UserBookType.ToRead })
            val set2 = ArrayList(books.filter { it.type == UserBookType.CurrentlyRead })
            val set3 =  ArrayList(books.filter { it.type == UserBookType.Read })

            withContext(Dispatchers.Main) {
                adapter.setData(set1, set2, set3)
            }
        }
    }

    private fun loadBooks() {
        val db = (requireContext().applicationContext as App).db!!.userBookDao()

        books = ArrayList(db.getAll())
    }

}