package app.colorrr.colorrr.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images_to_categories")
data class ImagesToCategories(
    @PrimaryKey var id: Int?,
    @ColumnInfo(name = "image_id") var image_id: Int,
    @ColumnInfo(name = "category_id") var category_id: Int)