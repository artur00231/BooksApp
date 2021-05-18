package com.booksapp.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Book::class, UserBook::class, User::class, Review::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun reviewDao(): ReviewDao
}