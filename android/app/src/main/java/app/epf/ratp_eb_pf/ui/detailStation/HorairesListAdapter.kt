package app.epf.ratp_eb_pf.ui.detailStation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.data.AppDatabase
import app.epf.ratp_eb_pf.data.SchedulesDao
import app.epf.ratp_eb_pf.model.Schedules
import kotlinx.android.synthetic.main.card_horaire_station.view.*
import kotlinx.coroutines.runBlocking

class HorairesListAdapter (
    private val schedulesList: MutableList<Schedules>
) : RecyclerView.Adapter<HorairesListAdapter.SchedulesViewHolder>() {

    private var listHorairesBDD: MutableList<Schedules>? = null
    private var schedulesDaoBDD: SchedulesDao? = null
    private lateinit var context: Context // Context du fragment contenant l'adapter


    class SchedulesViewHolder(val schedulesView: View) : RecyclerView.ViewHolder(schedulesView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchedulesViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.card_horaire_station, parent, false)

        context = parent.context

        // Bdd contenant les données sauvegardées
        val databaseSaved = Room.databaseBuilder(context, AppDatabase::class.java, "savedDatabase")
            .build()

        schedulesDaoBDD = databaseSaved.getSchedulesDao()

        runBlocking {
            listHorairesBDD = schedulesDaoBDD?.getSchedules() // Récupère les horaires de la destination
        }

        return SchedulesViewHolder(view)
    }

    override fun getItemCount(): Int = schedulesList.size

    override fun onBindViewHolder(holder: HorairesListAdapter.SchedulesViewHolder, position: Int) {
        val view = holder.schedulesView
        val schedules = schedulesList[position]

        view.horaire_direction.text = schedules.destination
        view.horaire_message.text = schedules.message
    }
}