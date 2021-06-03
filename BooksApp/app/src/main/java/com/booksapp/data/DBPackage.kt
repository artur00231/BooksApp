package com.booksapp.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import com.booksapp.App
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset

class DBPackage(private val context: Context) {

    fun createDump(location: Uri) {
        GlobalScope.launch {
            val data = dataToJson()

            context.contentResolver.openFileDescriptor(location, "w")?.use { descriptor ->
                FileOutputStream(descriptor.fileDescriptor).use {
                    it.write(
                        data.toString(4).toByteArray(Charset.forName("UTF-8"))
                    )
                }
            }
        }
    }

    fun createDumpToLocal(): Uri {
        val data = dataToJson()

        val filename = "tmp.txt"
        context.openFileOutput(filename, Context.MODE_PRIVATE).use {
            it.write(data.toString(4).toByteArray(Charset.forName("UTF-8")))
        }

        val file = File("${context.filesDir}/$filename")

        return FileProvider.getUriForFile(context, context.applicationContext.packageName + ".provider", file)
    }

    private fun dataToJson(): JSONObject {
        var bookDB = (context.applicationContext as App).db!!.bookDao()
        var reviewDB = (context.applicationContext as App).db!!.reviewDao()

        val booksData = JSONArray()
        val data = JSONObject()
        val books = bookDB.getAll()

        for (book in books) {
            val bookData = JSONObject()
            bookData.put("ISBN", book.ISBN)
            bookData.put("title", book.title)
            bookData.put("description", book.description)
            bookData.put("author", book.author)
            bookData.put("date", book.date)

            val reviewsData = JSONArray()

            val reviews = reviewDB.getReviewsForBook(book)

            for (review in reviews) {
                val reviewData = JSONObject()

                reviewData.put("rating", review.rating)
                reviewData.put("reviewText", review.reviewText)
                reviewData.put("time", review.time)
                reviewData.put("sig", review.reviewSign)
                reviewData.put("key", review.user.publicKey)

                reviewsData.put(reviewData)
            }

            bookData.put("reviews", reviewsData)

            booksData.put(bookData)
        }

        data.put("data", booksData)

        return data
    }
}