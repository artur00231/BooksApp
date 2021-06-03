package com.booksapp.data

import androidx.room.*

@Entity
data class User(@PrimaryKey(autoGenerate = true) var user_id: Long?,
                    @ColumnInfo(index = true) var publicKey: String) {
    constructor() : this(null, "")
}

@Entity(foreignKeys = [ForeignKey(
    entity = User::class,
    parentColumns = arrayOf("user_id"),
    childColumns = arrayOf("fk_user_id"),
    onDelete = ForeignKey.CASCADE),
    ForeignKey(
        entity = Book::class,
        parentColumns = arrayOf("book_id"),
        childColumns = arrayOf("fk_book_id"),
        onDelete = ForeignKey.CASCADE
    )])
data class Review(@PrimaryKey(autoGenerate = true) var review_id: Long?,
                  var rating: Float,
                  var reviewText:String,
                  var time: Long,
                  var reviewSign: String,
                  @Ignore var user: User,
                  @Ignore var book: Book,
                  @ColumnInfo(index = true) var fk_user_id: Long?,
                  @ColumnInfo(index = true) var fk_book_id: Long?
                  ) {
    constructor() : this(null, 0.0f, "", 0, "", User(), Book(), null, null)
    constructor(review_id: Long?, rating: Float, reviewText:String, time: Long, reviewSign: String, user: User, book: Book,) :
            this(review_id, rating, reviewText, time, reviewSign, user, book, user.user_id!!, book.book_id!!)
}

@Entity
data class ReviewWithBookAndUser(
    @Embedded var user: User,
    @Embedded var book: Book,
    var review_id: Long?,
    var rating: Float,
    var reviewText:String,
    var time: Long,
    var reviewSign: String,
    var fk_user_id: Long?,
    var fk_book_id: Long?
    ) {

    fun toReview(): Review {
        return Review(review_id, rating, reviewText, time, reviewSign, user, book, fk_user_id, fk_book_id)
    }

    companion object {
        fun toReviews(list: List<ReviewWithBookAndUser>): List<Review> {
            return list.map {
                return@map it.toReview()
            }
        }
    }
}

@Dao
interface UserDao {
    @Query("SELECT * FROM User")
    fun getAll(): List<User>

    @Query("SELECT * FROM User WHERE publicKey = :key")
    fun findUserByKey(key: String): User?

    @Query("SELECT * FROM User WHERE user_id = :id")
    fun findUserByID(id: Long): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(user: User): Long

    @Update
    fun update(user: User)

    @Delete
    fun delete(user: User)
}

@Dao
abstract class ReviewDao {
    @Query("SELECT * FROM Review JOIN User ON Review.fk_user_id = User.user_id JOIN Book on Review.fk_book_id = Book.book_id WHERE User.user_id = :userId AND Book.book_id = :bookId")
    abstract fun _getReview(userId: Long, bookId : Long): ReviewWithBookAndUser?

    fun getReview(user: User, book: Book): Review? {
        _getReview(user.user_id!!, book.book_id!!)?.let {
            return it.toReview()
        }
        return null
    }

    @Query("SELECT * FROM Review JOIN User ON Review.fk_user_id = User.user_id JOIN Book on Review.fk_book_id = Book.book_id WHERE Book.book_id = :bookId")
    abstract fun _getReviewsForBook(bookId: Long): List<ReviewWithBookAndUser>

    fun getReviewsForBook(book: Book): List<Review>{
        return ReviewWithBookAndUser.toReviews(_getReviewsForBook(book.book_id!!))
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(review: Review): Long

    @Update
    abstract fun update(review: Review)

    @Delete
    abstract fun delete(review: Review)
}