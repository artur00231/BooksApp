package com.booksapp

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.booksapp.auth.UserAuth
import com.booksapp.data.Book
import com.booksapp.data.Review
import com.booksapp.data.User
import com.booksapp.databinding.ActivityLoadDataBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.Charset

class LoadData : AppCompatActivity() {
    private lateinit var uri: Uri
    private lateinit var binding: ActivityLoadDataBinding

    private var buffer: ByteArray = ByteArray(512)
    private var readJSON: JSONObject = JSONObject()

    private var dataIndex: Int = 0
    private var dataSize: Int = 0

    private lateinit var newBook: Book
    private lateinit var newReviews: JSONArray
    private var existingBook: Book? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoadDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ISBN1.isEnabled = false
        binding.ISBN2.isEnabled = false
        binding.title1.isEnabled = false
        binding.title2.isEnabled = false
        binding.author1.isEnabled = false
        binding.author2.isEnabled = false
        binding.desc1.isEnabled = false
        binding.desc2.isEnabled = false
        binding.date1.isEnabled = false
        binding.date2.isEnabled = false

        binding.loadL.visibility = View.GONE
        binding.loadR.visibility = View.GONE

        binding.addNew.visibility = View.GONE
        binding.useImport.visibility = View.GONE
        binding.useUser.visibility = View.GONE
        binding.useBoth.visibility = View.GONE

        if (intent.getIntExtra("op", -1) == FROM_FILE) {
            uri = intent.getParcelableExtra<Uri>("uri")!!
            loadFromUri()
        }

        binding.addNew.setOnClickListener {
            addBook()
        }

        binding.useBoth.setOnClickListener {
            addNewBook()
        }

        binding.useUser.setOnClickListener {
            GlobalScope.launch {
                addReviews(existingBook!!)

                withContext(Dispatchers.Main) {
                    dataIndex++;
                    setupNext()
                }
            }
        }

        binding.useImport.setOnClickListener {
            GlobalScope.launch {
                val bookDD = (application as App).db!!.bookDao()
                newBook.id = existingBook!!.id!!

                bookDD.insert(newBook)

                addReviews(newBook)

                withContext(Dispatchers.Main) {
                    dataIndex++;
                    setupNext()
                }
            }
        }
    }

    private fun setupNext() {
        Log.i("app_ii", "setupNext l:$dataSize; i: $dataIndex")

        if (dataSize <= dataIndex) {
            Toast.makeText(this, "Data loaded", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.loadL.visibility = View.GONE
        binding.loadR.visibility = View.GONE

        binding.addNew.visibility = View.GONE
        binding.useImport.visibility = View.GONE
        binding.useUser.visibility = View.GONE
        binding.useBoth.visibility = View.GONE

        try {
            val bookData = readJSON.getJSONArray("data").getJSONObject(dataIndex)
            newReviews = readJSON.getJSONArray("data").getJSONObject(dataIndex).getJSONArray("reviews")

            newBook = Book(null,
                bookData.getString("ISBN"),
                bookData.getString("title"),
                bookData.getString("author"),
                bookData.getString("description"),
                bookData.getString("date")
            )
            existingBook = null

            binding.loadR.visibility = View.VISIBLE
            binding.ISBNLayout2.editText!!.setText(emptyToSpace(newBook.ISBN))
            binding.titleLayout2.editText!!.setText(emptyToSpace(newBook.title))
            binding.authorLayout2.editText!!.setText(emptyToSpace(newBook.author))
            binding.descLayout2.editText!!.setText(emptyToSpace(newBook.description))
            binding.dateLayout2.editText!!.setText(emptyToSpace(newBook.date))

            GlobalScope.launch {
                val bookDB = (application as App).db!!.bookDao()

                var existingBooks = bookDB.get(newBook.ISBN)
                if (existingBooks.isEmpty()) {
                    existingBooks = bookDB.getBooksByTitle(newBook.title)

                    when (existingBooks.size) {
                        0 -> {
                            //Unique book
                            binding.addNew.visibility = View.VISIBLE
                        }
                        1 -> {
                            binding.useImport.visibility = View.VISIBLE
                            binding.useUser.visibility = View.VISIBLE
                            binding.useBoth.visibility = View.VISIBLE

                            binding.loadL.visibility = View.VISIBLE
                            existingBook = existingBooks[0]
                            binding.ISBNLayout1.editText!!.setText(existingBooks[0].ISBN)
                            binding.titleLayout1.editText!!.setText(existingBooks[0].title)
                            binding.authorLayout1.editText!!.setText(existingBooks[0].author)
                            binding.descLayout1.editText!!.setText(existingBooks[0].description)
                            binding.dateLayout1.editText!!.setText(existingBooks[0].date)
                        }
                        else -> {
                            //Possible same book with different ISBN, but multiple books already exist, so print warning
                            binding.addNew.visibility = View.VISIBLE
                        }
                    }

                } else {
                    if (isSameBook(newBook, existingBooks[0])) {
                        //TODO add reviews
                        binding.addNew.visibility = View.VISIBLE
                        existingBook = existingBooks[0]
                    } else {
                        withContext(Dispatchers.Main) {
                            binding.useImport.visibility = View.VISIBLE
                            binding.useUser.visibility = View.VISIBLE

                            binding.loadL.visibility = View.VISIBLE
                            existingBook = existingBooks[0]
                            binding.ISBNLayout1.editText!!.setText(emptyToSpace(existingBooks[0].ISBN))
                            binding.titleLayout1.editText!!.setText(emptyToSpace(existingBooks[0].title))
                            binding.authorLayout1.editText!!.setText(emptyToSpace(existingBooks[0].author))
                            binding.descLayout1.editText!!.setText(emptyToSpace(existingBooks[0].description))
                            binding.dateLayout1.editText!!.setText(emptyToSpace(existingBooks[0].date))
                        }
                    }
                }
            }
        } catch (exception: Throwable) {
            Log.i("app_ii", "$exception")
            Toast.makeText(this, "Error reading data", Toast.LENGTH_SHORT).show()
            dataIndex++;
            setupNext()
        }
    }

    private fun loadFromUri() {
        GlobalScope.launch {
            try {
                Log.i("app_ii", "loadFromUri")
                val readData: ArrayList<Byte> = ArrayList()

                contentResolver.openFileDescriptor(uri, "r")?.use { descriptor ->
                    FileInputStream(descriptor.fileDescriptor).use {
                        var size = 0
                        do {
                            size = it.read(buffer)
                            for (i in 0 until size) {
                                readData.add(buffer[i])
                            }
                        } while (size == 512)
                    }
                }

                readJSON = JSONObject(readData.toByteArray().decodeToString())
                dataSize = readJSON.getJSONArray("data").length()

                withContext(Dispatchers.Main) {
                    setupNext()
                }

            } catch (exception: Throwable) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoadData, "Cannot read ${uri.path}!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun isSameBook(book1: Book, book2: Book): Boolean {
        return book1.title == book2.title && book1.author == book2.author && book1.description == book2.description
                && book1.date == book2.date
    }

    private fun emptyToSpace(text: String): String {
        return if (text.isEmpty()) {
            return " "
        } else {
            text
        }
    }

    private fun addBook() {
        GlobalScope.launch {
            if (existingBook == null) {
                val bookDB = (application as App).db!!.bookDao()
                newBook.id = bookDB.insert(newBook)

                addReviews(newBook)
            } else {
                addReviews(existingBook!!)
            }

            withContext(Dispatchers.Main) {
                dataIndex++;
                setupNext()
            }
        }
    }

    private fun addNewBook() {
        GlobalScope.launch {
            val bookDB = (application as App).db!!.bookDao()
            newBook.id = bookDB.insert(newBook)

            addReviews(newBook)

            withContext(Dispatchers.Main) {
                dataIndex++;
                setupNext()
            }
        }
    }

    private fun addReviews(book: Book) {
        try {
            val size = newReviews.length()

            val reviewDB = (application as App).db!!.reviewDao()

            for (i in 0 until size) {
                try {
                    val userKey = newReviews.getJSONObject(i).getString("key")
                    val signature = newReviews.getJSONObject(i).getString("sig")
                    val rating = newReviews.getJSONObject(i).getDouble("rating").toFloat()
                    val reviewText = newReviews.getJSONObject(i).getString("reviewText")
                    val time = newReviews.getJSONObject(i).getLong("time")
                    val isbn = ""; //TODO

                    if (!UserAuth.getInstance().verifyReview(rating, reviewText, time, isbn, signature, userKey)) {
                        Log.i("app_ii", "invalid sig")
                        continue
                    }

                    var user = reviewDB.findUserByKey(userKey)
                    if (user == null) {
                        user = User(null, userKey)
                        user.userId = reviewDB.insert(user)
                    }

                    val review = Review(null, rating, reviewText, time, signature, user, book)
                    val userReview = reviewDB.findReview(user.userId!!, book.id!!)

                    if (userReview == null) {
                        reviewDB.insert(review)
                    } else if (userReview.time < time) {
                        review.reviewId = userReview.reviewId
                        reviewDB.insert(review)
                    }
                } catch (exception: Throwable) {
                    //Do nothing
                }
            }
        } catch (exception: Throwable) {
            //Do nothing
        }
    }

    companion object {
        const val FROM_FILE = 0
    }
}