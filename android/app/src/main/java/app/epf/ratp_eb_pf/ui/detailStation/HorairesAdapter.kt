package app.epf.ratp_eb_pf.ui.detailStation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.model.Schedules
import kotlinx.android.synthetic.main.card_horaire_station.view.*

// Adapter des horaires (pour ajout dans recyclerview)

class HorairesAdapter(
    private val schedulesList: MutableList<Schedules>
) : RecyclerView.Adapter<HorairesAdapter.SchedulesViewHolder>() {

    class SchedulesViewHolder(val schedulesView: View) : RecyclerView.ViewHolder(schedulesView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchedulesViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.card_horaire_station, parent, false)

        return SchedulesViewHolder(view)
    }

    override fun getItemCount(): Int = schedulesList.size

    override fun onBindViewHolder(holder: SchedulesViewHolder, position: Int) {
        val view = holder.schedulesView
        val schedules = schedulesList[position]

        view.horaire_direction.text = schedules.destination
        view.horaire_message.text = schedules.message
    }
}