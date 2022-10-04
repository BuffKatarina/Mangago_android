package com.twr.mangago.rss

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.twr.mangago.R

class AddRssFragment : Fragment() {
    private lateinit var editRssView: TextInputEditText
    var link : String? = null
    var lastUpdated: String? = null
    var latestChapter: String? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_rss, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        super.onCreate(savedInstanceState)
        val textInputEditText = TextInputEditText(requireContext())
        val rssLayoutFragment = RssLayoutFragment()
        val editTitleDialog = AlertDialog.Builder(context)
            .setTitle(R.string.edit_title)
            .setView(textInputEditText)
            .setPositiveButton(R.string.button_save) { _, _ ->
                val title = textInputEditText.text.toString()
                setFragmentResult("resultKey", bundleOf(
                    "title" to title,
                    "link" to link,
                    "latestChapter" to latestChapter,
                    "lastUpdated" to lastUpdated
                ))
                requireActivity()
                    .supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, rssLayoutFragment, "RssLayoutFragment")
                    .addToBackStack(null)
                    .commit()
            }
            .setNegativeButton(R.string.cancel, null)
        val rssParser = RSSParser()
        editRssView = view.findViewById(R.id.edit_rss)
        val button = view.findViewById<MaterialButton>(R.id.button_search)
        val card = view.findViewById<MaterialCardView>(R.id.parsed_card)
        val cardText = view.findViewById<TextView>(R.id.parsed_rss)
        val progress = view.findViewById<CircularProgressIndicator>(R.id.progress)
        card.setOnClickListener {
            editTitleDialog.show()
        }
        button.setOnClickListener {
            if (TextUtils.isEmpty(editRssView.text)) {
                /*TODO*/
            } else {
                val url = editRssView.text.toString()
                progress.visibility = View.VISIBLE
                rssParser.parseRSS(url).observe(viewLifecycleOwner) { returnrepo ->
                    latestChapter = returnrepo["latestChapter"]
                    link = returnrepo["link"]
                    lastUpdated = returnrepo["link"]
                    card.visibility = View.VISIBLE
                    cardText.visibility = View.VISIBLE
                    cardText.text = returnrepo["title"] + "\n" + returnrepo["link"]
                    progress.visibility = View.GONE
                    textInputEditText.setText(returnrepo["title"])
                }

            }
        }
    }
}





