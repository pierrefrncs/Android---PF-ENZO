package app.epf.ratp_eb_pf.ui.listeLines

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.MergeAdapter
import androidx.recyclerview.widget.RecyclerView
import app.epf.ratp_eb_pf.*
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.data.SchedulesDao
import app.epf.ratp_eb_pf.data.StationsDao
import app.epf.ratp_eb_pf.data.TrafficDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.model.Stations
import app.epf.ratp_eb_pf.model.Traffic
import app.epf.ratp_eb_pf.ui.detailStation.StationDetailsActivity
import app.epf.ratp_eb_pf.ui.listeLines.details.StationsAdapter
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.jakewharton.rxbinding2.widget.textChanges
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_liste_lines.*
import kotlinx.android.synthetic.main.fragment_liste_lines.view.*
import kotlinx.coroutines.runBlocking
import java.text.Normalizer
import java.util.concurrent.TimeUnit

// Fragment d'accueil contenant la liste des lines

class ListLinesAccueil : Fragment() {

    private var traffic: MutableList<Traffic>? = null
    private var trafficDao: TrafficDao? = null
    private val regexUnaccent = "\\p{InCombiningDiacriticalMarks}+".toRegex()

    private lateinit var listLinesViewModel: ListLinesViewModel
    private var stationsDao: StationsDao? = null
    private var lineDao: LineDao? = null
    private var schedulesDao: SchedulesDao? = null
    private lateinit var globalRecyclerView: RecyclerView
    private var lines: MutableList<Line>? = null
    private var stations: MutableList<Stations>? = null

    private val filteredPostsLi: MutableList<Line> = mutableListOf()
    private val oldFilteredPostsLi: MutableList<Line> = mutableListOf()
    private var filteredPostsSta: MutableList<Stations> = mutableListOf()
    private var oldFilteredPostsSta: MutableList<Stations> = mutableListOf()

    // Adapter qui peut regrouper plusieurs adapters dans une même recyclerView
    private var mergeAdapter = MergeAdapter()
    private var linesAdapter: LinesAdapter? = null
    private var stationsAdapter: StationsAdapter? = null

    private val disposable = CompositeDisposable()

    private var mBundleRecyclerViewState: Bundle? = null
    private var mListState: Parcelable? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        listLinesViewModel =
            ViewModelProvider(this).get(ListLinesViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_liste_lines, container, false)

        globalRecyclerView = view.findViewById(R.id.lines_recyclerview)
        val rechercherList = view.findViewById<EditText>(R.id.rechercherList)
        val qrCode = view.findViewById<ImageView>(R.id.qr_code)
        globalRecyclerView.layoutManager = LinearLayoutManager(activity)


        // Si click sur le bouton qrCode --> affiche la caméra
        qrCode.setOnClickListener {
            IntentIntegrator.forSupportFragment(this@ListLinesAccueil)
                .addExtra("PROMPT_MESSAGE", "Encadrez un QR code avec le viseur pour le balayer")
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .setBeepEnabled(false)
                .initiateScan()
        }

        stationsDao = daoSta(requireContext())
        lineDao = daoLi(requireContext())
        schedulesDao = daoSch(requireContext())
        trafficDao = daoTraf(requireContext())

        runBlocking {
            lines = lineDao?.getLines()
            stations = stationsDao?.getStations()
            traffic = trafficDao?.getTraffic()
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
                        if (filteredPostsLi.isNullOrEmpty() && filteredPostsSta.isNullOrEmpty()) {
                            view.layoutNoFoundLine.visibility = View.VISIBLE
                        } else {
                            view.layoutNoFoundLine.visibility = View.GONE
                        }
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
                        if (filteredPostsLi.isNullOrEmpty() && filteredPostsSta.isNullOrEmpty()) {
                            view.layoutNoFoundLine.visibility = View.VISIBLE
                        } else {
                            view.layoutNoFoundLine.visibility = View.GONE
                        }
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

        // Pour récupèrer la position de la recyclerView
        if (mBundleRecyclerViewState != null) {
            mListState = mBundleRecyclerViewState!!.getParcelable("keyR")
            globalRecyclerView.layoutManager?.onRestoreInstanceState(mListState)
        }
    }

    override fun onPause() {
        super.onPause()

        mBundleRecyclerViewState = Bundle()

        // Pour enregistrer la position de la recyclerView
        mListState = globalRecyclerView.layoutManager?.onSaveInstanceState()
        mBundleRecyclerViewState!!.putParcelable("keyR", mListState)
    }

    // Synchro de la liste des lines
    private fun synchroServerAllLines(view: View) {
        runBlocking {
            lines = lineDao?.getLines() // Recupère la liste des lines depuis la bdd
        }

        // Définit le premier adapter du mergeAdapter et ajoute à la recyclerView
        lines?.let { oldFilteredPostsLi.addAll(it) }
        linesAdapter = LinesAdapter(oldFilteredPostsLi, traffic!!, view)
        mergeAdapter.addAdapter(linesAdapter!!)
        globalRecyclerView.adapter = mergeAdapter
    }

    // Synchro de la liste des lines
    private fun synchroServerAllStations(view: View) {
        /*runBlocking {
            stations = stationsDao?.getStations() // Recupère la liste des stations depuis la bdd
        }*/

        // Définit le second adapter du mergeAdapter (ajoute pas : affiche uniquement lines)
        stations?.let { oldFilteredPostsSta.addAll(it) }
        stationsAdapter = StationsAdapter(oldFilteredPostsSta, view)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            val result: IntentResult? =
                IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            if (result != null) {
                runBlocking {
                    val station = stationsDao?.getStation(result.contents)
                    if (station != null) {
                        val intent = Intent(activity, StationDetailsActivity::class.java)
                        intent.putExtra("station", station)
                        activity?.startActivity(intent)
                    } else {
                        Toast.makeText(
                            context,
                            "Le QR code est invalide",
                            Toast.LENGTH_SHORT
                        ).show()
                        qr_code.performClick()
                    }
                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // Pour ignorer les accents dans l'input
    private fun CharSequence.unAccent(): String {
        val temp = Normalizer.normalize(this, Normalizer.Form.NFD)
        return regexUnaccent.replace(temp, "")
    }
}
