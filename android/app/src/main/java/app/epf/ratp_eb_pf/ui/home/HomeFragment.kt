package app.epf.ratp_eb_pf.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.service.LinesService
import app.epf.ratp_eb_pf.ui.dashboard.dao
import app.epf.ratp_eb_pf.ui.dashboard.retrofit
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.coroutines.runBlocking

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var lineDao: LineDao? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)

        root.buttonSearch.setOnClickListener {
            synchroServer()
        }

        lineDao =  dao(requireContext())

        return root
    }


    private fun synchroServer() {
        val service = retrofit().create(LinesService::class.java)
        runBlocking {
            lineDao?.deleteLines()
            val result = service.getUsers("metros", "1")
            result.result.stations.map {

                var line = Line(0, it.name, it.slug)
                lineDao?.addLine(line)

            }
            var lines = lineDao?.getLines()
            //       clients_recyclerview.adapter = ClientAdapter(lines ?: emptyList())
            Log.d("Lines", "$lines")
        }

    }
}
