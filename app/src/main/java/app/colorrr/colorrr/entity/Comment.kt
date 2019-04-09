package app.colorrr.colorrr.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "comments")
class Comment(
    @PrimaryKey var id: Int?,
    @ColumnInfo(name = "post_id") var postID: Int,
    @ColumnInfo(name = "text") var text: String,
    @ColumnInfo(name = "timestamp") var timestamp: Double,
    @ColumnInfo(name = "sender_id") var senderID: Int,
    @ColumnInfo(name = "sender_name") var senderName: String,
    @ColumnInfo(name = "sender_avatar") var senderAvatar: String
)