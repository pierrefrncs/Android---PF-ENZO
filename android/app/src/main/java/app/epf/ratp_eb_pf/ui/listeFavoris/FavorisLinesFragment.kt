package app.epf.ratp_eb_pf.ui.listeFavoris

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.data.AppDatabase
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.ui.listeLinesMain.LinesAdapter
import kotlinx.android.synthetic.main.fragment_favoris_lines.view.*
import kotlinx.coroutines.runBlocking

// Sous-fragment des favoris pour les lines

class FavorisLinesFragment : Fragment() {

    private var lineDaoSaved: LineDao? = null
    private lateinit var linesRecyclerView: RecyclerView
    private var lines: MutableList<Line>? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_favoris_lines, container, false)

        linesRecyclerView = view.findViewById(R.id.savedLines_recyclerview)
        linesRecyclerView.layoutManager = LinearLayoutManager(activity)

        // En cas de click sur l'image "Aucune ligne favorite", envoie vers la liste des lignes
        view.noLinesImage.setOnClickListener {
            val navController = activity?.findNavController(R.id.nav_host_fragment)
            navController?.navigate(R.id.navigation_list_lignes)
        }

        val database =
            Room.databaseBuilder(requireContext(), AppDatabase::class.java, "savedDatabase")
                .build()

        lineDaoSaved = database.getLineDao()

        runBlocking {
            lines = lineDaoSaved?.getLines()
        }

        // Si aucune ligne favorite, affiche l'image "Aucune ligne favorite", sinon cachée
        runBlocking {
            if (!lines.isNullOrEmpty()) {
                view.layoutNoSavedLine.visibility = View.GONE
            } else {
                view.layoutNoSavedLine.visibility = View.VISIBLE
            }
        }

        // Ajoute l'adapter des lines (liste déroulante des lines favorites)
        linesRecyclerView.adapter = LinesAdapter(lines ?: mutableListOf(), view)

        return view
    }

    override fun onResume() {
        super.onResume()

        linesRecyclerView.adapter = LinesAdapter(lines ?: mutableListOf(), requireView())
    }
}