package com.twr.mangago.rss.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.twr.mangago.R
import com.twr.mangago.db.Rss

class RssRecyclerViewAdapter : ListAdapter<Rss, RssViewHolder>(RssComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RssViewHolder {
        return RssViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: RssViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(listOf<String>(current.title ,current.latestChapter, current.lastUpdated))
    }
}

class RssViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    private val rssTitle: TextView = itemView.findViewById(R.id.title)
    private val latestChapter : TextView = itemView.findViewById(R.id.latest_chapter)
    private val lastUpdated : TextView = itemView.findViewById(R.id.last_updated)

    fun bind(data:List<String>?){
        rssTitle.text = data!![0]
        latestChapter.text = data[1]
        lastUpdated.text = data[2]
    }
    companion object{
        fun create(parent: ViewGroup): RssViewHolder {
            val view: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.rss_recyclerview_row, parent, false)
            return RssViewHolder(view)
        }
    }
}

class RssComparator : DiffUtil.ItemCallback<Rss>() {
    override fun areItemsTheSame(oldItem: Rss, newItem: Rss): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Rss, newItem: Rss): Boolean {
        return oldItem.link == newItem.link
    }
}
