package com.twr.mangago

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.twr.mangago.db.Rss

@Suppress("DEPRECATION")
class RssLayoutFragment : Fragment() {
    private val addRssActivityRequestCode = 1
    private val rssViewModel: RssViewModel by viewModels {
        RssViewModelFactory((requireActivity().application as RssApplication).repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
            val intent = Intent(context, AddRssActivity::class.java)
            startActivityForResult(intent, addRssActivityRequestCode)
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, intentData: Intent?) {
        super.onActivityResult(requestCode, resultCode, intentData)

        if (requestCode == addRssActivityRequestCode && resultCode == Activity.RESULT_OK) {
            val rssParser = RSSParser()
            intentData?.getStringExtra(AddRssActivity.EXTRA_REPLY)?.let { reply ->
                rssParser.parseRSS(reply).observe(viewLifecycleOwner) { returnrepo ->
                    Log.i("IDK",returnrepo.toString())
                    val parsed = returnrepo
                    val rss = Rss(
                        parsed["link"]!!,
                    parsed["title"]!!,
                    parsed["lastUpdated"]!!,
                    parsed["latestChapter"]!!)
                    rssViewModel.insert(rss)
                    val adapter = RssRecyclerViewAdapter()
                    adapter.notifyDataSetChanged()
                }
            }
        } else {
            Toast.makeText(
                context,
                "empty not saved",
                Toast.LENGTH_LONG
            ).show()
        }
    }

}
