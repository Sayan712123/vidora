package com.vidora.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.vidora.app.data.AppGraph
import com.vidora.app.data.UserStore

class VidoraApplication : Application(), ImageLoaderFactory {

    override fun onCreate() {
        super.onCreate()
        AppGraph.init(UserStore(this))
    }

    /**
     * Tuned for low-end phones: a small memory cache, a moderate disk cache, no
     * crossfade (one less animation per image) and hardware bitmaps where supported.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .crossfade(false)
            .allowHardware(true)
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(this@VidoraApplication, 0.15)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("thumbnails"))
                    .maxSizeBytes(64L * 1024 * 1024)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()
}
