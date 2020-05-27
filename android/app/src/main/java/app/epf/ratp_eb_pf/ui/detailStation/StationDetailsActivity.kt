package app.epf.ratp_eb_pf.ui.detailStation

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.daoLi
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.model.Stations
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_details_line.*
import kotlinx.android.synthetic.main.activity_station_details.*
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream
import java.text.Normalizer

// Activité qui contient les détails des stations (schedule) après click sur une station

class StationDetailsActivity : AppCompatActivity() {

    private val regexUnaccent = "\\p{InCombiningDiacriticalMarks}+".toRegex()
    private val regexParentheses = "\\(.*?\\)".toRegex()

    private var station: Stations? = null
    private var line: Line? = null
    private var lineDao: LineDao? = null
    private var listDestination: List<String>? = null
    private var bundle = Bundle()
    private lateinit var viewpager: ViewPager
    private lateinit var tabLayout: TabLayout
    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_station_details)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Bouton retour en haut de la page

        station = intent.getSerializableExtra("station") as Stations

        // Récupère les coordonnées des stations dans le fichier .csv
        val coordonnees = resources.openRawResource(R.raw.coordonnees_stations)
        val tsvReader = csvReader {
            delimiter = ';'
        }
        val listAirports: List<Map<String, String>> =
            tsvReader.readAllWithHeader(coordonnees)

        // Permet de récupérer les coordonnées GPS de la station
        var location = LatLng(1.0, 1.0)
        run loop@{
            listAirports.map { itMap ->
                itMap.map {
                    if (it.value.unAccent() == station?.name?.unAccent() && !itMap["coord"].isNullOrEmpty()) {
                        val coList = itMap["coord"]?.split(",")
                        val lat = coList?.get(0)
                        val long = coList?.get(1)
                        location = LatLng(lat!!.toDouble(), long!!.toDouble())
                        return@loop //permet de quitter la boucle
                    }
                }
            }
        }

        // Génère la carte GoogleMaps avec un marqueur pour la position
        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync {
            googleMap = it
            googleMap.addMarker(MarkerOptions().position(location).title(station!!.name))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
        }

        lineDao = daoLi(this)
        station?.line.toString()
        runBlocking {
            line = lineDao?.getLineSpec(station?.line!!)
        }

        listDestination = line?.directions?.split(" / ")

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

        // Pour que les sous-fragments connaissent les données de la ligne
        bundle.putSerializable("station", station)

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

    // Pour ignorer les accents dans l'input
    private fun CharSequence.unAccent(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return regexUnaccent.replace(temp, "")
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        scope.launch {
            val adapter = StationTabAdapter(supportFragmentManager, bundle)
            // N'affiche qu'un seul onglet si terminus
            val stationNameTrimmed = station?.name?.replace(regexParentheses, "")?.trim()
            if (stationNameTrimmed != listDestination?.get(0))
                listDestination?.get(0)?.let { adapter.addFragment(HoraireAFragment(), it) }
            if (stationNameTrimmed != listDestination?.get(1))
                listDestination?.get(1)?.let { adapter.addFragment(HoraireBFragment(), it) }
            withContext(Dispatchers.Main) {
                viewPager.adapter = adapter
            }
        }
    }

    // Interface pour pouvoir actualiser les 2 fragments en même temps
    interface RefreshPage {
        fun refreshPage()
    }

}
