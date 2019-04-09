package app.colorrr.colorrr.database.dao

import androidx.room.*
import app.colorrr.colorrr.entity.Session
import io.reactivex.Flowable
import io.reactivex.Observable

@Dao
interface SessionDao {
    @Query("SELECT * from session LIMIT 1")
    fun getSession(): Observable<List<Session>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(task: Session)

    @Delete
    fun delete(task: Session)

    @Query("DELETE from session")
    fun clear()
}