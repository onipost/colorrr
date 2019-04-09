package app.colorrr.colorrr.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images_unfinished")
data class ImageUnfinished(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "image_id") val imageID: Int,
    @ColumnInfo(name = "user_id") val userID: Int,
    @ColumnInfo(name = "timestamp") var timestamp: Double,
    @ColumnInfo(name = "feed_id") val feedID: Int,
    @ColumnInfo(name = "link_preview") var linkPreview: String,
    @ColumnInfo(name = "link_archive") var linkArchive: String
) : Image