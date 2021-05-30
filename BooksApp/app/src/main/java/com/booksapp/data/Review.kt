package com.booksapp.data

import androidx.room.*
import com.booksapp.auth.UserAuth

@Entity
data class User(@PrimaryKey(autoGenerate = true) var userId: Long?,
                    @ColumnInfo(index = true) var publicKey: String) {}

@Entity
data class Review(@PrimaryKey(autoGenerate = true) var reviewId: Long?,
                    var rating: Float,
                    var reviewText: String,
                    var time: Long,
                    var reviewSign: String,
                    @Embedded var user: User,
                    @Embedded var book: Book) {}

@Dao
interface ReviewDao {
    @Query("SELECT * FROM User")
    fun getAll(): List<User>

    @Query("SELECT * FROM User WHERE publicKey = :key")
    fun findUserByKey(key: String): User?

    @Query("SELECT * FROM User WHERE userId = :id")
    fun findUserByID(id: Long): User?

    @Query("SELECT * FROM Review WHERE userId = :userId AND id = :bookId")
    fun findReview(userId: Long, bookId : Long): Review?

    @Query("SELECT * FROM Review WHERE id = :bookId")
    fun findReviewsForBook(bookId: Long): List<Review>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(review: Review): Long

    @Delete
    fun delete(user: User)
}