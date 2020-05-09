package app.epf.ratp_eb_pf.ui.favoris

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.epf.ratp_eb_pf.MainActivity
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.data.AppDatabase
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.ui.listeLines.LinesAdapter
import app.epf.ratp_eb_pf.ui.listeLines.ListLinesFragment
import kotlinx.android.synthetic.main.fragment_favoris.view.*
import kotlinx.coroutines.runBlocking

class FavorisFragment : Fragment() {

    private lateinit var favorisViewModel: FavorisViewModel
    private var lineDaoSaved: LineDao? = null
    private lateinit var linesRecyclerView: RecyclerView
    private var lines: MutableList<Line>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        favorisViewModel =
            ViewModelProviders.of(this).get(FavorisViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_favoris, container, false)

        linesRecyclerView = view.findViewById(R.id.savedLines_recyclerview)
        linesRecyclerView.layoutManager = LinearLayoutManager(activity)

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

        runBlocking {
            if (!lines.isNullOrEmpty()) {
                view.layoutNoSavedLine.visibility = View.GONE
            } else {
                view.layoutNoSavedLine.visibility = View.VISIBLE
            }
        }
        linesRecyclerView.adapter = LinesAdapter(lines ?: mutableListOf(), view)

        return view
    }

    override fun onResume() {
        super.onResume()

        linesRecyclerView.adapter = LinesAdapter(lines ?: mutableListOf(), requireView())
    }
}
