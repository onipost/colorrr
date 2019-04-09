package app.colorrr.colorrr.database.converters

import androidx.room.TypeConverter

class ImageRelativeConverter {
    @TypeConverter
    fun fromRelated(colors: ArrayList<Int>): String {
        var result = ""
        colors.forEach {
            result += if (result.isNotEmpty()) ",$it" else it
        }
        return result
    }

    @TypeConverter
    fun toRelated(data: String): ArrayList<Int> {
        val result = ArrayList<Int>()
        data.split(",").forEach {
            if (it != "")
                result.add(it.toInt())
        }
        return result
    }
}