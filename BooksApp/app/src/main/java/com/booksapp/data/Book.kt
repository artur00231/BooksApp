package com.booksapp.data

import androidx.room.*

@Entity
data class Book(@PrimaryKey(autoGenerate = true) var book_id: Long?,
                @ColumnInfo(index = true) var ISBN: String,
                @ColumnInfo(index = true) var title: String,
                var description : String,
                var author : String,
                var date : String,
    ) {
    constructor() : this(null, "", "", "", "", "") {

    }
}

enum class UserBookType { ToRead, CurrentlyRead, Read }

@Entity(foreignKeys = [ForeignKey(
    entity = Book::class,
    parentColumns = arrayOf("book_id"),
    childColumns = arrayOf("fk_book_id"),
    onDelete = ForeignKey.CASCADE
)])
data class UserBook(@PrimaryKey(autoGenerate = true) var user_book_id: Long?,
                    @Ignore var book : Book,
                    @ColumnInfo(index = true) var fk_book_id: Long?,
                    var type : UserBookType
) {
    constructor() : this(null, Book(), null, UserBookType.ToRead)
}

@Entity
data class UserBookWithBook(
    @Embedded var book: Book,
    var user_book_id: Long?,
    var fk_book_id: Long?,
    var type : UserBookType) {

    fun toUserBook(): UserBook {
        return UserBook(user_book_id, book, book.book_id, type)
    }

    companion object {
        fun toUserBooks(list: List<UserBookWithBook>): List<UserBook> {
            return list.map {
                return@map it.toUserBook()
            }
        }
    }
}

@Dao
interface BookDao {
    @Query("SELECT * FROM Book")
    fun getAll(): List<Book>

    @Query("SELECT * FROM Book WHERE book_id = :id")
    fun get(id: Int): Book?

    @Query("SELECT * FROM Book WHERE ISBN = :ISBN")
    fun get(ISBN: String): List<Book>

    @Query("SELECT * FROM Book WHERE title = :title")
    fun getBooksByTitle(title: String): List<Book>

    @Query("SELECT * FROM Book WHERE ISBN = :ISBN")
    fun getByISBN(ISBN: String): Book?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(book: Book): Long

    @Update
    fun update(book: Book)

    @Delete
    fun delete(book: Book)
}

@Dao
abstract class UserBookDao {
    @Query("SELECT * FROM Book JOIN UserBook ON Book.book_id = UserBook.fk_book_id")
    abstract fun _getAll(): List<UserBookWithBook>

    fun getAll(): List<UserBook>{
        return UserBookWithBook.toUserBooks(_getAll())
    }

    @Query("SELECT * FROM Book JOIN UserBook ON Book.book_id = UserBook.fk_book_id ORDER BY Book.title DESC")
    abstract fun _getAllSorted(): List<UserBookWithBook>

    fun getAllSorted(): List<UserBook>{
        return UserBookWithBook.toUserBooks(_getAllSorted())
    }

    @Query("SELECT * FROM Book JOIN UserBook ON Book.book_id = UserBook.fk_book_id WHERE UserBook.type = :type")
    abstract fun _getUserBooks(type: Long): List<UserBookWithBook>

    fun getUserBooks(type: Long): List<UserBook>{
        return UserBookWithBook.toUserBooks(_getUserBooks(type))
    }

    @Query("SELECT * FROM Book JOIN UserBook ON Book.book_id = UserBook.fk_book_id WHERE Book.book_id = :book_id")
    abstract fun _getByBook(book_id: Long): UserBookWithBook?

    fun getByBook(book: Book): UserBook? {
        _getByBook(book.book_id!!)?.let {
            return it.toUserBook()
        }
        return null
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(userBook: UserBook): Long

    @Update
    abstract fun update(userBook: UserBook)

    @Delete
    abstract fun delete(userBook: UserBook)
}