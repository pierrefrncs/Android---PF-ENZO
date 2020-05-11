package app.epf.ratp_eb_pf.ui.detailLine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.retrofit
import app.epf.ratp_eb_pf.service.TrafficService
import kotlinx.android.synthetic.main.fragment_line_traffic.view.*
import kotlinx.coroutines.runBlocking

class TrafficDetailsFragment : Fragment() {

//    private var trafficsDao: TrafficDao? = null
//    private var traffics: MutableList<Traffic>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_line_traffic, container, false)

   //     trafficsDao = daoTra(requireContext())

        val side = arguments?.getSerializable("line") as Line

        synchroServerTraffics(view, side.code)

        return view
    }


    private fun synchroServerTraffics(view: View, code: String) {
        val service = retrofit().create(TrafficService::class.java)
        runBlocking {
            //           trafficsDao?.deleteTraffics()
            val result = service.getTrafficService("metros", code)
            val topo = result.result

//                val traffic = Traffic(0, topo.line, topo.slug, topo.title, topo.message)
            //               trafficsDao?.addTraffic(traffic)

            view.etatTraffic.text = topo.title
            view.messageTraffic.text = topo.message

            //          traffics = trafficsDao?.getTraffics()
        }
    }
}