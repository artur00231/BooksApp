package com.booksapp.data

import androidx.room.*

@Entity
data class Book(@PrimaryKey(autoGenerate = true) var id: Long?,
                @ColumnInfo(index = true) var ISBN: String,
                var title: String,
                var description : String,
                var author : String,
                var date : String,
    )

enum class UserBookType { ToRead, CurrentlyRead, Read }

@Entity
data class UserBook(@PrimaryKey(autoGenerate = true) var userBook_id: Long?,
                    @Embedded var book : Book,
                    var type : UserBookType
)

@Dao
interface BookDao {
    @Query("SELECT * FROM Book")
    fun getAll(): List<Book>

    @Query("SELECT * FROM UserBook")
    fun getAllUserBooks(): List<UserBook>

    @Query("SELECT * FROM UserBook ORDER BY title DESC")
    fun getAllUserBooksSorted(): List<UserBook>

    @Query("SELECT * FROM UserBook WHERE type = :type")
    fun getUserBooks(type: Long): List<UserBook>

    @Query("SELECT * FROM Book WHERE id = :id")
    fun get(id: Int): Book?

    @Query("SELECT * FROM UserBook WHERE id = :book_id")
    fun getUserBook(book_id: Long): UserBook?

    @Query("SELECT * FROM Book WHERE ISBN = :ISBN")
    fun getByISBN(ISBN: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(book: Book): Long

    @Delete
    fun delete(book: Book)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(userBook: UserBook): Long

    @Delete
    fun delete(userBook: UserBook)
}

