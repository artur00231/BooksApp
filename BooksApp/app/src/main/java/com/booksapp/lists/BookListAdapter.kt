package com.booksapp.lists

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.booksapp.R
import com.booksapp.data.Book
import com.booksapp.databinding.BookListViewBinding
import me.zhanghai.android.fastscroll.PopupTextProvider

class BookListAdapter : RecyclerView.Adapter<BookListAdapter.ViewHolder>(),
    PopupTextProvider {
    private var data: MutableList<Book> = arrayListOf()
    private var filteredData: MutableList<Book> = arrayListOf()
    private var extended: HashMap<Long, Boolean> = hashMapOf()
    private var filterData: BookListFilter.BookFilterData = BookListFilter.BookFilterData("", "")

    open class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)
    open class ImageListViewHolder(val binding: BookListViewBinding, val context: Context) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        val binding = BookListViewBinding.inflate(layoutInflater, parent, false)
        return ImageListViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = (holder as ImageListViewHolder).binding
        val bookId = filteredData[position].book_id!!

        binding.title.text = filteredData[position].title

        if (extended[bookId] == null || extended[bookId] == false) {
            binding.expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_s)
            binding.additionalInfo.visibility = View.GONE
        } else {
            binding.expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_s)
            binding.additionalInfo.visibility = View.VISIBLE
        }

        binding.expandButton.setOnClickListener {
            val bookId = filteredData[position].book_id!!
            if (extended[bookId] != null && extended[bookId] == true) {
                binding.expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down_s)
                binding.additionalInfo.visibility = View.GONE
                extended[bookId] = false
            } else {
                binding.expandButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_s)
                binding.additionalInfo.visibility = View.VISIBLE
                extended[bookId] = true
            }
        }

        binding.author.text = filteredData[position].author
        if (binding.author.text.isNullOrEmpty()) {
            binding.author.visibility = View.GONE
        } else {
            binding.author.visibility = View.VISIBLE
            binding.author.text = "Authors: ${binding.author.text}"
        }

        binding.description.text = filteredData[position].description
        if (binding.description.text.isNullOrEmpty()) {
            binding.description.visibility = View.GONE
        } else {
            binding.description.visibility = View.VISIBLE
            binding.description.text = "Description:\n${binding.description.text}"
        }

        binding.date.text = filteredData[position].date
        if (binding.date.text.isNullOrEmpty()) {
            binding.date.visibility = View.GONE
        } else {
            binding.date.visibility = View.VISIBLE
            binding.date.text = "Date: ${binding.date.text}"
        }

        if (binding.author.visibility == View.GONE && binding.description.visibility == View.GONE
                && binding.date.visibility == View.GONE) {
            binding.expandButton.visibility = View.GONE
        } else {
            binding.expandButton.visibility = View.VISIBLE
        }

        holder.view.setOnClickListener {
            val intent = Intent(holder.context, ReviewList::class.java)
            intent.putExtra("isbn", filteredData[position].ISBN)
            intent.putExtra("id", filteredData[position].book_id)
            holder.context.startActivity(intent)
        }

        holder.view.setOnLongClickListener {
            val intent = Intent(holder.context, BookAdd::class.java)
            intent.putExtra("id", filteredData[position].book_id!!)
            holder.context.startActivity(intent)
            true
        }
    }

    override fun getItemCount(): Int {
        return filteredData.size
    }

    //TODO reset extended for non existing data
    fun setData(images: MutableList<Book>) {
        data = images

        filter()
    }

    fun setFilters(filter: BookListFilter.BookFilterData) {
        if (filter != filterData) {
            filterData = filter

            filter()
        }
    }

    fun getBookFromPosition(position: Int): Book? {
        if (position < filteredData.size) {
            return  filteredData[position]
        }

        return null
    }

    private fun filter() {
        filteredData = ArrayList(data)

        if (filterData.title.isNotEmpty()) {
            filteredData.retainAll { it.title.contains(filterData.title, true) }
        }

        if (filterData.author.isNotEmpty()) {
            filteredData.retainAll { it.author.contains(filterData.author, true) }
        }

        notifyDataSetChanged()
    }

    override fun getPopupText(position: Int): String {
        return filteredData[position].title[0].toString().toUpperCase()
    }
}