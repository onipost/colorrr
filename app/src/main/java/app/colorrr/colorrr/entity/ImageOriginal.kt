package app.colorrr.colorrr.entity

import androidx.room.*
import app.colorrr.colorrr.database.converters.ImageRelativeConverter

@Entity(tableName = "images")
data class ImageOriginal(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "timestamp") var timestamp: Double,
    @ColumnInfo(name = "for_child") var for_child: Int,
    @ColumnInfo(name = "coloring_count") var coloring_count: Int,
    @ColumnInfo(name = "premium_image") var premium_image: Int,
    @ColumnInfo(name = "category_id") var category_id: Int,
    @ColumnInfo(name = "link_preview") var link_preview: String,
    @ColumnInfo(name = "link_archive") var link_archive: String,
    @field:TypeConverters(ImageRelativeConverter::class) @ColumnInfo(name = "related") var related: ArrayList<Int>
) : Image