package app.epf.ratp_eb_pf.ui.listeLines

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.daoLi
import app.epf.ratp_eb_pf.daoSta
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.data.StationsDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.model.Stations
import app.epf.ratp_eb_pf.retrofit
import app.epf.ratp_eb_pf.service.LinesService
import app.epf.ratp_eb_pf.service.StationsService
import app.epf.ratp_eb_pf.ui.listeLines.details.StationsAdapter
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.runBlocking
import java.text.Normalizer
import java.util.concurrent.TimeUnit

// Fragment d'accueil contenant la liste des lines

class ListLinesAccueil : Fragment() {

    private val regexUnaccent = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    private lateinit var listLinesViewModel: ListLinesViewModel
    private var stationsDao: StationsDao? = null
    private var lineDao: LineDao? = null
    private lateinit var globalRecyclerView: RecyclerView
    private var lines: MutableList<Line>? = null
    private var stations: MutableList<Stations>? = null
    private var list: MutableList<String> = mutableListOf()

    private val filteredPostsLi: MutableList<Line> = mutableListOf()
    private val oldFilteredPostsLi: MutableList<Line> = mutableListOf()
    private var filteredPostsSta: MutableList<Stations> = mutableListOf()
    private var oldFilteredPostsSta: MutableList<Stations> = mutableListOf()

    private var mergeAdapter = MergeAdapter() // Adapter qui peut regrouper plusieurs adapters dans une même recyclerView
    private var linesAdapter: LinesAdapter? = null
    private var stationsAdapter: StationsAdapter? = null

    private val disposable = CompositeDisposable()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        listLinesViewModel =
            ViewModelProviders.of(this).get(ListLinesViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_liste_lines, container, false)

        globalRecyclerView = view.findViewById(R.id.lines_recyclerview)
        val rechercherList = view.findViewById<EditText>(R.id.rechercherList)
        val qrCode = view.findViewById<ImageView>(R.id.qr_code)
        globalRecyclerView.layoutManager = LinearLayoutManager(activity)


        // Si click sur le bouton qrCode --> affiche la caméra
        qrCode.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 100)
        }

        // Configure les BDD
//        val database =
//            Room.databaseBuilder(requireContext(), AppDatabase::class.java, "globalDatabase")
//                .build()

        stationsDao = daoSta(requireContext())
        lineDao = daoLi(requireContext())

        runBlocking {
            lines = lineDao?.getLines()
            stations = stationsDao?.getStations()
        }

        synchroServerAllLines(view)
        synchroServerAllStations(view)

        // Pour filtrer la liste des lines grâce à l'input rechercherList
        rechercherList
            .textChanges()
            .debounce(200, TimeUnit.MILLISECONDS)
            .subscribe {
                searchLines(it.toString())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val diffResult = DiffUtil.calculateDiff(
                            LinesDiffUtilCallback(oldFilteredPostsLi, filteredPostsLi)
                        )
                        oldFilteredPostsLi.clear()
                        oldFilteredPostsLi.addAll(filteredPostsLi)
                        linesAdapter?.let { it1 -> diffResult.dispatchUpdatesTo(it1) }
                    }.addTo(disposable)
            }.addTo(disposable)

        // Pour filtrer la liste des stations grâce à l'input rechercherList
        rechercherList
            .textChanges()
            .debounce(200, TimeUnit.MILLISECONDS)
            .subscribe {
                searchStations(it.toString())
                    .subscribeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val diffResult = DiffUtil.calculateDiff(
                            StationsDiffUtilCallback(oldFilteredPostsSta, filteredPostsSta)
                        )
                        val test = oldFilteredPostsSta.size
                        oldFilteredPostsSta.clear()
                        // Si la taille de la liste filtrée est la même que celle de base --> n'affiche pas (donc supprime) l'adapter des stations
                        if (stations?.size == test || stations?.size == filteredPostsSta.size) {
                            stationsAdapter?.let { it1 -> mergeAdapter.removeAdapter(it1) }
                        } else {
                            // Sinon l'affiche normalement et le rajoute (si inexistant)
                            oldFilteredPostsSta.addAll(filteredPostsSta)
                            stationsAdapter?.let { it1 -> diffResult.dispatchUpdatesTo(it1) }
                            mergeAdapter.addAdapter(stationsAdapter!!)
                        }
                    }.addTo(disposable)
            }.addTo(disposable)


        return view
    }

    override fun onResume() {
        super.onResume()

        globalRecyclerView.adapter = mergeAdapter
    }

    // Synchro de la liste des lines
    private fun synchroServerAllLines(view: View) {

        // si la BDD des lines et stations est vide
        if (lines.isNullOrEmpty() || stations.isNullOrEmpty()) {
            val service =
                retrofit().create(LinesService::class.java) // Fonction retrofit d'ActivityUtils
            runBlocking {
                lineDao?.deleteLines() // Supprime les anciennes lines
                val result =
                    service.getLinesService("metros") // Obtient les lines du type correspondant
                var id = 1 // Pour toujours avoir le premier id à 1
                result.result.metros.map {

                    val line = Line(id, it.code, it.name, it.directions, it.id.toInt(), false)
                    // Enlève les lines de metro inutiles
                    if (it.id != "79" && it.id != "455") {
                        list.add(it.code)
                        lineDao?.addLine(line)  // Ajoute la station dans la bdd
                        id += 1
                    }
                }
                lines = lineDao?.getLines() // Recupère la liste des lines depuis la bdd
            }
        }
        // Définit le premier adapter du mergeAdapter et ajoute à la recyclerView
        lines?.let { oldFilteredPostsLi.addAll(it) }
        linesAdapter = LinesAdapter(oldFilteredPostsLi, view)
        mergeAdapter.addAdapter(linesAdapter!!)
        globalRecyclerView.adapter = mergeAdapter
    }

    // Synchro de la liste des lines
    private fun synchroServerAllStations(view: View) {

        // si la BDD des stations est vide
        if (stations.isNullOrEmpty()) {
            runBlocking {
                stationsDao?.deleteStations() // Supprime les anciennes stations
            }
            var id = 1 // Pour toujours avoir le premier id à 1

            // Boucle sur toutes les lines
            for (code: String in list) {
                val service =
                    retrofit().create(StationsService::class.java) // Fonction retrofit d'ActivityUtils
                runBlocking {

                    // Obtient les stations de la line du type correspondant
                    val result = service.getStationsService("metros", code)

                    result.result.stations.map {

                        val station = Stations(id, it.name, it.slug, code, false)
                        stationsDao?.addStation(station)  // Ajoute la station dans la bdd
                        id += 1

                    }
                }
            }
            runBlocking {
                stations =
                    stationsDao?.getStations() // Recupère la liste des stations depuis la bdd
            }
        }
        // Définit le second adapter du mergeAdapter (ajoute pas : affiche uniquement lines)
        stations?.let { oldFilteredPostsSta.addAll(it) }
        stationsAdapter = StationsAdapter(oldFilteredPostsSta, view)
//        mergeAdapter.addAdapter(stationsAdapter!!)
//        globalRecyclerView.adapter = mergeAdapter
    }

    // Pour vérifier si la liste des lines contient ou non la recherche de l'input
    private fun searchLines(query: String): Completable = Completable.create {
        val wanted = lines?.filter { itLine ->
            itLine.name.unAccent().contains(query.unAccent(), true)
        }?.toList()

        filteredPostsLi.clear()
        if (!wanted.isNullOrEmpty()) {
            filteredPostsLi.addAll(wanted)
        }
        it.onComplete()
    }

    // Pour vérifier si la liste des stations contient ou non la recherche de l'input
    private fun searchStations(query: String): Completable = Completable.create {
        val wanted = stations?.filter { itSta ->
            itSta.name.unAccent().contains(query.unAccent(), true)
        }?.toList()

        filteredPostsSta.clear()
        if (!wanted.isNullOrEmpty()) {
            filteredPostsSta.addAll(wanted)
        }
        it.onComplete()
    }

    // Pour ignorer les accents dans l'input
    private fun CharSequence.unAccent(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return regexUnaccent.replace(temp, "")
    }

    // Pour cacher le keyboard d'un fragment
    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    //    private fun Activity.hideKeyboard() {
//        hideKeyboard(currentFocus ?: View(this))
//    }

    // Pour cacher le keyboard avec uniquement un context
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
