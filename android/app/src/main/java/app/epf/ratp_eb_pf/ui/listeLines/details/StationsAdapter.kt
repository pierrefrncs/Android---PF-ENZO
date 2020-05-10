package app.epf.ratp_eb_pf.ui.listeLines.details

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.epf.ratp_eb_pf.MainActivity
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.data.AppDatabase
import app.epf.ratp_eb_pf.data.StationsDao
import app.epf.ratp_eb_pf.model.Stations
import app.epf.ratp_eb_pf.ui.listeLines.details.stations.StationDetailsActivity
import kotlinx.android.synthetic.main.fragment_favoris_stations.view.*
import kotlinx.android.synthetic.main.stations_view.view.*
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InputStream

class StationsAdapter(
    private val stationsList: MutableList<Stations>,
    private val viewFragment: View
) :
    RecyclerView.Adapter<StationsAdapter.StationsViewHolder>() {

    private var listStationsBdd: MutableList<Stations>? = null
    private var stationDaoSaved: StationsDao? = null
    private lateinit var context: Context
    private var toastMessage: Toast? = null

    class StationsViewHolder(val stationsView: View) : RecyclerView.ViewHolder(stationsView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationsViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.stations_view, parent, false)

        context = parent.context

        val databaseSaved = Room.databaseBuilder(context, AppDatabase::class.java, "savedDatabase")
            .build()

        stationDaoSaved = databaseSaved.getStationsDao()

        runBlocking {
            listStationsBdd = stationDaoSaved?.getStations()
        }

        return StationsViewHolder(view)
    }

    override fun getItemCount(): Int = stationsList.size


    override fun onBindViewHolder(holder: StationsViewHolder, position: Int) {
        val view = holder.stationsView
        var favoris = false

        val station = stationsList[position]

        view.name_stations.text = station.name

        val fragmentFavoris = try {
            // https://stackoverflow.com/a/54829516/13289762
            (context as MainActivity).supportFragmentManager.fragments.last()?.childFragmentManager?.fragments
                ?.get(0)?.childFragmentManager?.fragments
                ?.get(1)?.javaClass?.simpleName
        } catch (ex: Exception) {
            ""
        }

        if (fragmentFavoris == "FavorisStationsFragment") {
            view.logo_ligneStation.visibility = View.VISIBLE

            var ims: InputStream? = null
            try {
                ims = view.context.assets.open("metroLines/M${station.line}genRVB.png")
                val d = Drawable.createFromStream(ims, null)
                view.logo_ligneStation.setImageDrawable(d)
            } catch (ex: IOException) {
                //file does not exist
                view.logo_ligneStation.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp)
            } finally {
                ims?.close()
            }

        }

        view.setOnClickListener { itView ->
            val intent = Intent(itView.context, StationDetailsActivity::class.java)
            intent.putExtra("station", station)
            itView.context.startActivity(intent)
        }


        listStationsBdd?.map { itList ->
            if (itList.uuid == station.uuid) {
                favoris = true
            }
        }

        if (station.favoris || favoris) {
            view.fab_favStation.setImageResource(R.drawable.ic_star_black_24dp)
            favoris = true

        } else if (!station.favoris || !favoris) {
            view.fab_favStation.setImageResource(R.drawable.ic_star_border_black_24dp)
            favoris = false

        }


        view.fab_favStation.setOnClickListener {
            toastMessage?.cancel()

            if (!favoris) {
                val row = listStationsBdd?.size
                val id =
                    if (!listStationsBdd.isNullOrEmpty()) listStationsBdd!![row!! - 1].id + 1 else 1

                runBlocking {
                    station.favoris = true
                    val stationS =
                        Stations(id, station.name, station.slug, station.line, station.favoris)
                    stationDaoSaved?.addStation(stationS)

                }

                favoris = true
                view.fab_favStation.setImageResource(R.drawable.ic_star_black_24dp)
                toastMessage = Toast.makeText(
                    context,
                    "La station a bien été ajoutée aux favoris",
                    Toast.LENGTH_SHORT
                )
                toastMessage?.show()

            } else if (favoris) {

                runBlocking {
                    stationDaoSaved?.deleteStation(station.uuid)
                }

                if (fragmentFavoris == "FavorisStationsFragment") {
                    stationsList.remove(station)

                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, itemCount)

                    if (stationsList.isNullOrEmpty()) {
                        viewFragment.layoutNoSavedStation.visibility = View.VISIBLE
                    }
                }

                favoris = false

                view.fab_favStation.setImageResource(R.drawable.ic_star_border_black_24dp)
                toastMessage = Toast.makeText(
                    context,
                    "La station a bien été supprimée des favoris",
                    Toast.LENGTH_SHORT
                )
                toastMessage?.show()
            }
            runBlocking {
                listStationsBdd = stationDaoSaved?.getStations()
            }

        }
    }
}