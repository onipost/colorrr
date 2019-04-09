package app.colorrr.colorrr.database.dao

import androidx.room.*
import app.colorrr.colorrr.entity.Palette
import io.reactivex.Observable
import io.reactivex.Single

@Dao
interface PaletteDao {

    @Query("SELECT * from palettes WHERE id = :id")
    fun get(id: Int): Single<Palette>

    @Query("SELECT * from palettes")
    fun getAll(): Single<List<Palette>>

    @Query("SELECT * from palettes WHERE user_id = 0")
    fun getSystem(): Observable<List<Palette>>

    @Query("SELECT * from palettes WHERE user_id != 0")
    fun getUserPalettes(): Single<List<Palette>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(task: Palette)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(items: List<Palette>)

    @Delete
    fun delete(task: Palette)

    @Query("DELETE FROM palettes WHERE user_id = 0")
    fun deleteAdminItems()

    @Query("DELETE FROM palettes WHERE user_id != 0")
    fun deleteUserItems()

    @Update
    fun update(task: Palette)
}