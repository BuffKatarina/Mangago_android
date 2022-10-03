package com.twr.mangago

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.twr.mangago.db.Rss

class RssRecyclerViewAdapter : ListAdapter<Rss, RssViewHolder>(RssComparator()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RssViewHolder {
        return RssViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: RssViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current.link)
    }
}

class RssViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    private val rssItemView: TextView = itemView.findViewById(R.id.textView)

    fun bind(text:String?){
        rssItemView.text = text
    }
    companion object{
        fun create(parent: ViewGroup): RssViewHolder{
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
