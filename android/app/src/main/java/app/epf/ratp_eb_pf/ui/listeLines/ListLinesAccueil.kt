package app.epf.ratp_eb_pf.ui.listeLines

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
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.android.synthetic.main.fragment_liste_lines.view.*
import kotlinx.coroutines.runBlocking


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
        //       val savedTopLevel_layout = view.findViewById<LinearLayout>(R.id.savedTopLevel_layout)
        linesRecyclerView.layoutManager = LinearLayoutManager(activity)

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

        qrCode.setOnClickListener{
            IntentIntegrator.forSupportFragment(this)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .setBeepEnabled(false)
                .initiateScan()
        }

        stationsDao = daoSta(requireContext())
        lineDao = daoLi(requireContext())

        runBlocking {
            lines = lineDao?.getLines()

            lines?.map {
                listArray.add(it.name)
            }
            val adapterMetro = IgnoreAccentsArrayAdapter(
                requireContext(),
                android.R.layout.simple_list_item_1, listArray.toTypedArray()
            )
            rechercherList.setAdapter(adapterMetro)

            clearFocusAutoTextView(rechercherList)
        }

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


    // a modifier pour que ça redirige sur une vue
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(getActivity(), "Scanned: " + result.contents, Toast.LENGTH_LONG).show()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun synchroServerAllLines(view: View) {
        if (lines.isNullOrEmpty()) {
            val service = retrofit().create(LinesService::class.java)
            runBlocking {
                lineDao?.deleteLines()
                val result = service.getLinesService("metros")
                result.result.metros.map {
<<<<<<< Updated upstream:android/app/src/main/java/app/epf/ratp_eb_pf/ui/listeLines/ListLinesAccueil.kt

                    val line = Line(0, it.code, it.name, it.directions, it.id.toInt(), false)
=======
                    val line = Line(id, it.code, it.name, it.directions, it.id.toInt(), false)
                    // Enlève les stations de metro inutiles
>>>>>>> Stashed changes:android/app/src/main/java/app/epf/ratp_eb_pf/ui/listeLinesMain/ListLinesAccueil.kt
                    if (it.id != "79" && it.id != "455") {
                        lineDao?.addLine(line)
                    }
                }
                lines = lineDao?.getLines()
                linesRecyclerView.adapter = LinesAdapter(lines ?: mutableListOf(), view)
            }
        }
    }

<<<<<<< Updated upstream:android/app/src/main/java/app/epf/ratp_eb_pf/ui/listeLines/ListLinesAccueil.kt
    // vide textView si non choisi dans la liste
=======
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
>>>>>>> Stashed changes:android/app/src/main/java/app/epf/ratp_eb_pf/ui/listeLinesMain/ListLinesAccueil.kt
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


    private fun Fragment.hideKeyboard() {
        view?.let { activity?.hideKeyboard(it) }
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
