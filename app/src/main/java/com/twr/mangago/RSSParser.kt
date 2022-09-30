package com.twr.mangago

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prof.rssparser.Parser
import kotlinx.coroutines.launch

class RSSParser : ViewModel() {

    fun parseRSS(url : String?){
        val parser = Parser.Builder().build()
        viewModelScope.launch{
        try{
            val channel = parser.getChannel(url!!)
            var title = channel.title
            var link = channel.link
            var date = channel.lastBuildDate
            var description = channel.articles[1]
            Log.i("DESCRIPTION!",channel.articles.toString())
            Log.i("DESCRIPTION", description.toString())

        }
        catch(e : Exception){
            e.printStackTrace()
        }
    }
    }
}
