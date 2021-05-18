package com.booksapp.lists

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.TokenWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import com.booksapp.App
import com.booksapp.data.Book
import com.booksapp.data.BookDao
import com.booksapp.data.UserBook
import com.booksapp.data.UserBookType
import com.booksapp.databinding.ActivityUserBookEditBinding
import kotlinx.coroutines.*

class UserBookEdit : AppCompatActivity() {
    lateinit var binding : ActivityUserBookEditBinding
    lateinit var db : BookDao
    private var book : Book? = null
    private var userBook : UserBook? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserBookEditBinding.inflate(layoutInflater)

        setContentView(binding.root)

        db = (applicationContext as App).db!!.bookDao()
        binding.button3.isActivated = false

        GlobalScope.launch {
            val isbn = intent.getStringExtra("isbn")
            if (isbn != null) book = db.getByISBN(isbn)

            if (book == null) {
                Toast.makeText(applicationContext, "Error", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                binding.editTitle.text = book!!.title
                userBook = db.getUserBook(book!!.id!!)
            }

            withContext(Dispatchers.Main) {
                if (userBook != null) {
                    when (userBook!!.type) {
                        UserBookType.ToRead -> binding.listToRead.isChecked = true
                        UserBookType.CurrentlyRead -> binding.listReading.isChecked = true
                        UserBookType.Read -> binding.listFinished.isChecked = true
                    }
                }
                else binding.listNone.isChecked = true
                binding.button3.isActivated = true
            }
        }

    }

    fun confirm(v : View) {
        binding.button3.isActivated = false
        if (binding.editList.checkedRadioButtonId == binding.listNone.id) {
            GlobalScope.launch {
                if (userBook != null) {
                    db.delete(userBook!!)
                }
                finish()
            }
        } else {
            GlobalScope.launch {
                val type = when (binding.editList.checkedRadioButtonId) {
                    binding.listToRead.id -> UserBookType.ToRead
                    binding.listReading.id -> UserBookType.CurrentlyRead
                    else -> UserBookType.Read
                }
                if (userBook == null) {
                    userBook = UserBook(null, book!!, type)
                } else {
                    userBook!!.type = type
                }
                db.insert(userBook!!)
                finish()
            }
        }
    }
}