package com.booksapp

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.booksapp.databinding.ActivityMainBinding
import com.booksapp.lists.BookList
import com.booksapp.lists.UserBookList


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: PagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.mainToolbar.root
        setSupportActionBar(toolbar)
        toolbar.showOverflowMenu();

        adapter = PagesAdapter(supportFragmentManager, lifecycle)
        binding.mainPages.adapter = adapter
        binding.mainPages.isUserInputEnabled = false
    }

    public override fun onCreateOptionsMenu(menu : Menu) : Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    public override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        when (item.itemId) {
            R.id.action_all_books -> {
                if (binding.mainPages.currentItem == 1) {
                    return true
                }

                binding.mainPages.currentItem = 1

                return true
            }
            R.id.action_user_books -> {
                if (binding.mainPages.currentItem == 0) {
                    return true
                }

                binding.mainPages.currentItem = 0

                return true
            }
            else -> super.onOptionsItemSelected(item);
        }

        return false
    }
}