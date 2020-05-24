package app.epf.ratp_eb_pf.ui.listeLines

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
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.ui.listeLines.details.DetailsLineActivity
import kotlinx.android.synthetic.main.card_lines_view.view.*
import kotlinx.android.synthetic.main.fragment_favoris_lines.view.*
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InputStream

// Adapter des lines (pour ajout dans recyclerView)

class LinesAdapter(private val linesList: MutableList<Line>, private val viewFragment: View) :  // Pour récuperer la view du fragment contenant l'adapter
    RecyclerView.Adapter<LinesAdapter.LinesViewHolder>() {

    private var listLinesBdd: MutableList<Line>? = null
    private var lineDaoSaved: LineDao? = null
    private lateinit var context: Context // Context du fragment contenant l'adapter
    private var toastMessage: Toast? = null // Pour réinitialiser les messages toast quand plusieurs apparaissent en même temps

    class LinesViewHolder(val linesView: View) : RecyclerView.ViewHolder(linesView) {
        fun bind(post: Line) {
            linesView.name_line.text = post.name
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinesViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.card_lines_view, parent, false)

        context = parent.context

        // Bdd contenant les données sauvegardées (favoris)
        val databaseSaved = Room.databaseBuilder(context, AppDatabase::class.java, "savedDatabase")
            .build()

        lineDaoSaved = databaseSaved.getLineDao()

        runBlocking {
            listLinesBdd = lineDaoSaved?.getLines() // Récupère les lines favorites
        }

        return LinesViewHolder(view)
    }

    override fun getItemCount(): Int = linesList.size // Taille de l'adapter


    override fun onBindViewHolder(holder: LinesViewHolder, position: Int) {
        val view = holder.linesView
        var favoris = false

        val line = linesList[position] // Position de la line dans la recyclerView

       // view.name_line.text = line.name
        holder.bind(linesList[position])

        // Permet d'acceder au package "assets" avec les logos des lignes
        var ims: InputStream? = null
        try {
            ims = view.context.assets.open("metroLines/M${line.code}genRVB.png")
            val d = Drawable.createFromStream(ims, null)
            view.logo_line.setImageDrawable(d)
        } catch (ex: IOException) {
            //file does not exist --> logo par défaut
            view.logo_line.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp)
        } finally {
            ims?.close()
        }

        // En cas de click sur la cardview d'une line --> affiche activité correspondante (détails de la line)
        view.setOnClickListener { itView ->
            val intent = Intent(itView.context, DetailsLineActivity::class.java)
            intent.putExtra("line", line)
            itView.context.startActivity(intent)
        }

        // Check si la line appartient déjà aux favoris (idRatp unique)
        listLinesBdd?.map { itList ->
            if (itList.idRatp == line.idRatp) {
                favoris = true
            }
        }

        // Si déjà favorite --> étoile pleine
        if (line.favoris || favoris) {
            view.fab_favLine.setImageResource(R.drawable.ic_star_black_24dp)
            favoris = true
            // Sinon étoile vide
        } else if (!line.favoris || !favoris) {
            view.fab_favLine.setImageResource(R.drawable.ic_star_border_black_24dp)
            favoris = false

        }

        // En cas de clique sur le bouton favoris
        view.fab_favLine.setOnClickListener {
            toastMessage?.cancel() // Annule le précédent toast

            // Si pas encore dans les favoris
            if (!favoris) {
                val row = listLinesBdd?.size
                // incrémente l'id manuellement (1 si pas de favoris, sinon part du dernier existant)
                val id =
                    if (!listLinesBdd.isNullOrEmpty()) listLinesBdd!![row!! - 1].id + 1 else 1

                // Ajoute la line aux favoris
                runBlocking {
                    line.favoris = true
                    val lineS =
                        Line(id, line.code, line.name, line.directions, line.idRatp, line.favoris)
                    lineDaoSaved?.addLine(lineS)

                }
                favoris = true
                view.fab_favLine.setImageResource(R.drawable.ic_star_black_24dp)

                // Message pour dire que la line a bien été ajoutée
                toastMessage = Toast.makeText(
                    context,
                    "La ligne a bien été ajoutée aux favoris",
                    Toast.LENGTH_SHORT
                )
                toastMessage?.show()

                // Si déjà dans les favoris
            } else if (favoris) {

                // Supprime des favoris
                runBlocking {
                    lineDaoSaved?.deleteLine(line.idRatp)
                }

                // Trouve le nom du fragment contenant l'adapter
                val fragmentFavoris = try {
                    // https://stackoverflow.com/a/54829516/13289762
                    (context as MainActivity).supportFragmentManager.fragments.last()?.childFragmentManager?.fragments
                        ?.get(0)?.childFragmentManager?.fragments
                        ?.get(0)?.javaClass?.simpleName
                } catch (ex: Exception) {
                    ""
                }

                // si le fragment parent est celui des lines favorites
                if (fragmentFavoris == "FavorisLinesFragment") {
                    linesList.remove(line) // Supprime de la recyclerView

                    // Update la recyclerView
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, itemCount)

                    // Si plus aucun favoris --> affiche l'image "Aucune line favorite"
                    if (linesList.isNullOrEmpty()) {
                        viewFragment.layoutNoSavedLine.visibility = View.VISIBLE
                    }
                }

                favoris = false
                view.fab_favLine.setImageResource(R.drawable.ic_star_border_black_24dp)

                // Message pour dire que la line a bien été supprimée
                toastMessage = Toast.makeText(
                    context,
                    "La ligne a bien été supprimée des favoris",
                    Toast.LENGTH_SHORT
                )
                toastMessage?.show()
            }
            // Récupère la nouvelle liste des favoris de la bdd
            runBlocking {
                listLinesBdd = lineDaoSaved?.getLines()
            }

        }
    }
}