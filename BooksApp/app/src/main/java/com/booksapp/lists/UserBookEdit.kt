package com.booksapp.lists

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.TokenWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.booksapp.App
import com.booksapp.auth.UserAuth
import com.booksapp.data.*
import com.booksapp.databinding.ActivityUserBookEditBinding
import kotlinx.coroutines.*

class UserBookEdit : AppCompatActivity() {
    lateinit var binding : ActivityUserBookEditBinding
    lateinit var db : BookDao
    lateinit var reviewDb : ReviewDao
    private var book : Book? = null
    private var userBook : UserBook? = null

    private var review : Review? = null
    private var reviewChanged = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("changed", reviewChanged)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        reviewChanged = savedInstanceState?.getBoolean("changed") ?: false

        binding = ActivityUserBookEditBinding.inflate(layoutInflater)

        setContentView(binding.root)

        db = (applicationContext as App).db!!.bookDao()
        reviewDb = (applicationContext as App).db!!.reviewDao()
        binding.editRating.isEnabled = false
        binding.editReview.isEnabled = false
        binding.button3.isEnabled = false

        val set = savedInstanceState == null;

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

            review = reviewDb.findReview(UserAuth.userId!!, book!!.id!!)

            withContext(Dispatchers.Main) {
                if (set) {
                    if (userBook != null) {
                        when (userBook!!.type) {
                            UserBookType.ToRead -> binding.listToRead.isChecked = true
                            UserBookType.CurrentlyRead -> binding.listReading.isChecked = true
                            UserBookType.Read -> binding.listFinished.isChecked = true
                        }
                    } else binding.listNone.isChecked = true

                    if (review != null) {
                        binding.editRating.rating = review!!.rating
                        binding.editReview.setText(review!!.reviewText)
                    }
                }

                binding.editReview.addTextChangedListener { reviewChanged = true }
                binding.editRating.setOnRatingBarChangeListener { _, _, _ -> reviewChanged = true }

                binding.editReview.isEnabled = true
                binding.editRating.isEnabled = true
                binding.button3.isEnabled = true
            }
        }

    }

    fun confirm(v : View) {
        binding.button3.isEnabled = false
        if (binding.editList.checkedRadioButtonId == binding.listNone.id) {
            GlobalScope.launch {
                if (userBook != null) {
                    db.delete(userBook!!)
                }
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
            }
        }

        if (reviewChanged) {
            GlobalScope.launch {
                val time = System.currentTimeMillis()

                var newReview = Review(
                    if (review != null) {review!!.reviewId} else {null},
                    binding.editRating.rating,
                    binding.editReview.text.toString(),
                    time,
                    UserAuth.getInstance().signReview(binding.editRating.rating, binding.editReview.text.toString(), time, book!!.ISBN),
                    reviewDb.findUserByID(UserAuth.userId!!)!!,
                    book!!
                )

                reviewDb.insert(newReview)
            }
        }

        finish()
    }
}