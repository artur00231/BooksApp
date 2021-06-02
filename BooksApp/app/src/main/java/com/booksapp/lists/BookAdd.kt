package com.booksapp.lists

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.booksapp.App
import com.booksapp.R
import com.booksapp.data.Book
import com.booksapp.databinding.ActivityBookAddBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

class BookAdd : AppCompatActivity() {
    private lateinit var binding: ActivityBookAddBinding
    private var requestActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBookAddBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.lookup.setOnClickListener {
            //Get book information
            if (!requestActive) {
                requestActive = true

                val ISBN = binding.addIsbnLayout.editText!!.text.toString()
                if (ISBN.isEmpty()){
                    requestActive = false
                    return@setOnClickListener
                }

                var url = resources.getString(R.string.openLibraryUrl)
                url = url.replace("ISBNNUMBER", ISBN.toString())
                val queue = Volley.newRequestQueue(this)

                val bookRequest = StringRequest(
                    Request.Method.GET, url,
                    { response ->
                        responseHandler(response, ISBN)
                        requestActive = false
                    },
                    {
                        Toast.makeText(this, "Cannot connect to library!", Toast.LENGTH_SHORT).show()
                        requestActive = false
                    })

                queue.add(bookRequest)
            }

        }
    }

    fun add(v : View) {
        binding.button.isActivated = false;
        val book = Book(null,
            binding.addIsbnLayout.editText!!.text.toString(),
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

    private fun responseHandler(response: String, ISBN: String) {
        var responseJSon: JSONObject = JSONObject()

        try {
            responseJSon = JSONObject(response)
        } catch (exception: JSONException) {
            Toast.makeText(this, "Invalid ISBN", Toast.LENGTH_SHORT).show()
        }

        try {
            val title = (responseJSon["ISBN:$ISBN"] as JSONObject).getString("title")
            binding.addTitle.setText(title)
        } catch (exception: JSONException) {
            Toast.makeText(this, "Invalid ISBN", Toast.LENGTH_SHORT).show()
        }

        try {
            val publishDate = (responseJSon["ISBN:$ISBN"]as JSONObject).getString("publish_date")
            binding.addDate.setText(publishDate)
        } catch (exception: JSONException) {
            //Non important information
            //continue
        }

        try {
            val authorsArray = (responseJSon["ISBN:$ISBN"] as JSONObject).getJSONArray("authors")
            var authors = ""

            if (authorsArray.length() > 0) {
                authors = authorsArray.getJSONObject(0).getString("name")
            }

            for (i in 1 until authorsArray.length()) {
                authors = authors + ", " + authorsArray.getJSONObject(i).getString("name")
            }

            binding.addAuthor.setText(authors)
        } catch (exception: JSONException) {
            //Non important information
            //continue
        }

        try {
            var desc = (responseJSon["ISBN:$ISBN"] as JSONObject).getString("description")
            val descParts = desc.split("\"")

            if (descParts.size > 1) {
                desc = descParts[1]
            }

            binding.addDesc.setText(desc)
        } catch (exception: JSONException) {
            //Non important information
            //continue
        }
    }

}