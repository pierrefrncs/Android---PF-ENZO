package app.epf.ratp_eb_pf.ui.listeLines.details

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.retrofit
import app.epf.ratp_eb_pf.service.TrafficService
import kotlinx.android.synthetic.main.fragment_traffic_details.view.*
import kotlinx.coroutines.runBlocking

// Sous-fragment (de DetailsLineActivity) qui contient l'état du traffic d'une ligne

class TrafficDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_traffic_details, container, false)

        // Recupère les infos de la ligne du fragment/activity parent
        val lineFromParent = arguments?.getSerializable("line") as Line

        val itemsSwipeToRefresh =
            view.findViewById<SwipeRefreshLayout>(R.id.itemsswipetorefreshTraffic)

        synchroServerTraffics(view, lineFromParent.code)

        itemsSwipeToRefresh.setProgressBackgroundColorSchemeColor(
            ContextCompat.getColor(requireContext(), R.color.colorPrimary)
        )
        itemsSwipeToRefresh.setColorSchemeColors(Color.WHITE)

        // Appel l'api lors du refresh de la page
        itemsSwipeToRefresh.setOnRefreshListener {
            synchroServerTraffics(view, lineFromParent.code)
            itemsSwipeToRefresh.isRefreshing = false
        }

        return view
    }


    // Synchro de l'état du traffic
    private fun synchroServerTraffics(view: View, code: String) {
        val service =
            retrofit().create(TrafficService::class.java) // Fonction retrofit d'ActivityUtils
        runBlocking {
            val result = service.getTrafficService("metros", code)
            val topo = result.result

            view.etatTraffic.text = topo.title
            view.messageTraffic.text = topo.message
        }
    }
}