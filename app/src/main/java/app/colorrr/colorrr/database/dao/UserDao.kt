package app.colorrr.colorrr.database.dao

import androidx.room.*
import app.colorrr.colorrr.entity.*

@Dao
interface UserDao {
    @Query("SELECT * from user LIMIT 1")
    fun getUser(): User

    @Query("SELECT * from images_published")
    fun getPublished(): List<ImagePublished>

    @Query("SELECT * from images_unfinished")
    fun getUnfinished(): List<ImageUnfinished>

    @Query(
        "SELECT " +
                "images.id, " +
                "images.name, " +
                "images.timestamp, " +
                "images.for_child, " +
                "images.coloring_count, " +
                "images.premium_image, " +
                "images.category_id, " +
                "images.link_preview, " +
                "images.link_archive, " +
                "images.related, " +
                "images_unfinished.id as image_unfinished_id," +
                "images_unfinished.user_id as image_unfinished_user_id, " +
                "images_unfinished.timestamp as image_unfinished_timestamp, " +
                "images_unfinished.feed_id as image_unfinished_feed_id, " +
                "images_unfinished.link_preview as image_unfinished_link_preview, " +
                "images_unfinished.link_archive as image_unfinished_link_archive " +
                "FROM images_unfinished " +
                "INNER JOIN images ON images_unfinished.image_id = images.id"
    )
    fun getUnfinishedFull(): List<ImageToCategory>

    @Query("SELECT * from images_premium")
    fun getPremium(): List<ImagePremium>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(task: User)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPublished(items: List<ImagePublished>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUnfinished(items: List<ImageUnfinished>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPremium(items: List<ImagePremium>)

    @Update
    fun update(task: User)

    @Update
    fun updatePublished(task: ImagePublished)

    @Update
    fun updateUnfinished(task: ImageUnfinished)

    @Query("DELETE FROM user")
    fun deleteUser()

    @Query("DELETE FROM images_published")
    fun deletePublished()

    @Query("DELETE FROM images_unfinished")
    fun deleteUnfinished()

    @Query("DELETE FROM images_premium")
    fun deletePremium()
}