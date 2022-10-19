package com.twr.mangago.rss.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rss_table")
class Rss(
    @PrimaryKey @ColumnInfo(name = "rss_link") val link: String,
    @ColumnInfo(name = "title") val title:String,
    @ColumnInfo(name = "lastUpdated") val lastUpdated: String,
    @ColumnInfo(name = "latestChapter") val latestChapter:String,
)