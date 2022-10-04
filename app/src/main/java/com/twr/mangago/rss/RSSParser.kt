package com.twr.mangago.rss

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prof.rssparser.Parser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RSSParser : ViewModel() {

    fun parseRSS(url : String?) : LiveData<HashMap<String,String>>{
        val parser = Parser.Builder().build()
        val result = MutableLiveData<HashMap<String, String>>()
        val rssHashmap : HashMap<String, String> = HashMap()
        viewModelScope.launch{
            withContext(Dispatchers.IO){
                try{
                    val channel = parser.getChannel(url!!)
                    val title = channel.title
                    val article = channel.articles[0]
                    val latestChapter = article.title
                    val link= article.link
                    val lastUpdated = channel.lastBuildDate
                    rssHashmap["title"] = title!!
                    rssHashmap["latestChapter"] = latestChapter!!
                    rssHashmap["link"] = link!!
                    rssHashmap["lastUpdated"] = lastUpdated!!
                    result.postValue(rssHashmap)

                }
                catch(e : Exception){
                    e.printStackTrace()
                }
            }
        }
        return result

    }


}
