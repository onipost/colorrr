package app.colorrr.colorrr.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "system")
data class TosPrivacy(
    @PrimaryKey(autoGenerate = true) val id: Int?,
    @ColumnInfo(name = "tos") var tos: String,
    @ColumnInfo(name = "privacy") var privacy: String
)