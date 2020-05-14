package app.epf.ratp_eb_pf.ui.detailLine

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.model.Line
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_details_line.*
import kotlinx.coroutines.*
import java.io.IOException
import java.io.InputStream

class DetailsLineActivity : AppCompatActivity() {

    private var line: Line? = null
    private lateinit var viewpager: ViewPager
    private lateinit var tabLayout: TabLayout
    private var bundle = Bundle()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_line)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        line = intent.getSerializableExtra("line") as Line

        LineNameDetail.text = line?.name

        var ims: InputStream? = null
        try {
            ims = assets.open("metroLines/M${line?.code}genRVB.png")
            val d = Drawable.createFromStream(ims, null)
            logoDetailLine.setImageDrawable(d)
        } catch (ex: IOException) {
            //file does not exist
            logoDetailLine.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp)
        } finally {
            ims?.close()
        }

        LineDirectionsDetail.text = line?.directions

        bundle.putSerializable("line", line)
        viewpager = findViewById(R.id.fragment_rechercheinterne)
        setupViewPager(viewpager)
        viewpager.offscreenPageLimit = 1
        tabLayout = findViewById(R.id.tablayout_details)
        tabLayout.setupWithViewPager(viewpager)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupViewPager(viewPager: ViewPager) {
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        //https://stackoverflow.com/a/18847195/13289762

        scope.launch {
            val adapter =
<<<<<<< Updated upstream
                DetailsTabAdapter(
                    supportFragmentManager,
                    bundle
                )
            adapter.addFragment(StationsDetailsFragment(), "Ligne")
            adapter.addFragment(TrafficDetailsFragment(), "Etat du trafic")
            withContext(Dispatchers.Main) {
=======
                DetailsTabAdapter(supportFragmentManager, bundle)
                adapter.addFragment(StationsListFragment(), "Stations") // Ajoute sous-fragment avec la liste des stations
                adapter.addFragment(TrafficDetailsFragment(), "Etat du trafic") // Ajoute sous-fragment pour l'état du traffic
                withContext(Dispatchers.Main) {
>>>>>>> Stashed changes
                viewPager.adapter = adapter
            }
        }
    }
}
