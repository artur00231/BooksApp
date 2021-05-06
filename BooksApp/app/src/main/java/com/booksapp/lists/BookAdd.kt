package com.booksapp.lists

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.booksapp.App
import com.booksapp.data.Book
import com.booksapp.databinding.ActivityBookAddBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class BookAdd : AppCompatActivity() {
    lateinit var binding: ActivityBookAddBinding;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBookAddBinding.inflate(layoutInflater)

        setContentView(binding.root)
    }

    fun add(v : View) {
        binding.button.isActivated = false;
        val book = Book(null,
            binding.addIsbn.text.toString(),
            binding.addTitle.text.toString(),
            binding.addDesc.text.toString(),
            binding.addAuthor.text.toString(),
            binding.addDate.text.toString()
        )

        GlobalScope.launch {
            val db = (applicationContext as App).db!!.bookDao()
            db.insert(book);
            finish()
        }
    }
}