package com.twr.mangago.rss

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.twr.mangago.R
import com.twr.mangago.db.Rss
import com.twr.mangago.rss.adapter.RssRecyclerViewAdapter
import com.twr.mangago.rss.model.RssViewModel
import com.twr.mangago.rss.model.RssViewModelFactory

@Suppress("DEPRECATION")
class RssLayoutFragment : Fragment() {
    private val rssViewModel: RssViewModel by viewModels {
        RssViewModelFactory((requireActivity().application as RssApplication).repository)
    }



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        parentFragmentManager.setFragmentResultListener("resultKey", viewLifecycleOwner){ _, bundle ->
            val title = bundle.getString("title")
            val link = bundle.getString("link")
            val lastUpdated = bundle.getString("lastUpdated")
            val latestChapter = bundle.getString("latestChapter")
            rssViewModel.insert(Rss(link!!, title!!, lastUpdated!!, latestChapter!!))
        }
        return inflater.inflate(R.layout.rss_layout_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recyclerView = view.findViewById<RecyclerView>(R.id.feedRecyclerView)
        val adapter = RssRecyclerViewAdapter()
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
        rssViewModel.allRss.observe(viewLifecycleOwner) { rss ->
            rss?.let { adapter.submitList(it) }
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            val addRssFragment = AddRssFragment()
            requireActivity().supportFragmentManager.beginTransaction().
            replace(R.id.fragment_container, addRssFragment, "AddRssFragment")
                .addToBackStack(null).commit()
        }
    }

    }


