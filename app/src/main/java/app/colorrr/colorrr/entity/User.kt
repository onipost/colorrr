package app.colorrr.colorrr.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
open class User(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userID: Int,
    @ColumnInfo(name = "email")
    var email: String,
    @ColumnInfo(name = "name")
    var name: String,
    @ColumnInfo(name = "image")
    var image: String,
    @ColumnInfo(name = "alerts")
    var alerts: Int,
    @ColumnInfo(name = "dark_theme")
    var darkTheme: Int,
    @ColumnInfo(name = "repaint")
    var repaint: Int,
    @ColumnInfo(name = "premium")
    var premium: Int,
    @ColumnInfo(name = "is_anonymous")
    var isAnonymous: Boolean,
    @ColumnInfo(name = "followers_count")
    var followersCount: Int,
    @ColumnInfo(name = "followed_count")
    var followedCount: Int,
    @ColumnInfo(name = "likes_count")
    var likesCount: Int,
    @ColumnInfo(name = "published_count")
    var publishedCount: Int
)