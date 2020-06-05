package app.epf.ratp_eb_pf.ui.listeLines.details

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.daoSta
import app.epf.ratp_eb_pf.data.StationsDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.model.Stations
import kotlinx.coroutines.runBlocking

// Sous-fragment (de DetailsLineActivity) qui contient la liste des stations d'une ligne

class StationsListFragment : Fragment() {

    private var stationsDao: StationsDao? = null
    private var stations: MutableList<Stations>? = null
    private var stationsLigne = mutableListOf<Stations>()
    private lateinit var stationsRecyclerView: RecyclerView

    private var mBundleRecyclerViewState: Bundle? = null
    private var mListState: Parcelable? = null

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

        // Pour récupèrer la position de la recyclerView
        if (mBundleRecyclerViewState != null) {
            mListState = mBundleRecyclerViewState!!.getParcelable("keyR")
            stationsRecyclerView.layoutManager?.onRestoreInstanceState(mListState)
        }
    }

    override fun onPause() {
        super.onPause()

        mBundleRecyclerViewState = Bundle()

        // Pour enregistrer la position de la recyclerView
        mListState = stationsRecyclerView.layoutManager?.onSaveInstanceState()
        mBundleRecyclerViewState!!.putParcelable("keyR", mListState)
    }

    // Synchro de la liste des stations --> Par la BDD au lieu de l'API
    private fun synchroServerStations(view: View, code: String) {
        runBlocking {
            stations = stationsDao?.getStations()
            // Obtient les stations de la ligne spécifiée uniquement
            run loop@{
                var top = ""
                stations?.map {
                    if (it.line == code) {
                        stationsLigne.add(it)
                        top = it.line
                    }
                    if (top == code && it.line != code) {
                        return@loop
                    }
                }
            }
            stationsRecyclerView.adapter = StationsAdapter(stationsLigne, view)
        }
    }
}