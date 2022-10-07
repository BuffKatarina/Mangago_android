package com.twr.mangago.rss.worker.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.twr.mangago.rss.RssApplication
import com.twr.mangago.rss.worker.CheckRssUpdatesWorker
import java.util.concurrent.TimeUnit

class CheckRssUpdatesViewModel(application: RssApplication): ViewModel() {
    private val workManager = WorkManager.getInstance(application)
    private val constraints =
        Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
    private val work = PeriodicWorkRequestBuilder<CheckRssUpdatesWorker>(1, TimeUnit.HOURS)
        .setConstraints(constraints)

    fun checkRssUpdates(){
        workManager.enqueue(work.setConstraints(constraints).build())
    }

}

class CheckRssUpdatesModelFactory(private val application: RssApplication): ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return CheckRssUpdatesViewModel(application) as T
     }
}