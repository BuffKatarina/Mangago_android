package com.twr.mangago.rss.adapter


import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.twr.mangago.R
import com.twr.mangago.db.Rss
import com.twr.mangago.rss.model.RssViewModel

class RssRecyclerViewAdapter(viewModel: RssViewModel) : ListAdapter<Rss, RssViewHolder>(RssComparator()) {
    private val rssViewModel = viewModel

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RssViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.rss_recyclerview_row, parent, false)
        return RssViewHolder(view,{pos->
            val current = getItem(pos)
            rssViewModel.delete(Rss(current.link, current.title, current.lastUpdated, current.latestChapter))
        },{})

    }

    override fun onBindViewHolder(holder: RssViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(listOf<String>(current.title ,current.latestChapter, current.lastUpdated))
    }
}

class RssViewHolder(itemView: View, listener1: DeleteListener, listener2: EditListener) : RecyclerView.ViewHolder(itemView), OnClickListener{
    private val rssTitle: TextView = itemView.findViewById(R.id.title)
    private val latestChapter : TextView = itemView.findViewById(R.id.latest_chapter)
    private val lastUpdated : TextView = itemView.findViewById(R.id.last_updated)
    private val delete = itemView.findViewById<MaterialButton>(R.id.button_delete)
    private val edit = itemView.findViewById<MaterialButton>(R.id.button_edit)
    private val deleteListener = listener1
    private val editListener = listener2

    init{
        delete.setOnClickListener(this)
        edit.setOnClickListener(this)
    }
    fun bind(data:List<String>?){
        rssTitle.text = data!![0]
        latestChapter.text = data[1]
        lastUpdated.text = data[2]

    }

    override fun onClick(view: View?) {
        when(view?.id){
            R.id.button_edit ->{
                editListener.onEdit(this.layoutPosition)
            }
            R.id.button_delete -> {
                deleteListener.onDelete(this.layoutPosition)
            }
        }
    }


}

fun interface DeleteListener{
    fun onDelete(position: Int)
}
fun interface EditListener{
    fun onEdit(position: Int)

}


class RssComparator : DiffUtil.ItemCallback<Rss>() {
    override fun areItemsTheSame(oldItem: Rss, newItem: Rss): Boolean {
        return oldItem === newItem
    }

    override fun areContentsTheSame(oldItem: Rss, newItem: Rss): Boolean {

        return (oldItem.link == newItem.link)
       }
}





