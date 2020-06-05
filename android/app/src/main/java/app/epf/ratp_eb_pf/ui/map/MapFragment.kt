package app.epf.ratp_eb_pf.ui.map

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import app.epf.ratp_eb_pf.R

class MapFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //        val map = view.findViewById<PhotoView>(R.id.map)
//
//        map.setOnViewDragListener { _, _ ->
//            map.scrollX = 0
//        }

        return inflater.inflate(R.layout.fragment_map, container, false)
    }
}
