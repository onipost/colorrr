package app.colorrr.colorrr.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import app.colorrr.colorrr.database.dao.*
import app.colorrr.colorrr.entity.*

@Database(
    entities = [
        Palette::class,
        Category::class,
        ImageOriginal::class,
        ImagesToCategories::class,
        ImagePublished::class,
        ImageUnfinished::class,
        ImagePremium::class,
        Session::class,
        User::class,
        TosPrivacy::class,
        Feed::class,
        Comment::class
    ],
    version = 1
)
abstract class AppDB : RoomDatabase() {
    abstract fun paletteDao(): PaletteDao
    abstract fun categoryDao(): CategoryDao
    abstract fun imageDao(): ImageDao
    abstract fun sessionDao(): SessionDao
    abstract fun userDao(): UserDao
    abstract fun systemDao(): SystemDao
    abstract fun feedDao(): FeedDao

    companion object {
        @Volatile
        private var instance: AppDB? = null

        fun getInstance(context: Context): AppDB {
            val tempInstance = this.instance
            if (tempInstance != null)
                return tempInstance

            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDB::class.java,
                    "ColorrrDatabase"
                ).build()
                this.instance = instance
                return instance
            }
        }
    }
}