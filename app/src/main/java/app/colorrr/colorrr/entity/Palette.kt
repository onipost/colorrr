package app.colorrr.colorrr.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import app.colorrr.colorrr.database.converters.PaletteConverter

@Entity(tableName = "palettes")
data class Palette(
    @PrimaryKey var id: Int?,
    @ColumnInfo(name = "user_id") var user_id: Int,
    @ColumnInfo(name = "premium") var premium: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "main_color") var main_color: String,
    @ColumnInfo(name = "debug") var debug: Int,
    @field:TypeConverters(PaletteConverter::class) @ColumnInfo(name = "colors") var colors: ArrayList<String>
) {
    constructor() : this(-1, -1, 0, "", "", 0, ArrayList<String>())

}