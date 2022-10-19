package com.twr.mangago.rss.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Rss::class], version = 1, exportSchema = false)
abstract class RssDb : RoomDatabase(){
    abstract fun rssDao() : RssDao

    companion object{
        @Volatile
        private var INSTANCE : RssDb? = null
        fun getDatabase(context : Context
                        ): RssDb {
            return if (INSTANCE == null){
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RssDb::class.java,
                    "rss_db"
                ).build()
                INSTANCE = instance
                INSTANCE!!
            } else{
                INSTANCE!!
            }
        }

    }

}