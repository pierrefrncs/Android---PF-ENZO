package app.epf.ratp_eb_pf.ui.detailStation

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.data.SchedulesDao
import app.epf.ratp_eb_pf.model.Schedules
import app.epf.ratp_eb_pf.retrofit
import app.epf.ratp_eb_pf.service.SchedulesService
import kotlinx.coroutines.runBlocking

// Activité qui contient les détails des stations (schedule) après click sur une station

class StationDetailsActivity : AppCompatActivity() {

    private var scheduleDao: SchedulesDao? =null
    private val type = "metro"
    private val code = "1"
    private val slug = "esplanade+de+la+defense"
    private val way = "A"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Bouton retour en haut de la page

        // récup num de la ligne et slug de la station

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

    private fun synchroStationData(view: View){
        val service = retrofit().create(SchedulesService::class.java)
        runBlocking {
                scheduleDao?.deleteSchedules()
                val result = service.getScheduleService(type,code,slug,way)
            result.result.schedule.map {
                val id = 1
                val schedules = Schedules(id, it.message, it.destination)
                scheduleDao?.addSchedules(schedules)
            }
        }
    }
}
