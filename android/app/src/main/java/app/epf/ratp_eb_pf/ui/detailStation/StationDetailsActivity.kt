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

<<<<<<< Updated upstream
        // récup num de la ligne et slug de la station
=======
        station = intent.getSerializableExtra("station") as Stations

        StationNameDetail.text = station?.name

        // Permet d'accéder aux fichiers du package "assets" --> logos des lignes
        var ims: InputStream? = null
        try {
            ims = assets.open("metroLines/M${station?.line}genRVB.png")
            val d = Drawable.createFromStream(ims, null)
            logoDetailStation.setImageDrawable(d)
        } catch (ex: IOException) {
            //file does not exist --> Affiche une image par défaut
            logoDetailLine.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp)
        } finally {
            ims?.close()
        }

        bundle.putSerializable("station", station) // Pour que les sous-fragments connaissent les données de la station
>>>>>>> Stashed changes

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

<<<<<<< Updated upstream
    private fun synchroStationData(view: View){
        val service = retrofit().create(SchedulesService::class.java)
        runBlocking {
                scheduleDao?.deleteSchedules()
                val result = service.getScheduleService(type,code,slug,way)
            result.result.schedule.map {
                val id = 1
                val schedules = Schedules(id, it.message, it.destination)
                scheduleDao?.addSchedules(schedules)
=======
    private fun setupViewPager(viewPager: ViewPager){
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        // A MODIFIER POUR AFFICHER LES DESTINATION ET PAS UN TITRE GÉNÉRIQUE
        scope.launch {
            val adapter = StationTabAdapter(supportFragmentManager, bundle)
            adapter.addFragment(HoraireAFragment(), "Direction A")
            adapter.addFragment(HoraireBFragment(), "Direction B")
            withContext(Dispatchers.Main){
                viewPager.adapter = adapter
>>>>>>> Stashed changes
            }
        }
    }
}
