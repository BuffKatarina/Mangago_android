package com.twr.mangago.rss

import android.app.AlertDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.twr.mangago.R
import com.twr.mangago.hideKeyboard

class AddRssFragment : Fragment() {
    private lateinit var editRssView: TextInputEditText
    private var link : String? = null
    private var lastUpdated: String? = null
    private var latestChapter: String? = null
    private lateinit var bottomNavigationView:BottomNavigationView



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_add_rss, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var textInputEditText:TextInputEditText? = null
        val rssParser = RSSParser()
        var fromSearch = false
        editRssView = view.findViewById(R.id.edit_rss)
        var parsedSuccessful = false
        val searchButton = view.findViewById<MaterialButton>(R.id.button_search)
        val card = view.findViewById<MaterialCardView>(R.id.parsed_card)
        val cardText = view.findViewById<TextView>(R.id.parsed_rss)
        val manager = requireActivity().supportFragmentManager
        bottomNavigationView = requireActivity().findViewById(R.id.bottomNavigationMenu)
        bottomNavigationView.visibility = View.GONE
        val progress = view.findViewById<CircularProgressIndicator>(R.id.progress)
        var title: String? = null
        parentFragmentManager.setFragmentResultListener("rssOnLink", viewLifecycleOwner){_, bundle ->
            val rssOnLink = bundle.getString("url")
            editRssView.setText(rssOnLink)
            searchButton.performClick()
            searchButton.visibility = View.GONE
        }

        searchButton.setOnClickListener {
            hideKeyboard()
            if (TextUtils.isEmpty(editRssView.text)) {
                Toast.makeText(context, getString(R.string.empty), Toast.LENGTH_SHORT).show()
            }
            textInputEditText = TextInputEditText(requireContext())
            val url = editRssView.text.toString()
            progress.visibility = View.VISIBLE
            rssParser.parseRSS(url, requireContext()).observe(viewLifecycleOwner) { parsedMap ->
                if (parsedMap.size == 1 ){
                    //parsedMap = {"error":error message}
                    card.visibility = View.VISIBLE
                    cardText.visibility = View.VISIBLE
                    progress.visibility = View.GONE
                    cardText.text = parsedMap["error"]
                }
                else{
                    latestChapter = parsedMap["latestChapter"]
                    link = parsedMap["link"]
                    lastUpdated = parsedMap["lastUpdated"]
                    card.visibility = View.VISIBLE
                    cardText.visibility = View.VISIBLE
                    title = parsedMap["title"]
                    cardText.text = getString(R.string.add_rss_fragment_card_text,
                        title,
                        parsedMap["link"])
                    progress.visibility = View.GONE
                    textInputEditText!!.setText(parsedMap["title"])
                    fromSearch = true
                    parsedSuccessful = true
                }
            }


        }
        val editTitleDialog = AlertDialog.Builder(context)
            .setTitle(R.string.edit_title)
            .setPositiveButton(R.string.button_save) { _, _ ->
                val editedTitle = textInputEditText!!.text.toString()
                setFragmentResult("rssKey", bundleOf(
                    "title" to editedTitle,
                    "link" to link,
                    "latestChapter" to latestChapter,
                    "lastUpdated" to lastUpdated
                ))

               manager
                    .beginTransaction()
                    .remove(this)
                    .commit()
                    manager.popBackStack()


            }
            .setNegativeButton(R.string.cancel,null)
        card.setOnClickListener {
            if (!parsedSuccessful){
                //refreshes parsing of rss
                searchButton.performClick()
            }
            else{
                //Recycles the input text view from searchButton if
                //textInputEditText view already created in search and
                // alert dialog is settings
                //textInputEditText as view for the same time
                if (!fromSearch){
                    textInputEditText = TextInputEditText(requireContext())
                    textInputEditText!!.setText(title!!)
                    }
                fromSearch = false
                editTitleDialog.setView(textInputEditText)
                editTitleDialog.show()
            }
        }

    }

    override fun onDestroy() {
        bottomNavigationView.visibility = View.VISIBLE
        super.onDestroy()
    }
}





