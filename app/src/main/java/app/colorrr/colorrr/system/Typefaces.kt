package app.colorrr.colorrr.system

import android.content.Context
import android.graphics.Typeface
import java.util.concurrent.ConcurrentHashMap

object Typefaces {
    private val cache = ConcurrentHashMap<String, Typeface>()

    operator fun get(c: Context, assetPath: String): Typeface? {
        synchronized(cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    cache[assetPath] = Typeface.createFromAsset(c.assets, assetPath)
                } catch (e: Exception) {
                    return null
                }
            }
            return cache[assetPath]
        }
    }
}