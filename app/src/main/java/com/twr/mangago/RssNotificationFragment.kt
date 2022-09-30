package com.twr.mangago

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class RssNotificationFragment : Fragment() {
    var rssView: View? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rssView = inflater.inflate(R.layout.rss_notification_fragment, container, false)
        return view
    } /* @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        }*/
}