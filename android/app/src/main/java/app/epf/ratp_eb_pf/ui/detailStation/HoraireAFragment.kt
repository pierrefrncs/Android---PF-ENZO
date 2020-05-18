package app.epf.ratp_eb_pf.ui.detailStation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.daoLi
import app.epf.ratp_eb_pf.daoSch
import app.epf.ratp_eb_pf.data.SchedulesDao
import app.epf.ratp_eb_pf.model.Schedules
import app.epf.ratp_eb_pf.model.Stations
import app.epf.ratp_eb_pf.retrofit
import app.epf.ratp_eb_pf.service.SchedulesService
import kotlinx.coroutines.runBlocking

class HoraireAFragment: Fragment() {

    private var scheduleDao: SchedulesDao? =null
    private var horaires: MutableList<Schedules>? = null
    private lateinit var horairesRecyclerView: RecyclerView
    private var stationFromParent:Stations? = null

    private val type = "metros"
    private val way = "A"

    override fun onCreateView(inflater: LayoutInflater,
                          container: ViewGroup?,
                          savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_horaire_station, container, false)
        stationFromParent = arguments?.getSerializable("station") as Stations

        scheduleDao = daoSch(requireContext())

        horairesRecyclerView = view.findViewById(R.id.horaires_recyclerview)
        horairesRecyclerView.layoutManager = LinearLayoutManager(activity)

        synchroStationData(view)

        return view
    }

    private fun synchroStationData(view: View) {
        val serviceSchedules = retrofit().create(SchedulesService::class.java)
        runBlocking {
            scheduleDao?.deleteSchedules()
            val result = serviceSchedules.getScheduleService(type, stationFromParent!!.line,stationFromParent!!.slug,way)
            var id = 1
            result.result.schedules.map {

                val schedules = Schedules(id, it.message, it.destination)
                scheduleDao?.addSchedules(schedules)
                id += 1
            }
            horaires = scheduleDao?.getSchedules()
            horairesRecyclerView.adapter = HorairesListAdapter(horaires ?: mutableListOf(), view)
        }
    }
}