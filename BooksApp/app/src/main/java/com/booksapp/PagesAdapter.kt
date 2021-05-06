package com.booksapp

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.booksapp.lists.BookList
import com.booksapp.lists.UserBookList

class PagesAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) : FragmentStateAdapter(fragmentManager, lifecycle) {
    private lateinit var bookList: BookList
    private lateinit var userBookList: UserBookList

    override fun createFragment(position: Int): Fragment {
        return if (position == 0) {
            userBookList = UserBookList()
            userBookList
        } else {
            bookList = BookList()
            bookList
        }
    }

    override fun getItemCount(): Int {
        return 2
    }

}