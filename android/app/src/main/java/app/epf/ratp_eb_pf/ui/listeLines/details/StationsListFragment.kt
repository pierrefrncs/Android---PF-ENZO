package app.epf.ratp_eb_pf.ui.listeLines.details

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.daoSta
import app.epf.ratp_eb_pf.data.AppDatabase
import app.epf.ratp_eb_pf.data.StationsDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.model.Stations
import app.epf.ratp_eb_pf.retrofit
import app.epf.ratp_eb_pf.service.StationsService
import kotlinx.coroutines.runBlocking

// Sous-fragment (de DetailsLineActivity) qui contient la liste des stations d'une ligne

class StationsListFragment : Fragment() {

    private var stationsDao: StationsDao? = null
    private var stations: MutableList<Stations>? = null
    private var stationsLigne = mutableListOf<Stations>()
    private lateinit var stationsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stations_details, container, false)

        stationsRecyclerView = view.findViewById(R.id.stations_recyclerview)
        stationsRecyclerView.layoutManager = LinearLayoutManager(activity)

//        val database =
//            Room.databaseBuilder(requireContext(), AppDatabase::class.java, "globalDatabase")
//                .build()

        stationsDao = daoSta(requireContext()) // Récupère la database des stations

        // Recupère les infos de la ligne du fragment/activity parent
        val lineFromParent = arguments?.getSerializable("line") as Line

        synchroServerStations(view, lineFromParent.code)

        return view
    }

    override fun onResume() {
        super.onResume()

        stationsRecyclerView.adapter = StationsAdapter(stationsLigne, requireView())
    }

    // Synchro de la liste des stations --> Par la BDD au lieu de l'API
    private fun synchroServerStations(view: View, code: String) {

//        val service = retrofit().create(StationsService::class.java) // Fonction retrofit d'ActivityUtils
//        runBlocking {
//            stationsDao?.deleteStations() // Supprime les anciennes stations
//            val result = service.getStationsService("metros", code) // Obtient les stations de la ligne correspondante
//            var id = 1 // Pour toujours avoir le premier id à 1
//            result.result.stations.map {
//                val station = Stations(id, it.name, it.slug, code, false)
//                stationsDao?.addStation(station) // Ajoute la station dans la bdd
//                id += 1
//            }
//            stations = stationsDao?.getStations() // Recupère la liste des stations depuis la bdd
//            stationsRecyclerView.adapter = StationsAdapter(stations ?: mutableListOf(), view)
//        }
        runBlocking {
            stations = stationsDao?.getStations()
            // Obtient les stations de la ligne spécifiée uniquement
            stations?.map {
                if (it.line == code) {
                    stationsLigne.add(it)
                }
            }
            stationsRecyclerView.adapter = StationsAdapter(stationsLigne, view)
        }
    }
}