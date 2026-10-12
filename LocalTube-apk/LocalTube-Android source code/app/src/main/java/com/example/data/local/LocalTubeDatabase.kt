package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PlaylistEntity::class,
        PlaylistItemEntity::class,
        VideoTagEntity::class,
        TagEntity::class,
        VideoMetadataEntity::class,
        CustomChannelEntity::class,
        WatchHistoryEntity::class,
        LikedShortEntity::class,
        FavoriteVideoEntity::class,
        FolderSettingEntity::class,
        SubscribedChannelEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class LocalTubeDatabase : RoomDatabase() {

    abstract fun localTubeDao(): LocalTubeDao

    companion object {
        @Volatile
        private var INSTANCE: LocalTubeDatabase? = null

        fun getDatabase(context: Context): LocalTubeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LocalTubeDatabase::class.java,
                    "localtube_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Prepopulate default playlists and common tags
                            CoroutineScope(Dispatchers.IO).launch {
                                val dao = getDatabase(context).localTubeDao()
                                dao.insertPlaylist(
                                    PlaylistEntity(name = "Liked videos", isDefault = true)
                                )
                                dao.insertPlaylist(
                                    PlaylistEntity(name = "Watch Later", isDefault = true)
                                )
                                dao.insertPlaylist(
                                    PlaylistEntity(name = "Favorites", isDefault = true)
                                )
                                dao.insertPlaylist(
                                    PlaylistEntity(name = "Offline Mix", isDefault = false)
                                )

                                val defaultTags = listOf(
                                    "music", "camera", "shorts", "tutorial", "travel",
                                    "highlights", "gaming", "offline", "favorites", "vlog"
                                )
                                defaultTags.forEach { tag ->
                                    dao.insertAppTag(TagEntity(tag = tag))
                                }
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
