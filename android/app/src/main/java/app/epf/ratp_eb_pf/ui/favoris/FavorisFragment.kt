package app.epf.ratp_eb_pf.ui.favoris

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.viewpager.widget.ViewPager
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.data.AppDatabase
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.ui.listeLines.LinesAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.fragment_favoris.view.*
import kotlinx.coroutines.*

class FavorisFragment : Fragment() {

    private lateinit var favorisViewModel: FavorisViewModel

    private lateinit var viewpager: ViewPager
    private lateinit var tabLayout: TabLayout
    private var bundle = Bundle()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        favorisViewModel =
            ViewModelProviders.of(this).get(FavorisViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_favoris, container, false)

        viewpager = view.findViewById(R.id.fragment_rechercheinterneFavoris)
        setupViewPager(viewpager)
        viewpager.offscreenPageLimit = 1
        tabLayout = view.findViewById(R.id.tablayout_favoris)
        tabLayout.setupWithViewPager(viewpager)

        return view
    }


    private fun setupViewPager(viewPager: ViewPager) {
        val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

        //https://stackoverflow.com/a/18847195/13289762

        scope.launch {
            val adapter =
                FavorisTabAdapter(childFragmentManager, bundle)
            adapter.addFragment(FavorisLinesFragment(), "Lignes")
            adapter.addFragment(FavorisStationsFragment(), "Stations")
            withContext(Dispatchers.Main) {
                viewPager.adapter = adapter
            }
        }

    }

}
