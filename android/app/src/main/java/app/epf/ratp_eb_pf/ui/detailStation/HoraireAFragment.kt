package app.epf.ratp_eb_pf.ui.detailStation

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.size
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.model.Schedules
import app.epf.ratp_eb_pf.model.Stations
import app.epf.ratp_eb_pf.retrofit
import app.epf.ratp_eb_pf.service.SchedulesService
import kotlinx.coroutines.runBlocking

class HoraireAFragment : Fragment(), StationDetailsActivity.RefreshPage {

    private var horaires: MutableList<Schedules> = mutableListOf()
    private lateinit var horairesRecyclerView: RecyclerView
    private var stationFromParent: Stations? = null

    private val type = "metros"
    private val way = "A"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_horaire_station, container, false)
        stationFromParent = arguments?.getSerializable("station") as Stations

        horairesRecyclerView = view.findViewById(R.id.horaires_recyclerview)
        val itemsSwipeToRefresh = view.findViewById<SwipeRefreshLayout>(R.id.itemsswipetorefresh)
        horairesRecyclerView.layoutManager = LinearLayoutManager(activity)

        synchroStationDataA()

        itemsSwipeToRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )
        itemsSwipeToRefresh.setColorSchemeColors(Color.WHITE)

        // Actualise les 2 fragments avec le swipeToRefresh
        itemsSwipeToRefresh.setOnRefreshListener {
            val viewpager = activity?.findViewById<ViewPager>(R.id.fragment_pager_horaires)
            synchroStationDataA()
            if (viewpager?.size == 2) {
                val horaireBFragment =
                    viewpager.adapter?.instantiateItem(viewpager, 1) as HoraireBFragment
                horaireBFragment.refreshPage()
            }
            itemsSwipeToRefresh.isRefreshing = false
        }

        return view
    }

    private fun synchroStationDataA() {
        val serviceSchedules = retrofit().create(SchedulesService::class.java)
        horaires.clear()
        runBlocking {
            val result = serviceSchedules.getScheduleService(
                type,
                stationFromParent!!.line,
                stationFromParent!!.slug,
                way
            )
            var id = 1
            result.result.schedules.map {
                if (it.message == "Train a l'approche") {
                    it.message = "Train à l'approche"
                } else if (it.message == "Train a quai") {
                    it.message = "Train à quai"
                }
                val schedules = Schedules(id, it.message, it.destination)
                horaires.add(schedules)
                id += 1
            }
            horairesRecyclerView.adapter = HorairesAdapter(horaires)
        }
    }

    override fun refreshPage() {
        view?.let { synchroStationDataA() }
    }
}