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
import app.epf.ratp_eb_pf.ui.detailLine.DetailsLineActivity
import kotlinx.android.synthetic.main.fragment_favoris.view.*
import kotlinx.android.synthetic.main.card_lines.view.*
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.io.InputStream

class LinesAdapter(private val linesList: MutableList<Line>, private val viewFragment: View) :
    RecyclerView.Adapter<LinesAdapter.LinesViewHolder>() {

    private var listLinesBdd: MutableList<Line>? = null
    private var lineDaoSaved: LineDao? = null
    private lateinit var context: Context

    class LinesViewHolder(val linesView: View) : RecyclerView.ViewHolder(linesView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinesViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        val view: View = layoutInflater.inflate(R.layout.card_lines, parent, false)

        context = parent.context

        val databaseSaved = Room.databaseBuilder(context, AppDatabase::class.java, "savedDatabase")
            .build()

        lineDaoSaved = databaseSaved.getLineDao()

        runBlocking {
            listLinesBdd = lineDaoSaved?.getLines()
        }

        return LinesViewHolder(view)
    }

    override fun getItemCount(): Int = linesList.size


    override fun onBindViewHolder(holder: LinesViewHolder, position: Int) {
        val view = holder.linesView
        var favoris = false

        val line = linesList[position]

        view.name_line.text = line.name
        var ims: InputStream? = null
        try {
            ims = view.context.assets.open("metroLines/M${line.code}genRVB.png")
            val d = Drawable.createFromStream(ims, null)
            view.logo_line.setImageDrawable(d)
        } catch (ex: IOException) {
            //file does not exist
            view.logo_line.setImageResource(R.drawable.ic_format_list_bulleted_black_24dp)
        } finally {
            ims?.close()
        }

        view.setOnClickListener { itView ->
            val intent = Intent(itView.context, DetailsLineActivity::class.java)
            intent.putExtra("line", line)
            itView.context.startActivity(intent)
        }


        listLinesBdd?.map { itList ->
            if (itList.idRatp == line.idRatp) {
                favoris = true
            }
        }

        if (line.favoris || favoris) {
            view.fab_favLine.setImageResource(R.drawable.ic_star_black_24dp)
            favoris = true

        } else if (!line.favoris || !favoris) {
            view.fab_favLine.setImageResource(R.drawable.ic_star_border_black_24dp)
            favoris = false

        }


        view.fab_favLine.setOnClickListener {
            if (!favoris) {

                val row = listLinesBdd?.size
                val id =
                    if (!listLinesBdd.isNullOrEmpty()) listLinesBdd!![row!! - 1].id + 1 else 1

                runBlocking {
                    line.favoris = true
                    val lineS =
                        Line(id, line.code, line.name, line.directions, line.idRatp, line.favoris)
                    lineDaoSaved?.addLine(lineS)

                }

                favoris = true
                view.fab_favLine.setImageResource(R.drawable.ic_star_black_24dp)
                Toast.makeText(
                    context,
                    "La ligne a bien été ajoutée aux favoris",
                    Toast.LENGTH_SHORT
                ).show()

            }
            else if (favoris) {

                runBlocking {
                    lineDaoSaved?.deleteLine(line.idRatp)
                }

                // https://stackoverflow.com/a/54829516/13289762
                val fragmentFavoris =
                    (context as MainActivity).supportFragmentManager.fragments.last()?.childFragmentManager?.fragments
                        ?.get(0)?.javaClass?.simpleName

                if (fragmentFavoris == "FavorisFragment") {
                    linesList.remove(line)

                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, itemCount)

                    if (linesList.isNullOrEmpty()) {
                        viewFragment.layoutNoSavedLine.visibility = View.VISIBLE
                    }
                }

                favoris = false

                view.fab_favLine.setImageResource(R.drawable.ic_star_border_black_24dp)
                Toast.makeText(
                    context,
                    "La ligne a bien été supprimée des favoris",
                    Toast.LENGTH_SHORT
                ).show()
            }
            runBlocking {
                listLinesBdd = lineDaoSaved?.getLines()
            }

        }
    }
}