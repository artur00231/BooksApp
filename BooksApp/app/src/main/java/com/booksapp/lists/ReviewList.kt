package com.booksapp.lists

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.booksapp.App
import com.booksapp.R
import com.booksapp.data.Review
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject


class ReviewListAdapter(private var data: List<Review>) : RecyclerView.Adapter<ReviewListAdapter.ViewHolder>() {
    class ViewHolder(var view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.review_item, parent, false)

        return  ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.findViewById<RatingBar>(R.id.review_item_rating).apply {
            isActivated = false
            rating = data[position].rating
        }

        holder.view.findViewById<TextView>(R.id.review_item_text).text = data[position].reviewText
    }

    override fun getItemCount(): Int {
        return data.size
    }
}

class ReviewList : AppCompatActivity() {
    private lateinit var isbn: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review_list)

        var maybeIsbn = intent.getStringExtra("isbn")

        if (maybeIsbn == null) {
            finish()
        } else {
            isbn = maybeIsbn
            val rv = findViewById<RecyclerView>(R.id.review_list_recycler_view)
            rv.layoutManager = LinearLayoutManager(rv.context, LinearLayoutManager.VERTICAL, false)
            rv.addItemDecoration(DividerItemDecoration(rv.context, DividerItemDecoration.VERTICAL))

            GlobalScope.launch {
                val book = (applicationContext as App).db!!.bookDao().getByISBN(isbn)
                val reviews = (applicationContext as App).db!!.reviewDao().findReviewsForBook(book!!.id!!)

                Log.d("REVIEW TEST", ""+ reviews.size)

                withContext(Dispatchers.Main) {
                    rv.adapter = ReviewListAdapter(reviews)
                    rv.adapter!!.notifyDataSetChanged()
                }
            }

            var url = resources.getString(R.string.openLibraryUrl)

            url = url.replace("ISBNNUMBER", isbn.toString())
            val queue = Volley.newRequestQueue(this)

            val bookRequest = StringRequest(
                Request.Method.GET, url,
                { response ->
                    setUpLink(response)
                }, {})

            queue.add(bookRequest)
        }
    }

    private fun setUpLink(response: String) {
        var bookUrl = ""

        try {
            bookUrl = (JSONObject(response)["ISBN:$isbn"] as JSONObject).getString("url")
        } catch (exception: Throwable) {
            return
        }

        val button = findViewById<FloatingActionButton>(R.id.fab)
        button.visibility = View.VISIBLE

        button.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(bookUrl))
            startActivity(browserIntent)
        }
    }
}