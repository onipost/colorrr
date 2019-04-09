package app.colorrr.colorrr.database.dao

import androidx.room.*
import app.colorrr.colorrr.entity.ImageOriginal
import app.colorrr.colorrr.entity.ImageToCategory
import app.colorrr.colorrr.entity.ImagesToCategories
import io.reactivex.Flowable

@Dao
interface ImageDao {
    @Query("SELECT * from images WHERE id = :id")
    fun get(id: Int): Flowable<ImageOriginal>

    @Query("SELECT * from images ORDER BY id ASC")
    fun getAll(): Flowable<List<ImageOriginal>>

    @Query("SELECT * from images_to_categories")
    fun getImagesToCategories(): Flowable<List<ImagesToCategories>>

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
                "FROM images " +
                "LEFT JOIN images_to_categories ON images.id = images_to_categories.image_id " +
                "LEFT JOIN images_unfinished ON images.id = images_unfinished.image_id " +
                "WHERE images_to_categories.category_id = :category_id " +
                "LIMIT :start, :limit"
    )
    fun getByCategoryIdLimit(category_id: Int, start: Int, limit: Int): List<ImageToCategory>

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
                "FROM images " +
                "LEFT JOIN images_to_categories ON images.id = images_to_categories.image_id " +
                "LEFT JOIN images_unfinished ON images.id = images_unfinished.image_id " +
                "WHERE images_to_categories.category_id = :category_id " +
                "ORDER BY images.coloring_count DESC " +
                "LIMIT :start, :limit"
    )
    fun getByCategoryIdLimitPopular(category_id: Int, start: Int, limit: Int): List<ImageToCategory>

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
                "FROM images " +
                "LEFT JOIN images_to_categories ON images.id = images_to_categories.image_id " +
                "LEFT JOIN images_unfinished ON images.id = images_unfinished.image_id " +
                "WHERE images_to_categories.category_id = :category_id AND images.premium_image = 1 " +
                "LIMIT :start, :limit"
    )
    fun getByCategoryIdLimitPremium(category_id: Int, start: Int, limit: Int): List<ImageToCategory>

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
                "FROM images " +
                "LEFT JOIN images_to_categories ON images.id = images_to_categories.image_id " +
                "LEFT JOIN images_unfinished ON images.id = images_unfinished.image_id " +
                "WHERE images_to_categories.category_id = :category_id AND images.premium_image = 0 " +
                "LIMIT :start, :limit"
    )
    fun getByCategoryIdLimitFree(category_id: Int, start: Int, limit: Int): List<ImageToCategory>

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
                "FROM images " +
                "LEFT JOIN images_to_categories ON images.id = images_to_categories.image_id " +
                "LEFT JOIN images_unfinished ON images.id = images_unfinished.image_id " +
                "WHERE images_to_categories.category_id = :category_id AND images.for_child = 1 " +
                "LIMIT :start, :limit"
    )
    fun getByCategoryIdLimitKids(category_id: Int, start: Int, limit: Int): List<ImageToCategory>

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
                "FROM images " +
                "LEFT JOIN images_to_categories ON images.id = images_to_categories.image_id " +
                "LEFT JOIN images_unfinished ON images.id = images_unfinished.image_id " +
                "WHERE images_to_categories.category_id = :category_id AND images.id IN (:related) " +
                "LIMIT :start, :limit"
    )
    fun getByCategoryIdLimitRecommended(category_id: Int, start: Int, limit: Int, related: List<Int>): List<ImageToCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(task: ImageOriginal)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<ImageOriginal>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertImagesToCategories(items: List<ImagesToCategories>)

    @Delete
    fun delete(task: ImageOriginal)

    @Query("DELETE FROM images WHERE id IN (:ids)")
    fun deleteList(ids: List<Int>)

    @Query("DELETE FROM images")
    fun deleteAll()

    @Query("DELETE FROM images_to_categories WHERE image_id = :image_id AND category_id = :category_id")
    fun deleteImageToCategory(image_id: Int, category_id: Int)

    @Query("DELETE FROM images_to_categories")
    fun deleteImagesToCategories()

    @Update
    fun update(task: ImageOriginal)
}