package app.epf.ratp_eb_pf.ui.detailLine

import android.os.Bundle
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
import app.epf.ratp_eb_pf.retrofit
import app.epf.ratp_eb_pf.service.StationsService
import kotlinx.coroutines.runBlocking

class StationsDetailsFragment : Fragment() {

    private var stationsDao: StationsDao? = null
    private var stations: MutableList<Stations>? = null
    private lateinit var stationsRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_line_stations, container, false)

        stationsRecyclerView = view.findViewById(R.id.stations_recyclerview)
        stationsRecyclerView.layoutManager = LinearLayoutManager(activity)

        stationsDao = daoSta(requireContext())

        val side = arguments?.getSerializable("line") as Line

        synchroServerStations(side.code)

        return view
    }

    override fun onResume() {
        super.onResume()

        stationsRecyclerView.adapter =
            StationsAdapter(stations ?: mutableListOf())
    }

    private fun synchroServerStations(code: String) {
        val service = retrofit().create(StationsService::class.java)
        runBlocking {
            stationsDao?.deleteStations()
            val result = service.getStationsService("metros", code)
            result.result.stations.map {

                val station = Stations(0, it.name, it.slug)
                stationsDao?.addStation(station)
            }
            stations = stationsDao?.getStations()
            stationsRecyclerView.adapter =
                StationsAdapter(stations ?: mutableListOf())
        }
    }
}