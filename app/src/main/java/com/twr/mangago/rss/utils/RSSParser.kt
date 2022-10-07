package com.twr.mangago.rss.utils


import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prof.rssparser.Parser
import com.twr.mangago.R
import kotlinx.coroutines.launch

class RSSParser : ViewModel() {
    private val parser = Parser.Builder().build()

    private suspend fun rssAsLiveData(url : String?, context: Context,result: MutableLiveData<HashMap<String, String>>){
        result.postValue(parseRSS(url, context))
    }

    suspend fun parseRSS(url : String?, context: Context) : HashMap<String, String>{
        val rssHashmap : HashMap<String, String> = HashMap()
        try{
            val channel = parser.getChannel(url!!)
            val title = channel.title
            val article = channel.articles[0]
            val latestChapter = article.title
            val lastUpdated = article.pubDate
            rssHashmap["title"] = title!!
            rssHashmap["latestChapter"] = latestChapter!!
            rssHashmap["link"] = url
            rssHashmap["lastUpdated"] = lastUpdated!!

        }
        catch(e : Exception){
            e.printStackTrace()
            rssHashmap["error"] = context.getString(R.string.rss_search_error)
        }
        return rssHashmap

    }
    fun getParsedRss(url:String?, context: Context):LiveData<HashMap<String,String>>{
        val result = MutableLiveData<HashMap<String, String>>()
        viewModelScope.launch {
            rssAsLiveData(url, context, result)
        }
    return result

    }

}
