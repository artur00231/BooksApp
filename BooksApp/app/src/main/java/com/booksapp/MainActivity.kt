package com.booksapp

import android.Manifest.permission.INTERNET
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.booksapp.auth.UserAuth
import com.booksapp.databinding.ActivityMainBinding
import com.booksapp.lists.BookList
import com.booksapp.lists.UserBookList
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.annotations.AfterPermissionGranted


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

        UserAuth.init(this)

        if (!EasyPermissions.hasPermissions(this, INTERNET)) {
            EasyPermissions.requestPermissions(this, getString(R.string.permission_internet), REQUEST_CODE_INTERNET, INTERNET)
        }

        if (!EasyPermissions.hasPermissions(this, READ_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(this, getString(R.string.permission_read), REQUEST_CODE_READ, READ_EXTERNAL_STORAGE)
        }
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }


    companion object {
        const val REQUEST_CODE_INTERNET = 132
        const val REQUEST_CODE_READ = 56
    }
}