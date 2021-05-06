package com.booksapp.lists

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.booksapp.R
import com.booksapp.data.Book
import com.booksapp.data.UserBook
import com.booksapp.databinding.BookCardBinding
import com.booksapp.databinding.BookDividerCardBinding

class UserBookListAdapter : RecyclerView.Adapter<UserBookListAdapter.ViewHolder>() {
    enum class CardType { BOOK, DIVIDER }
    enum class DataSet { BooksToRead, BooksRead,  BooksCurrentlyRead, Dividers }

    private var booksToRead: UserBookListDataSet = arrayListOf()
    private var booksRead: UserBookListDataSet = arrayListOf()
    private var booksCurrentlyRead: UserBookListDataSet = arrayListOf()
    private var showDataSet : Array<Boolean> = arrayOf(true, true, true)
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
                    }
                    1 -> {
                        binding.dividerName.text = "Books currently read"
                        binding.dividerButton.setOnClickListener {
                            flipDataSetToShow(1, binding.dividerButton)
                        }
                    }
                    2 -> {
                        binding.dividerName.text = "Books read"
                        binding.dividerButton.setOnClickListener {
                            flipDataSetToShow(2, binding.dividerButton)
                        }
                    }
                }
            }
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
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

    /**
     * @return 0 - no action; 1 - only move up; 2 - only move down; 3 - move down or up;
     */
    fun getPossibleActions(position: Int): Int {
        val (type, _) = getItemDataSetAndPosition(position)

        return when (type) {
            DataSet.BooksToRead -> 2
            DataSet.BooksCurrentlyRead -> 3
            DataSet.BooksRead -> 1
            else -> 0
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

        if (showDataSet[dataSet]) {
            button.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up)
            notifyItemRangeInserted(getDividerPosition(dataSet) + 1, dataSetMap[dataSet]!!.size)
        } else {
            button.setImageResource(R.drawable.ic_baseline_keyboard_arrow_down)
            notifyItemRangeRemoved(getDividerPosition(dataSet) + 1, dataSetMap[dataSet]!!.size)
        }
    }
}

typealias UserBookListDataSet =  MutableList<UserBook>