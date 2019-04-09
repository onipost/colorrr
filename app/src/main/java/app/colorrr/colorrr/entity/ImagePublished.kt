package app.colorrr.colorrr.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images_published")
data class ImagePublished(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "image_id") val imageId: Int,
    @ColumnInfo(name = "user_id") val userId: Int,
    @ColumnInfo(name = "timestamp") val timestamp: Double,
    @ColumnInfo(name = "has_timelapse") var hasTimelapse: Int,
    @ColumnInfo(name = "user_name") var userName: String,
    @ColumnInfo(name = "user_avatar") var userAvatar: String,
    @ColumnInfo(name = "user_email") var userEmail: String,
    @ColumnInfo(name = "likes_count") var likesCount: Int,
    @ColumnInfo(name = "comments_count") var commentsCount: Int,
    @ColumnInfo(name = "liked") var liked: Int,
    @ColumnInfo(name = "followed") var followed: Int,
    @ColumnInfo(name = "link_preview") val linkPreview: String,
    @ColumnInfo(name = "link_archive") val linkArchive: String
) : Image