package app.colorrr.colorrr.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "categories")
data class Category(
    @PrimaryKey var id: Int?,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "description") var description: String,
    @ColumnInfo(name = "show_in_feed") var show_in_feed: Int,
    @ColumnInfo(name = "featured") var featured: Int,
    @ColumnInfo(name = "cover_image") var cover_image: String,
    @ColumnInfo(name = "timestamp") var timestamp: Double,
    @ColumnInfo(name = "debug") var debug: Int
) {
    constructor() : this(-1, "", "", 0, 0, "", 0.0, 0)

}