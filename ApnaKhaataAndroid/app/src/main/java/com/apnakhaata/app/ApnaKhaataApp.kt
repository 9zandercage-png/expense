package com.apnakhaata.app

import android.app.Application
import com.apnakhaata.app.data.AppDatabase
import com.apnakhaata.app.sms.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApnaKhaataApp : Application() {

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannel(this)

        // Seed default category rules on first launch
        val db = AppDatabase.get(this)
        CoroutineScope(Dispatchers.IO).launch {
            if (db.ruleDao().count() == 0) {
                db.ruleDao().insertAll(AppDatabase.DEFAULT_RULES)
            }
        }
    }
}
