package com.twr.mangago.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [Rss::class], version = 1, exportSchema = false)
abstract class RssDb : RoomDatabase(){
    abstract fun rssDao() : RssDao

    companion object{
        @Volatile
        private var INSTANCE : RssDb? = null
        fun getDatabase(context : Context,
                        scope: CoroutineScope): RssDb{
            return if (INSTANCE == null){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RssDb::class.java,
                    "rss_db"
                ).addCallback(RssDbCallback(scope)).build()
                INSTANCE = instance
                INSTANCE!!
            } else{
                INSTANCE!!
            }
        }

    }

    private class RssDbCallback(private val scope: CoroutineScope
    ):Callback(){
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let {database ->
                scope.launch {
                    populateDatabase(database.rssDao())
                }
            }
        }
        suspend fun populateDatabase(rssDao:RssDao){
            rssDao.deleteAll()
        }
    }
}