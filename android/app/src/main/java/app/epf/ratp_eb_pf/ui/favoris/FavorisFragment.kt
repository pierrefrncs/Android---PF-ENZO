package app.epf.ratp_eb_pf.ui.favoris

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager.widget.ViewPager
import app.epf.ratp_eb_pf.R
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.*

// Fragment global contenant les favoris

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

        // View pager pour l'affichage des 2 sous fragments (favoris lignes et stations)
        viewpager = view.findViewById(R.id.fragment_rechercheinterneFavoris)
        setupViewPager(viewpager)
        viewpager.offscreenPageLimit = 1 // Nombre de sous fragments - 1 pour améliorer la fluidité
        tabLayout = view.findViewById(R.id.tablayout_favoris)
        tabLayout.setupWithViewPager(viewpager)

        return view
    }


    private fun setupViewPager(viewPager: ViewPager) {
        val scope =
            CoroutineScope(Dispatchers.Default + SupervisorJob()) // Pour essayer d'accélerer le tout

        //https://stackoverflow.com/a/18847195/13289762

        scope.launch {
            val adapter = FavorisTabAdapter(childFragmentManager, bundle)
            adapter.addFragment(FavorisLinesFragment(), "Lignes") // Ajoute le sous-fragment lignes
            adapter.addFragment(FavorisStationsFragment(), "Stations") // Ajoute le sous-fragment stations
            withContext(Dispatchers.Main) {
                viewPager.adapter = adapter
            }
        }

    }

}
