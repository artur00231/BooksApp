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
    private var currentFragment = 0;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val toolbar = binding.mainToolbar.root
        setSupportActionBar(toolbar)
        toolbar.showOverflowMenu();
    }

    public override fun onCreateOptionsMenu(menu : Menu) : Boolean {
        menuInflater.inflate(R.menu.main_toolbar_menu, menu)
        return true
    }

    public override fun onOptionsItemSelected(item : MenuItem) : Boolean {
        when (item.itemId) {
            R.id.action_all_books -> {
                if (currentFragment == 0) {
                    return true
                }

                //TODO implement fragment change

                return true
            }
            R.id.action_user_books -> {
                if (currentFragment == 1) {
                    return true
                }

                //TODO implement fragment change

                return true
            }
            else -> super.onOptionsItemSelected(item);
        }

        return false
    }
}