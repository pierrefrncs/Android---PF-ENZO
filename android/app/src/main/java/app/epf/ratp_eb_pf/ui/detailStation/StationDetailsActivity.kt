package app.epf.ratp_eb_pf.ui.detailStation

import android.graphics.drawable.Drawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.viewpager.widget.ViewPager
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.daoLi
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.model.Stations
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_details_line.*
import kotlinx.android.synthetic.main.activity_station_details.*
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream

// Activité qui contient les détails des stations (schedule) après click sur une station

class StationDetailsActivity : AppCompatActivity() {

    private var station: Stations? = null
    private var line: Line? = null
    private var lineDao: LineDao? = null
    private var listDestination: List<String>? = null
    private var bundle = Bundle()
    private lateinit var viewpager: ViewPager
    private lateinit var tabLayout: TabLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Bouton retour en haut de la page

        station = intent.getSerializableExtra("station") as Stations

        lineDao = daoLi(this)
        station?.line.toString()
        runBlocking {
            line = lineDao?.getLineSpec(station?.line?.toInt()!!)
        }

        listDestination = line?.directions?.split("/")

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

        bundle.putSerializable("station", station) // Pour que les sous-fragments connaissent les données de la ligne

        viewpager = findViewById(R.id.fragment_pager_horaires)
        viewpager.offscreenPageLimit = 1

        tabLayout = findViewById(R.id.tablayout_details_station)
        tabLayout.setupWithViewPager(viewpager)

        setupViewPager(viewpager)
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

    private fun setupViewPager(viewPager: ViewPager){
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        scope.launch {
            val adapter = StationTabAdapter(supportFragmentManager, bundle)
            adapter.addFragment(HoraireAFragment(), listDestination?.get(0))
            adapter.addFragment(HoraireBFragment(), listDestination?.get(1))
            withContext(Dispatchers.Main){
                viewPager.adapter = adapter
            }
        }
    }

}
