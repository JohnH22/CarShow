package gr.ihu.ict.carshow

import android.app.Application
import gr.ihu.ict.carshow.data.local.AppDatabase
import gr.ihu.ict.carshow.data.local.DatabaseProvider
import gr.ihu.ict.carshow.utils.createNotificationChannel


class MyApplication : Application() {
    val database: AppDatabase by lazy {
        DatabaseProvider.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()


        createNotificationChannel(this)
    }
}