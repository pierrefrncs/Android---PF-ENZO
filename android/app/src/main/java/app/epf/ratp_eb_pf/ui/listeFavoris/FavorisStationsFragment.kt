package app.epf.ratp_eb_pf.ui.listeFavoris

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.data.AppDatabase
import app.epf.ratp_eb_pf.data.StationsDao
import app.epf.ratp_eb_pf.model.Stations
import app.epf.ratp_eb_pf.ui.detailLine.StationsAdapter
import kotlinx.android.synthetic.main.fragment_favoris_stations.view.*
import kotlinx.coroutines.runBlocking

// Sous-fragment des favoris pour les stations

class FavorisStationsFragment : Fragment() {

    private var stationDaoSaved: StationsDao? = null
    private lateinit var stationsRecyclerView: RecyclerView
    private var stations: MutableList<Stations>? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_favoris_stations, container, false)

        stationsRecyclerView = view.findViewById(R.id.savedStations_recyclerview)
        stationsRecyclerView.layoutManager = LinearLayoutManager(activity)

        // En cas de click sur l'image "Aucune station favorite", envoie vers la liste des stations
        view.noStationsImage.setOnClickListener {
            val navController = activity?.findNavController(R.id.nav_host_fragment)
            navController?.navigate(R.id.navigation_list_lignes)
        }

        val database =
            Room.databaseBuilder(requireContext(), AppDatabase::class.java, "savedDatabase")
                .build()

        stationDaoSaved = database.getStationsDao()

        runBlocking {
            stations = stationDaoSaved?.getStations()
        }

        // Si aucune station favorite, affiche l'image "Aucune station favorite", sinon cachée
        runBlocking {
            if (!stations.isNullOrEmpty()) {
                view.layoutNoSavedStation.visibility = View.GONE
            } else {
                view.layoutNoSavedStation.visibility = View.VISIBLE
            }
        }

        // Ajoute l'adapter des stations (liste déroulante des stations favorites)
        stationsRecyclerView.adapter = StationsAdapter(stations ?: mutableListOf(), view)


        return view
    }

    override fun onResume() {
        super.onResume()

        stationsRecyclerView.adapter = StationsAdapter(stations ?: mutableListOf(), requireView())
    }
}