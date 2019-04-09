package app.colorrr.colorrr.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "images_premium")
data class ImagePremium(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "image_id") val imageID: Int,
    @ColumnInfo(name = "user_id") val userID: Int
)