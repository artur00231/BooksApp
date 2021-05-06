package com.booksapp.lists

import android.os.Bundle
import android.util.Log
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
import com.booksapp.databinding.FragmentBookListBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class BookList : Fragment() {
    private lateinit var binding: FragmentBookListBinding;

    private var filterData: BookListFilter.BookFilterData = BookListFilter.BookFilterData("", "")

    private var bookList: ArrayList<Book> = arrayListOf()
    private var adapter: BookListAdapter = BookListAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        GlobalScope.launch {
            loadBooks()
            bookList.sortBy { it.title }

            withContext(Dispatchers.Main) {
                adapter.setData(bookList)
                applyFilter()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBookListBinding.inflate(inflater, container, false);
        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filterBooks.setOnClickListener {
            BookListFilter.newInstance(filterData).setCallback { filterData = it; applyFilter() }
                .show(parentFragmentManager, BookListFilter.TAG)
        }

        binding.bookList.adapter = adapter
        binding.bookList.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        val callback = CustomItemTouchHelperCallback(context!!)
        callback.setOnSwipeListener { viewHolder: RecyclerView.ViewHolder, direction: Int ->
            if (direction == 4) {
                var book = adapter.getBookFromPosition(viewHolder.adapterPosition)!!

                adapter.notifyItemChanged(viewHolder.adapterPosition)
                GlobalScope.launch {
                    var success = true

                    withContext(Dispatchers.IO) {
                        success = addUserBooks(book)
                    }

                    if (!success) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "This book is already added", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                Snackbar.make(requireView(), "Added", Snackbar.LENGTH_SHORT)
                    .setAction("Undo") {
                        //TODO implement this!!
                        Toast.makeText(requireContext(), "This is not working", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }
        }

        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(binding.bookList)
    }

    private fun applyFilter() {
        adapter.setFilters(filterData)
    }

    private fun loadBooks() {
        val db = (requireContext().applicationContext as App).db!!.bookDao()

        bookList = ArrayList(db.getAll())
    }

    private fun addUserBooks(book: Book): Boolean {
        val db = (requireContext().applicationContext as App).db!!.bookDao()

        val userBook = db.getUserBook(book.id!!)

        if (userBook == null) {
            db.insert(UserBook(null, book, 0))
            return true
        }

        return false;
    }
}