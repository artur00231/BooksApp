package com.booksapp.lists

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.booksapp.App
import com.booksapp.R
import com.booksapp.data.Book
import com.booksapp.data.UserBook
import com.booksapp.data.UserBookType
import com.booksapp.databinding.BookCardBinding
import com.booksapp.databinding.BookDividerCardBinding
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserBookListAdapter(val context: Context) : RecyclerView.Adapter<UserBookListAdapter.ViewHolder>() {
    enum class CardType { BOOK, DIVIDER }
    enum class DataSet { BooksToRead, BooksRead,  BooksCurrentlyRead, Dividers }
    enum class MoveDirection {Up, Down}

    @Parcelize
    data class UserBookListAdapterData(var showData0: Boolean, var showData1: Boolean, var showData2: Boolean) : Parcelable

    private var booksToRead: UserBookListDataSet = arrayListOf()
    private var booksRead: UserBookListDataSet = arrayListOf()
    private var booksCurrentlyRead: UserBookListDataSet = arrayListOf()
    private var showDataSet : Array<Boolean> = arrayOf(true, true, true)
    private var dividers : Array<ImageButton?> = arrayOf(null, null, null)
    private var dataSetMap : HashMap<Int, UserBookListDataSet> = hashMapOf(0 to booksToRead, 1 to booksCurrentlyRead, 2 to booksRead)

    open class ViewHolder(view : View) : RecyclerView.ViewHolder(view)

    open class BookListViewHolder(val binding : BookCardBinding) : ViewHolder(binding.root)

    open class BookDividerViewHolder(val binding: BookDividerCardBinding) : ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            CardType.BOOK.ordinal -> {
                val binding = BookCardBinding.inflate(layoutInflater, parent, false)
                BookListViewHolder(binding)
            }
            CardType.DIVIDER.ordinal -> {
                val binding = BookDividerCardBinding.inflate(layoutInflater, parent, false)
                BookDividerViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Inappropriate viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        val dataSet = getItemDataSetAndPosition(position).first.ordinal
        return if (dataSet == DataSet.Dividers.ordinal) {
            CardType.DIVIDER.ordinal
        } else {
            CardType.BOOK.ordinal
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dataSetAndPos = getItemDataSetAndPosition(position)

        val id = when (dataSetAndPos.first) {
            DataSet.Dividers -> -1
            DataSet.BooksToRead -> booksToRead[dataSetAndPos.second].book.book_id
            DataSet.BooksRead -> booksRead[dataSetAndPos.second].book.book_id
            DataSet.BooksCurrentlyRead -> booksCurrentlyRead[dataSetAndPos.second].book.book_id
        }

        if (id != -1L) {
            (holder as BookListViewHolder).binding.root.setOnClickListener {
                val intent = Intent(context, UserBookEdit::class.java)
                intent.putExtra("id", id)
                context.startActivity(intent)
            }
        }

        when (dataSetAndPos.first) {
            DataSet.BooksToRead -> {
                val binding = (holder as BookListViewHolder).binding
                binding.bookTitle.text = booksToRead[dataSetAndPos.second].book.title
            }
            DataSet.BooksCurrentlyRead -> {
                val binding = (holder as BookListViewHolder).binding
                binding.bookTitle.text = booksCurrentlyRead[dataSetAndPos.second].book.title
            }
            DataSet.BooksRead -> {
                val binding = (holder as BookListViewHolder).binding
                binding.bookTitle.text = booksRead[dataSetAndPos.second].book.title
            }

            DataSet.Dividers -> {
                val binding = (holder as BookDividerViewHolder).binding
                when (dataSetAndPos.second) {
                    0 -> {
                        binding.dividerName.text = "Books to read"
                        binding.dividerButton.setOnClickListener {
                            flipDataSetToShow(0, binding.dividerButton)
                        }
                        dividers[0] = binding.dividerButton
                        setImageButton(0, binding.dividerButton)
                    }
                    1 -> {
                        binding.dividerName.text = "Books currently read"
                        binding.dividerButton.setOnClickListener {
                            flipDataSetToShow(1, binding.dividerButton)
                        }
                        dividers[1] = binding.dividerButton
                        setImageButton(1, binding.dividerButton)
                    }
                    2 -> {
                        binding.dividerName.text = "Books read"
                        binding.dividerButton.setOnClickListener {
                            flipDataSetToShow(2, binding.dividerButton)
                        }
                        dividers[2] = binding.dividerButton
                        setImageButton(2, binding.dividerButton)
                    }
                }
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        if (holder is BookDividerViewHolder) {
            val dividerHolder = holder as BookDividerViewHolder

            when (dividerHolder.binding.dividerButton) {
                dividers[0] -> dividers[0] = null
                dividers[1] -> dividers[1] = null
                dividers[2] -> dividers[2] = null
            }
        }
    }

    override fun getItemCount(): Int {
        return 3 + (if (showDataSet[0]) booksToRead.size else 0) + (if (showDataSet[1]) booksCurrentlyRead.size else 0) + (if (showDataSet[2]) booksRead.size else 0)
    }

    fun setData(booksToRead: UserBookListDataSet, booksCurrentlyRead: UserBookListDataSet, booksRead: UserBookListDataSet) {
        this.booksToRead = booksToRead
        this.booksRead = booksRead
        this.booksCurrentlyRead = booksCurrentlyRead

        notifyDataSetChanged()
        dataSetMap = hashMapOf(0 to booksToRead, 1 to booksCurrentlyRead, 2 to booksRead)
    }

    fun saveToBundle(bundle: Bundle) {
        bundle.putParcelable("UserBookListAdapterData", UserBookListAdapterData(showDataSet[0], showDataSet[1], showDataSet[2]))
    }

    fun restoreFromBundle(bundle: Bundle) {
        bundle.getParcelable<UserBookListAdapterData>("UserBookListAdapterData")?.let {
            showDataSet[0] = it.showData0
            showDataSet[1] = it.showData1
            showDataSet[2] = it.showData2

            notifyDataSetChanged()
        }
    }
    /**
     * @return 0 - no action; 1 - only move up; 2 - only move down; 3 - move down or up;
     */
    fun getPossibleActions(position: Int): Int {
        val (type, _) = getItemDataSetAndPosition(position)

        return when (type) {
            DataSet.BooksToRead -> 3
            DataSet.BooksCurrentlyRead -> 3
            DataSet.BooksRead -> 1
            else -> 0
        }
    }

    fun moveBook(position: Int, direction: MoveDirection) {
        val (type, pos) = getItemDataSetAndPosition(position)

        if (type == DataSet.BooksToRead && direction == MoveDirection.Up) {
            val book = booksToRead[pos]
            booksToRead.removeAt(pos)
            notifyItemRemoved(position)

            booksCurrentlyRead.add(book)
            booksCurrentlyRead.sortBy { it.book.title }

            val newPosition = booksCurrentlyRead.indexOf(book)
            if (showDataSet[1]) {
                notifyItemInserted(newPosition + getDataSetOffset(DataSet.BooksCurrentlyRead))
            }

            moveUserBook(book, UserBookType.CurrentlyRead)
        } else if (type == DataSet.BooksToRead && direction == MoveDirection.Down) {
            val book = booksToRead[pos]
            booksToRead.removeAt(pos)
            notifyItemRemoved(position)

            removeUserBook(book)
        } else if (type == DataSet.BooksRead && direction == MoveDirection.Down) {
            val book = booksRead[pos]
            booksRead.removeAt(pos)
            notifyItemRemoved(position)

            booksCurrentlyRead.add(book)
            booksCurrentlyRead.sortBy { it.book.title }

            val newPosition = booksCurrentlyRead.indexOf(book)
            if (showDataSet[1]) {
                notifyItemInserted(newPosition + getDataSetOffset(DataSet.BooksCurrentlyRead))
            }

            moveUserBook(book, UserBookType.CurrentlyRead)
        } else if (type == DataSet.BooksCurrentlyRead && direction == MoveDirection.Down) {
            val book = booksCurrentlyRead[pos]
            booksCurrentlyRead.removeAt(pos)
            notifyItemRemoved(position)

            booksToRead.add(book)
            booksToRead.sortBy { it.book.title }

            val newPosition = booksToRead.indexOf(book)
            if (showDataSet[0]) {
                notifyItemInserted(newPosition + getDataSetOffset(DataSet.BooksToRead))
            }

            moveUserBook(book, UserBookType.ToRead)
        } else if (type == DataSet.BooksCurrentlyRead && direction == MoveDirection.Up) {
            val book = booksCurrentlyRead[pos]
            booksCurrentlyRead.removeAt(pos)
            notifyItemRemoved(position)

            booksRead.add(book)
            booksRead.sortBy { it.book.title }

            val newPosition = booksRead.indexOf(book)
            if (showDataSet[2]) {
                notifyItemInserted(newPosition + getDataSetOffset(DataSet.BooksRead))
            }

            moveUserBook(book, UserBookType.Read)
        } else {
            notifyItemChanged(position)
        }
    }

    private fun getItemDataSetAndPosition(position: Int) : Pair<DataSet, Int> {
        var innerPosition = position

        if (innerPosition == 0) {
            return Pair(DataSet.Dividers, 0)
        } else {
            innerPosition -= 1
        }


        if (showDataSet[0]) {
            if (innerPosition < booksToRead.size) {
                return Pair(DataSet.BooksToRead, innerPosition)
            } else {
                innerPosition -= booksToRead.size
            }
        }

        if (innerPosition == 0) {
            return Pair(DataSet.Dividers, 1)
        } else {
            innerPosition -= 1
        }

        if (showDataSet[1]) {
            if (innerPosition < booksCurrentlyRead.size) {
                return Pair(DataSet.BooksCurrentlyRead, innerPosition)
            } else {
                innerPosition -= booksCurrentlyRead.size
            }
        }

        if (innerPosition == 0) {
            return Pair(DataSet.Dividers, 2)
        } else {
            innerPosition -= 1
        }

        return Pair(DataSet.BooksRead, innerPosition)
    }

    /**
     * Cannot get dividers offset
     * @see getDividerPosition
     */
    private fun getDataSetOffset(dataSet : DataSet) : Int {
        return when (dataSet) {
            DataSet.BooksToRead -> {
                if (showDataSet[0]) 1 else -1
            }
            DataSet.BooksCurrentlyRead -> {
                val offset = 2 + (if (showDataSet[0]) booksToRead.size else 0)
                if (showDataSet[1]) offset else -1
            }
            DataSet.BooksRead -> {
                var offset = 3 + (if (showDataSet[0]) booksToRead.size else 0)
                offset += (if (showDataSet[1]) booksToRead.size else 0)
                if (showDataSet[1]) offset else -1
            }
            DataSet.Dividers -> {
                -1
            }
        }
    }

    private fun getDividerPosition(divider : Int) : Int {
        return when(divider) {
            0 -> 0
            1 -> {
                1 + (if (showDataSet[0]) booksToRead.size else 0)
            }
            2 -> {
                2 + (if (showDataSet[0]) booksToRead.size else 0) + (if (showDataSet[1]) booksToRead.size else 0)
            }
            else -> -1
        }
    }

    private fun flipDataSetToShow(dataSet : Int, button : ImageButton) {
        showDataSet[dataSet] = !showDataSet[dataSet]

        setImageButton(dataSet, button)
        if (showDataSet[dataSet]) {
            notifyItemRangeInserted(getDividerPosition(dataSet) + 1, dataSetMap[dataSet]!!.size)
        } else {
            notifyItemRangeRemoved(getDividerPosition(dataSet) + 1, dataSetMap[dataSet]!!.size)
        }
    }

    private fun setImageButton(dataSet : Int, button : ImageButton) {
        if (showDataSet[dataSet]) {
            button.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up)
        } else {
            button.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down)
        }
    }

    private fun removeUserBook(userBook: UserBook) {
        GlobalScope.launch {
            val db = (context.applicationContext as App).db!!.userBookDao()
            withContext(Dispatchers.IO) {
                db.delete(userBook)
            }
        }
    }

    private fun moveUserBook(userBook: UserBook, newType : UserBookType) {
        GlobalScope.launch {
            userBook.type = newType

            val db = (context.applicationContext as App).db!!.userBookDao()
            withContext(Dispatchers.IO) {
                db.update(userBook)
            }
        }
    }
}

typealias UserBookListDataSet =  MutableList<UserBook>