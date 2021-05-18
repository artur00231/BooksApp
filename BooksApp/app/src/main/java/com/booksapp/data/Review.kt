package com.booksapp.data

import androidx.room.*

@Entity
data class User(@PrimaryKey(autoGenerate = true) var userId: Int?,
                    @ColumnInfo(index = true) var publicKey: String) {}

@Entity
data class Review(@PrimaryKey(autoGenerate = true) var reviewId: Int?,
                    var rating: Float,
                    var reviewText: String,
                    var reviewSign: String,
                    @Embedded var user: User,
                    @Embedded var book: Book) {}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM User")
    fun getAll(): List<User>

    @Query("SELECT * FROM User WHERE publicKey = :key")
    fun findUserByKey(key: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Long

    @Delete
    fun delete(user: User)
}