package com.twr.mangago.ui.reader.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.squareup.picasso.Picasso
import com.twr.mangago.R


class ReaderRecyclerViewAdapter(private val imageArray:ArrayList<String>): RecyclerView.Adapter<ReaderRecyclerViewAdapter.ReaderViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReaderViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.reader_fragment_recyclerview, parent, false)
        return ReaderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReaderViewHolder, position: Int) {
        holder.bind(imageArray[position])
    }

    override fun getItemCount() = imageArray.size

    class ReaderViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        private val imageView = itemView.findViewById<ImageView>(R.id.image_view)
        private val progress = itemView.findViewById<CircularProgressIndicator>(R.id.progress)
        init{

        }
        fun bind(imageLink : String){
            Picasso.get().load(imageLink).into(imageView)

        }
}


}

