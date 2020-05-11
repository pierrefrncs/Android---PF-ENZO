package app.epf.ratp_eb_pf.ui.detailLine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.model.Stations
import kotlinx.android.synthetic.main.card_line_station.view.*

class StationsAdapter(private val stationsList: MutableList<Stations>) :
    RecyclerView.Adapter<StationsAdapter.StationsViewHolder>() {

    class StationsViewHolder(val stationsView: View) : RecyclerView.ViewHolder(stationsView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationsViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.card_line_station, parent, false)

        return StationsViewHolder(view)
    }

    override fun getItemCount(): Int = stationsList.size


    override fun onBindViewHolder(holder: StationsViewHolder, position: Int) {

        val view = holder.stationsView
        val station = stationsList[position]

        view.name_stations.text = station.name
    }
}