package app.colorrr.colorrr.database.dao

import androidx.room.*
import app.colorrr.colorrr.entity.TosPrivacy

@Dao
interface SystemDao {

    @Query("SELECT * from system LIMIT 1")
    fun get(): TosPrivacy

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(task: TosPrivacy)

    @Query("DELETE FROM system")
    fun delete()
}