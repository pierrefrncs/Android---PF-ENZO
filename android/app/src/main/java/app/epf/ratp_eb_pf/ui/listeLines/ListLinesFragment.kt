package app.epf.ratp_eb_pf.ui.listeLines

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ListAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import android.view.View.OnFocusChangeListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.epf.ratp_eb_pf.*
import app.epf.ratp_eb_pf.data.StationsDao
import app.epf.ratp_eb_pf.model.Stations
import app.epf.ratp_eb_pf.service.StationsService
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.service.AllLines
import kotlinx.android.synthetic.main.fragment_liste_lines.view.*
import kotlinx.coroutines.runBlocking

class ListLinesFragment : Fragment() {

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
        val savedTopLevel_layout = view.findViewById<LinearLayout>(R.id.savedTopLevel_layout)
        linesRecyclerView.layoutManager = LinearLayoutManager(activity)

        view.buttonSearch.setOnClickListener {
            if (rechercherList != null) {
                val intent = Intent(requireContext(), DetailsLigne::class.java)
                lines?.map {
                    if (rechercherList.text.toString() == it.name) {
                        intent.putExtra("line", it)
                        startActivity(intent)
                        return@setOnClickListener
                    }
                }

            }
        }

//        savedTopLevel_layout.setOnClickListener {
//            hideKeyboard()
//            //         activity?.currentFocus?.clearFocus()
//            savedTopLevel_layout.requestFocus()
//        }

        qrCode.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, 100)
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

        synchroServerAllLines()

        return view
    }

    override fun onResume() {
        super.onResume()

        linesRecyclerView.adapter = LinesAdapter(lines ?: mutableListOf(), requireView())
    }


    private fun synchroServerStations() {
        val service = retrofit().create(StationsService::class.java)
        runBlocking {
            stationsDao?.deleteStations()
            val result = service.getStationsService("metros", "1")
            result.result.stations.map {

                val station = Stations(0, it.name, it.slug)
                stationsDao?.addStations(station)

            }
            val stations = stationsDao?.getStations()
        }
    }


    private fun synchroServerAllLines() {
        if (lines.isNullOrEmpty()) {
            val service = retrofit().create(AllLines::class.java)
            runBlocking {
                lineDao?.deleteLines()
                val result = service.getLinesService("metros")
                result.result.metros.map {

                    val line = Line(0, it.code, it.name, it.directions, it.id.toInt(), false)
                    if (it.id != "79" && it.id != "455") {
                        lineDao?.addLine(line)
                    }

                }
                lines = lineDao?.getLines()
                linesRecyclerView.adapter = LinesAdapter(lines ?: mutableListOf(), requireView())
            }
        }
    }

    // vide textView si non choisi dans la liste
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

//    private fun Activity.hideKeyboard() {
//        hideKeyboard(currentFocus ?: View(this))
//    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
}
