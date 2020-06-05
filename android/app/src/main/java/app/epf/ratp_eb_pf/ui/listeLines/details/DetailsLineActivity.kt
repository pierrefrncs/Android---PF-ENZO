package app.epf.ratp_eb_pf.ui.listeLines.details

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.daoTraf
import app.epf.ratp_eb_pf.data.TrafficDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.model.Traffic
import app.epf.ratp_eb_pf.retrofit
import app.epf.ratp_eb_pf.service.TrafficSpecService
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_details_line.*
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream

// Activité qui contient les détails d'une ligne (dans des sous-fragments)

class DetailsLineActivity : AppCompatActivity() {

    private var line: Line? = null
    private lateinit var viewpager: ViewPager
    private lateinit var tabLayout: TabLayout
    private var bundle = Bundle()
    private var trafficDao: TrafficDao? = null
    private var traffic: Traffic? = null

    // Fragment global contenant les details d'une ligne après click dessus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_line)

        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Bouton retour en haut de la page

        line =
            intent.getSerializableExtra("line") as Line // recupère la ligne sur lequel l'user a cliqué

        LineNameDetail.text = line?.name

        // Permet d'accéder aux fichiers du package "assets" --> logos des lignes
        var ims: InputStream? = null
        try {
            ims = assets.open("metroLines/M${line?.code}genRVB.png")
            val d = Drawable.createFromStream(ims, null)
            logoDetailLine.setImageDrawable(d)
        } catch (ex: IOException) {
            //file does not exist --> Affiche une image par défaut
            logoDetailLine.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp)
        } finally {
            ims?.close()
        }

        LineDirectionsDetail.text = line?.directions

        trafficDao = daoTraf(this)
        val service = retrofit().create(TrafficSpecService::class.java)
        runBlocking {
            traffic = trafficDao?.getTraffic(line!!.code)
            val idTraf = traffic!!.id
            val result = service.getTrafficSpecService("metros", line!!.code)
            traffic = Traffic(
                idTraf,
                result.result.line,
                result.result.slug,
                result.result.title,
                result.result.message
            )
            trafficDao?.updateTraffic(
                traffic!!.line,
                traffic!!.title,
                traffic!!.message
            )  // Ajoute la station dans la bdd

        }

        //set traffic indicator color
        if (traffic!!.slug.equals("normal")) {
            status_traffic_detail.setColorFilter(
                ContextCompat.getColor(
                    this,
                    R.color.trafficOk
                ), android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else if (traffic!!.slug.equals("critical")) {
            status_traffic_detail.setColorFilter(
                ContextCompat.getColor(this, R.color.trafficPerturbé),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        } else {
            status_traffic_detail.setColorFilter(
                ContextCompat.getColor(this, R.color.trafficTravaux),
                android.graphics.PorterDuff.Mode.SRC_IN
            )
        }

        bundle.putSerializable(
            "line",
            line
        ) // Pour que les sous-fragments connaissent les données de la ligne
        bundle.putSerializable("traffic", traffic)
        viewpager = findViewById(R.id.fragment_rechercheinterne)
        setupViewPager(viewpager)
        viewpager.offscreenPageLimit = 1 // Nombre de sous-fragments - 1 pour améliorer la fluidité
        tabLayout = findViewById(R.id.tablayout_details)
        tabLayout.setupWithViewPager(viewpager)
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

    // Setup ViewPager avec les sous-fragments
    private fun setupViewPager(viewPager: ViewPager) {
        val scope =
            CoroutineScope(Dispatchers.Main + SupervisorJob()) // Pour essayer d'améliorer la réactivité

        //https://stackoverflow.com/a/18847195/13289762

        scope.launch {
            val adapter =
                DetailsTabAdapter(supportFragmentManager, bundle)
            adapter.addFragment(
                StationsListFragment(),
                "Stations"
            ) // Ajoute sous-fragment avec la liste des stations
            adapter.addFragment(
                TrafficDetailsFragment(),
                "Etat du trafic"
            ) // Ajoute sous-fragment pour l'état du traffic
            withContext(Dispatchers.Main) {
                viewPager.adapter = adapter
            }
        }

    }
}
