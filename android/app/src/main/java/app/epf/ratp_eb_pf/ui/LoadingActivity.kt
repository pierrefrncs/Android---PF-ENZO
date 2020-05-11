package app.epf.ratp_eb_pf.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import app.epf.ratp_eb_pf.MainActivity
import app.epf.ratp_eb_pf.R

// Splash activity s'affichant au démarrage de l'application

class LoadingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        Handler().postDelayed({
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        },1000)
    }
}
