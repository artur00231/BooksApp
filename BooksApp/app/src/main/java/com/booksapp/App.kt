package com.booksapp

import android.app.Application
import androidx.room.Room
import com.booksapp.data.AppDatabase

class App : Application(){
    var db: AppDatabase? = null;

    override fun onCreate() {
        super.onCreate()

        db = Room.databaseBuilder(
            this,
            AppDatabase::class.java, "BookDB"
        ).fallbackToDestructiveMigration().build()
    }
}