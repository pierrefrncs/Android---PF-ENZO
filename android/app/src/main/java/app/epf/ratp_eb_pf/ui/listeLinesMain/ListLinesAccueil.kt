package app.epf.ratp_eb_pf.ui.listeLinesMain

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.ListAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.epf.ratp_eb_pf.*
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.data.StationsDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.service.LinesService
import app.epf.ratp_eb_pf.ui.detailLine.DetailsLineActivity
import app.epf.ratp_eb_pf.ui.detailStation.StationDetailsActivity
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_liste_lines.view.*
import kotlinx.coroutines.runBlocking


// Fragment d'accueil contenant la liste des lines

class ListLinesAccueil : Fragment() {

    private lateinit var listLinesViewModel: ListLinesViewModel
    private var stationsDao: StationsDao? = null
    private var lineDao: LineDao? = null
    private lateinit var linesRecyclerView: RecyclerView
    private var lines: MutableList<Line>? = null
    private var listArray = mutableListOf<String>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        listLinesViewModel =
            ViewModelProviders.of(this).get(ListLinesViewModel::class.java)
        val view = inflater.inflate(R.layout.fragment_liste_lines, container, false)

        linesRecyclerView = view.findViewById(R.id.lines_recyclerview)
        val rechercherList = view.findViewById<AutoCompleteTextView>(R.id.rechercherList)
        val qrCode = view.findViewById<ImageView>(R.id.qr_code)
        linesRecyclerView.layoutManager = LinearLayoutManager(activity)

        // Si click sur le bouton physique recherche --> envoie vers les détails de la line correspondante
        view.buttonSearch.setOnClickListener {
            if (rechercherList != null) {
                val intent = Intent(requireContext(), DetailsLineActivity::class.java)
                lines?.map {
                    if (rechercherList.text.toString() == it.name) {
                        intent.putExtra("line", it)
                        startActivity(intent)
                        return@setOnClickListener
                    }
                }
            }
        }

        // Si click sur le bouton recherche du clavier --> envoie vers les détails de la line correspondante
        view.rechercherList.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (rechercherList != null) {
                    val intent = Intent(requireContext(), DetailsLineActivity::class.java)
                    lines?.map {
                        if (rechercherList.text.toString() == it.name) {
                            intent.putExtra("line", it)
                            hideKeyboard()
                            startActivity(intent)
                        }
                    }
                }
            }
            false
        }

        // Si click sur le bouton qrCode --> affiche la caméra
        qrCode.setOnClickListener {
            IntentIntegrator.forSupportFragment(this)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .setBeepEnabled(false)
                .initiateScan()
        }

        // Configure les BDD
        stationsDao = daoSta(requireContext())
        lineDao = daoLi(requireContext())

        runBlocking {
            lines = lineDao?.getLines()

            lines?.map {
                listArray.add(it.name)
            }
            // Ajoute un adapter à l'input contenant la liste des lines
            val adapterMetro = IgnoreAccentsArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1, listArray.toTypedArray()
            )
            rechercherList.setAdapter(adapterMetro)

            clearFocusAutoTextView(rechercherList) // Si rien choisi parmi la liste --> vide l'input
        }

        // En cas de click sur l'input --> affiche la liste déroulante avec les lines
        rechercherList.setOnTouchListener { v, event ->
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> rechercherList.showDropDown()
            }
            v?.onTouchEvent(event) ?: true
        }

        synchroServerAllLines(view)

        return view
    }

    override fun onResume() {
        super.onResume()

        linesRecyclerView.adapter = LinesAdapter(lines ?: mutableListOf(), requireView())
    }

    // Synchro de la liste des lines
    private fun synchroServerAllLines(view: View) {
        if (lines.isNullOrEmpty()) {
            val service = retrofit().create(LinesService::class.java) // Fonction retrofit d'ActivityUtils
            runBlocking {
                lineDao?.deleteLines() // Supprime les anciennes lines
                val result = service.getLinesService("metros") // Obtient les lignes du type correspondant
                var id = 1 // Pour toujours avoir le premier id à 1
                result.result.metros.map {

                    val line = Line(id, it.code, it.name, it.directions, it.id.toInt(), false)
                    // Enlève les stations de metro inutiles
                    if (it.id != "79" && it.id != "455") {
                        lineDao?.addLine(line)  // Ajoute la station dans la bdd
                        id += 1
                    }
                }
                lines = lineDao?.getLines() // Recupère la liste des stations depuis la bdd
                linesRecyclerView.adapter = LinesAdapter(lines ?: mutableListOf(), view)
            }
        }
    }

    // Récup valeur du QR code et lande l'activité liée
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(activity, "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(activity, "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
                val intent = Intent(this@ListLinesAccueil.context, StationDetailsActivity::class.java)
                startActivity(intent)

            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
    // vide input si valeur non choisie dans la liste
    private fun clearFocusAutoTextView(autoCompleteTextView: AutoCompleteTextView) {
        autoCompleteTextView.onFocusChangeListener = OnFocusChangeListener { _, b ->
            if (!b) {
                // on focus off
                val str: String = autoCompleteTextView.text.toString()
                val listAdapter: ListAdapter = autoCompleteTextView.adapter
                for (i in 0 until listAdapter.count) {
                    val temp: String = listAdapter.getItem(i).toString()
                    if (str.compareTo(temp) == 0) {
                        return@OnFocusChangeListener
                    }
                }
                autoCompleteTextView.setText("")
            }
        }
    }

    // Pour cacher le keyboard d'un fragment
    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    // Pour cacher le keyboard avec uniquement un context
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
