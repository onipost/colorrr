package app.colorrr.colorrr.entity

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.colorrr.colorrr.database.converters.ImageRelativeConverter

data class ImageToCategory(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "timestamp") var timestamp: Double,
    @ColumnInfo(name = "for_child") var for_child: Int,
    @ColumnInfo(name = "coloring_count") var coloring_count: Int,
    @ColumnInfo(name = "premium_image") var premium_image: Int,
    @ColumnInfo(name = "category_id") var category_id: Int,
    @ColumnInfo(name = "link_preview") var link_preview: String,
    @ColumnInfo(name = "link_archive") var link_archive: String,
    @field:TypeConverters(ImageRelativeConverter::class) @ColumnInfo(name = "related") var related: ArrayList<Int>,
    @ColumnInfo(name = "image_unfinished_id") var unfinishedId: Int?,
    @ColumnInfo(name = "image_unfinished_user_id") val unfinishedUserID: Int?,
    @ColumnInfo(name = "image_unfinished_timestamp") var unfinishedTimestamp: Double?,
    @ColumnInfo(name = "image_unfinished_feed_id") val unfinishedFeedID: Int?,
    @ColumnInfo(name = "image_unfinished_link_preview") var unfinishedLinkPreview: String?,
    @ColumnInfo(name = "image_unfinished_link_archive") var unfinishedLinkArchive: String?
) : Image