package app.colorrr.colorrr.database.converters

import androidx.room.TypeConverter
import kotlin.collections.ArrayList


class PaletteConverter {
    @TypeConverter
    fun fromColors(colors: ArrayList<String>): String {
        var result = ""
        colors.forEach { result += if (result.isNotEmpty()) ",$it" else it }
        return result
    }

    @TypeConverter
    fun toColors(data: String): ArrayList<String> {
        val result = ArrayList<String>()
        data.split(",").forEach { result.add(it) }
        return result
    }
}