package com.booksapp.lists

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ShareCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.booksapp.App
import com.booksapp.LoadData
import com.booksapp.data.Book
import com.booksapp.data.DBPackage
import com.booksapp.data.UserBook
import com.booksapp.data.UserBookType
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
                        GlobalScope.launch {
                            removeUserBooks(book)
                        }
                    }
                    .show()
            }
        }

        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(binding.bookList)

        binding.addBookButton.setOnClickListener() {
            var intent = Intent(context, BookAdd::class.java)
            startActivity(intent)
        }

        binding.saveData.setOnClickListener() {
            createFile()
        }

        binding.loadData.setOnClickListener() {
            openFile()
        }

        binding.shareDataButton.setOnClickListener() {
            GlobalScope.launch {
                val dbPackage = DBPackage(requireContext())
                val uri = dbPackage.createDumpToLocal()

                withContext(Dispatchers.Main) {
                    val shareIntent = ShareCompat.IntentBuilder.from(requireActivity())
                      .setType("*/*")
                      .addStream(uri)
                      .createChooserIntent()

                    shareIntent.flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION

                    startActivity(Intent.createChooser(shareIntent, "Share books with.."))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        GlobalScope.launch {
            loadBooks()
            bookList.sortBy { it.title }

            withContext(Dispatchers.Main) {
                adapter.setData(bookList)
                applyFilter()
            }
        }
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
            db.insert(UserBook(null, book, UserBookType.ToRead))
            return true
        }

        return false;
    }

    private fun removeUserBooks(book: Book): Boolean {
        val db = (requireContext().applicationContext as App).db!!.bookDao()

        val userBook = db.getUserBook(book.id!!)

        if (userBook != null) {
            db.delete(userBook)
            return true
        }

        return false;
    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_TITLE, "data.txt")
        }


        startActivityForResult(intent, CREATE_FILE)
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }


        startActivityForResult(intent, OPEN_FILE)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == CREATE_FILE
            && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                DBPackage(requireContext()).createDump(uri)
            }
        }

        if (requestCode == OPEN_FILE
            && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                val intent = Intent(requireContext(), LoadData::class.java)
                intent.putExtra("op", LoadData.FROM_FILE)
                intent.putExtra("uri", uri)

                startActivity(intent)
            }
        }
    }

    companion object {
        const val CREATE_FILE = 47
        const val OPEN_FILE = 94
    }
}