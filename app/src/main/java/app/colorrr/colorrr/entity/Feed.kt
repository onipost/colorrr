package app.colorrr.colorrr.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feed")
data class Feed(
    @PrimaryKey var id: Int?,
    @ColumnInfo(name = "image_id") var imageID: Int,
    @ColumnInfo(name = "user_id") var userID: Int,
    @ColumnInfo(name = "timestamp") var timestamp: Double,
    @ColumnInfo(name = "has_timelapse") var hasTimelapse: Int,
    @ColumnInfo(name = "user_name") var userName: String,
    @ColumnInfo(name = "user_avatar") var userAvatar: String,
    @ColumnInfo(name = "user_email") var userEmail: String,
    @ColumnInfo(name = "likes_count") var likesCount: Int,
    @ColumnInfo(name = "comments_count") var commentsCount: Int,
    @ColumnInfo(name = "liked") var liked: Int,
    @ColumnInfo(name = "followed") var followed: Int,
    @ColumnInfo(name = "last_comment_id") var lastCommentID: Int,
    @ColumnInfo(name = "link_preview") var linkPreview: String,
    @ColumnInfo(name = "link_archive") var linkArchive: String
)