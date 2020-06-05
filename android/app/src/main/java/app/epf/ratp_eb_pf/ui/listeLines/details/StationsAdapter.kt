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
import app.epf.ratp_eb_pf.ui.detailStation.StationDetailsActivity
import app.epf.ratp_eb_pf.ui.favoris.FavorisFragment
import app.epf.ratp_eb_pf.ui.listeLines.ListLinesAccueil
import kotlinx.android.synthetic.main.card_stations_view.view.*
import kotlinx.android.synthetic.main.fragment_favoris_stations.view.*
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InputStream

// Adapter des stations (pour ajout dans recyclerView)

class StationsAdapter(
    private val stationsList: MutableList<Stations>,
    private val viewFragment: View // Pour récuperer la view du fragment contenant l'adapter
) : RecyclerView.Adapter<StationsAdapter.StationsViewHolder>() {

    private var listStationsBdd: MutableList<Stations>? = null
    private var fragmentName: String = ""
    private val favorisFragmentName = FavorisFragment().javaClass.simpleName
    private val accueilFragmentName = ListLinesAccueil().javaClass.simpleName
    private var stationDaoSaved: StationsDao? = null
    private lateinit var context: Context // Context du fragment contenant l'adapter
    private var toastMessage: Toast? =
        null // Pour réinitialiser les messages toast quand plusieurs apparaissent en même temps

    class StationsViewHolder(val stationsView: View) : RecyclerView.ViewHolder(stationsView) {
        fun bind(post: Stations) {
            stationsView.name_stations.text = post.name
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationsViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.card_stations_view, parent, false)

        context = parent.context

        // Trouve le nom du fragment contenant l'adapter dans la navigation principale
        fragmentName = try {
            // https://stackoverflow.com/a/54829516/13289762
            (context as MainActivity).supportFragmentManager.fragments[0].childFragmentManager.fragments[0].javaClass.simpleName
        } catch (ex: Exception) {
            ""
        }

        // Bdd contenant les données sauvegardées (favoris)
        val databaseSaved = Room.databaseBuilder(context, AppDatabase::class.java, "savedDatabase")
            .build()

        stationDaoSaved = databaseSaved.getStationsDao()

        runBlocking {
            listStationsBdd = stationDaoSaved?.getStations() // Récupère les stations favorites
        }

        return StationsViewHolder(view)
    }

    override fun getItemCount(): Int = stationsList.size // Taille de l'adapter


    override fun onBindViewHolder(holder: StationsViewHolder, position: Int) {
        val view = holder.stationsView
        var favoris = false

        val station = stationsList[position] // Position de la station dans la recyclerView

        holder.bind(stationsList[position])

        view.name_stations.text = station.name

        // Si ce fragment est celui des favoris : rajoute le logo de la ligne
        if (fragmentName == favorisFragmentName || fragmentName == accueilFragmentName) {
            view.logo_ligneStation.visibility = View.VISIBLE

            // Permet d'acceder au package "assets" avec les logos des lignes
            var ims: InputStream? = null
            try {
                ims = view.context.assets.open("metroLines/M${station.line}genRVB.png")
                val d = Drawable.createFromStream(ims, null)
                view.logo_ligneStation.setImageDrawable(d)
            } catch (ex: IOException) {
                //file does not exist --> logo par défaut
                view.logo_ligneStation.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp)
            } finally {
                ims?.close()
            }
        }

        // En cas de click sur la cardview d'une station --> affiche activité correspondante (détails de la station)
        view.setOnClickListener { itView ->
            val intent = Intent(itView.context, StationDetailsActivity::class.java)

            intent.putExtra("station", station)
            itView.context.startActivity(intent)
        }


        // Check si la station appartient déjà aux favoris (uuid unique)
        listStationsBdd?.map { itList ->
            if (itList.uuid == station.uuid) {
                favoris = true
            }
        }

        // Si déjà favorite --> étoile pleine
        if (station.favoris || favoris) {
            view.fab_favStation.setImageResource(R.drawable.ic_star_black_24dp)
            favoris = true
            // Sinon étoile vide
        } else if (!station.favoris || !favoris) {
            view.fab_favStation.setImageResource(R.drawable.ic_star_border_black_24dp)
            favoris = false

        }

        // En cas de clique sur le bouton favoris
        view.fab_favStation.setOnClickListener {
            toastMessage?.cancel() // Annule le précédent toast

            // Si pas encore dans les favoris
            if (!favoris) {
                val row = listStationsBdd?.size
                // incrémente l'id manuellement (1 si pas de favoris, sinon part du dernier existant)
                val id =
                    if (!listStationsBdd.isNullOrEmpty()) listStationsBdd!![row!! - 1].id + 1 else 1

                // Ajoute la station aux favoris
                runBlocking {
                    station.favoris = true
                    val stationS =
                        Stations(id, station.name, station.slug, station.line, station.favoris)
                    stationDaoSaved?.addStation(stationS)
                }
                favoris = true
                view.fab_favStation.setImageResource(R.drawable.ic_star_black_24dp)

                // Message pour dire que la station a bien été ajoutée
                toastMessage = Toast.makeText(
                    context,
                    "La station a bien été ajoutée aux favoris",
                    Toast.LENGTH_SHORT
                )
                toastMessage?.show()

                // Si déjà dans les favoris
            } else if (favoris) {

                // Supprime des favoris
                runBlocking {
                    stationDaoSaved?.deleteStation(station.uuid)
                }

                // si le fragment parent est celui des stations favorites
                if (fragmentName == favorisFragmentName) {
                    stationsList.remove(station) // Supprime de la recyclerView

                    // Update la recyclerView
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, itemCount)

                    // Si plus aucun favoris --> affiche l'image "Aucune station favorite"
                    if (stationsList.isNullOrEmpty()) {
                        viewFragment.layoutNoSavedStation.visibility = View.VISIBLE
                    }
                }

                favoris = false
                view.fab_favStation.setImageResource(R.drawable.ic_star_border_black_24dp)

                // Message pour dire que la station a bien été supprimée
                toastMessage = Toast.makeText(
                    context,
                    "La station a bien été supprimée des favoris",
                    Toast.LENGTH_SHORT
                )
                toastMessage?.show()
            }
            // Récupère la nouvelle liste des favoris de la bdd
            runBlocking {
                listStationsBdd = stationDaoSaved?.getStations()
            }

        }
    }
}