package app.epf.ratp_eb_pf.ui.listeLines.details.stations

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import app.epf.ratp_eb_pf.R

// Activité qui contient les détails des stations (schedule) après click sur une station

class StationDetailsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Bouton retour en haut de la page



    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> { // Configuration bouton retour
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
