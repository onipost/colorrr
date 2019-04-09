package app.colorrr.colorrr.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session")
data class Session(
    @PrimaryKey @ColumnInfo(name = "userID") var userID: Int,
    @ColumnInfo(name = "sessionID") var sessionID: String
)