package app.colorrr.colorrr.database.dao

import androidx.room.*
import app.colorrr.colorrr.entity.Category
import io.reactivex.Flowable

@Dao
interface CategoryDao {
    @Query("SELECT * from categories WHERE id = :id")
    fun get(id: Int): Flowable<List<Category>>

    @Query(
        "SELECT " +
                "categories.id, " +
                "categories.name, " +
                "categories.description, " +
                "categories.show_in_feed, " +
                "categories.featured, " +
                "categories.cover_image, " +
                "categories.timestamp, " +
                "categories.debug " +
                "FROM categories " +
                "INNER JOIN images_to_categories ON images_to_categories.category_id = categories.id " +
                "INNER JOIN images ON images_to_categories.image_id = images.id"
    )
    fun getAllCheck(): Flowable<List<Category>>

    @Query("SELECT * from categories")
    fun getAll(): Flowable<List<Category>>

    @Query("SELECT * from categories WHERE show_in_feed = 1 ORDER BY timestamp DESC LIMIT :start, :limit")
    fun getCategories(start: Int, limit: Int): List<Category>

    @Query("SELECT * from categories WHERE show_in_feed = 1 ORDER BY timestamp DESC LIMIT :start, :limit")
    fun getCategoriesAndImages(start: Int, limit: Int): List<Category>

    @Query("SELECT * from categories WHERE featured = 1 ORDER BY timestamp DESC")
    fun getFeaturedCategories(): List<Category>

    @Query("SELECT COUNT(*) from categories WHERE featured = 1 ORDER BY timestamp DESC")
    fun getFeaturedCategoriesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(task: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<Category>)

    @Delete
    fun delete(task: Category)

    @Query("DELETE FROM categories")
    fun deleteAll()

    @Update
    fun update(task: Category)
}