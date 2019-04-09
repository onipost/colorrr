package app.colorrr.colorrr.database.dao

import androidx.room.*
import app.colorrr.colorrr.entity.Comment
import app.colorrr.colorrr.entity.Feed
import io.reactivex.Flowable

@Dao
interface FeedDao {
    @Query("SELECT * from feed WHERE id = :id")
    fun get(id: Int): Flowable<List<Feed>>

    @Query("SELECT * FROM feed ORDER BY timestamp DESC")
    fun getAll(): Flowable<List<Feed>>

    @Query("SELECT * FROM feed WHERE user_id = :userID")
    fun getByUser(userID: Int): List<Feed>

    @Query("SELECT * FROM feed ORDER BY likes_count DESC, timestamp DESC")
    fun getPopular(): Flowable<List<Feed>>

    @Query("SELECT * FROM feed WHERE followed = 1 ORDER BY timestamp DESC")
    fun getByFollows(): Flowable<List<Feed>>

    @Query("SELECT * from comments WHERE post_id = :postID")
    fun getComments(postID: Int): List<Comment>

    @Query("SELECT * from comments WHERE post_id = :postID LIMIT :start, :limit")
    fun getComments(start: Int, limit: Int, postID: Int): List<Comment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<Feed>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCommentsAll(items: List<Comment>)

    @Update
    fun update(task: Feed)

    @Transaction
    fun updateAll(list: List<Feed>) {
        list.forEach { this.update(it) }
    }

    @Delete
    fun delete(task: Feed)

    @Query("DELETE FROM feed")
    fun deleteAll()

    @Delete
    fun deleteComment(task: Comment)

    @Query("DELETE FROM comments")
    fun deleteCommentsAll()
}