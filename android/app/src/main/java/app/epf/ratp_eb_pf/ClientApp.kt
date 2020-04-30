package app.epf.ratp_eb_pf

import android.app.Application
import com.facebook.stetho.Stetho

class ClientApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Stetho.initializeWithDefaults(this)
    }
}