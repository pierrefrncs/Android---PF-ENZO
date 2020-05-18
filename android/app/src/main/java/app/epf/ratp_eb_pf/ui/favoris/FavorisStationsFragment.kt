package app.epf.ratp_eb_pf.ui.favoris

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.data.AppDatabase
import app.epf.ratp_eb_pf.data.StationsDao
import app.epf.ratp_eb_pf.model.Stations
import app.epf.ratp_eb_pf.ui.listeLines.details.StationsAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_favoris_stations.view.*
import kotlinx.coroutines.runBlocking
import java.util.*

// Sous-fragment des favoris pour les stations

class FavorisStationsFragment : Fragment() {

    private var stationDaoSaved: StationsDao? = null
    private lateinit var stationsRecyclerView: RecyclerView
    private var stations: MutableList<Stations>? = null

    private var mRecentlyDeletedItemPosition = 0
    private lateinit var mRecentlyDeletedItem: Stations


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_favoris_stations, container, false)

        stationsRecyclerView = view.findViewById(R.id.savedStations_recyclerview)
        stationsRecyclerView.layoutManager = LinearLayoutManager(activity)

        // En cas de click sur l'image "Aucune station favorite", envoie vers la liste des stations
        view.noStationsImage.setOnClickListener {
            val navController = activity?.findNavController(R.id.nav_host_fragment)
            navController?.navigate(R.id.navigation_list_lignes)
        }

        val database =
            Room.databaseBuilder(requireContext(), AppDatabase::class.java, "savedDatabase")
                .build()

        stationDaoSaved = database.getStationsDao()

        runBlocking {
            stations = stationDaoSaved?.getStations()
        }

        // Si aucune station favorite, affiche l'image "Aucune station favorite", sinon cachée
        runBlocking {
            if (!stations.isNullOrEmpty()) {
                view.layoutNoSavedStation.visibility = View.GONE
            } else {
                view.layoutNoSavedStation.visibility = View.VISIBLE
            }
        }

        // Ajoute l'adapter des stations (liste déroulante des stations favorites)
        stationsRecyclerView.adapter = StationsAdapter(stations ?: mutableListOf(), view)

        // Attache à la recyclerView
        val itemTouchHelper = ItemTouchHelper(simpleCallback())
        itemTouchHelper.attachToRecyclerView(stationsRecyclerView)

        return view
    }

    override fun onResume() {
        super.onResume()

        stationsRecyclerView.adapter = StationsAdapter(stations ?: mutableListOf(), requireView())

    }

    // CallBak pour drag and swipe les stations favorites (déplacer et supprimer)

    // https://www.youtube.com/watch?v=H9D_HoOeKWM&t=225s
    // https://medium.com/@kitek/recyclerview-swipe-to-delete-easier-than-you-thought-cff67ff5e5f6
    private fun simpleCallback(): ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT
        ) {

            private val deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_white_24dp)
            private val intrinsicWidth = deleteIcon?.intrinsicWidth!!
            private val intrinsicHeight = deleteIcon?.intrinsicHeight!!
            private val background = ColorDrawable()
            private val backgroundColor = Color.parseColor("#f44336")
            private val clearPaint = Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

            // Pour déplacer un favoris
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                // Enregistre le déplacement dans la liste et notifie la recyclerView
                Collections.swap(stations!!, fromPosition, toPosition)
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)

                runBlocking {
                    stationDaoSaved?.deleteStations()
                }
                // Update BDD
                var idValue = 1
                stations?.map {
                    it.id = idValue
                    runBlocking {
                        stationDaoSaved?.addStation(it)
                    }
                    idValue += 1
                }
                return false
            }

            // Pour supprimer un favoris
            override fun onSwiped(
                viewHolder: RecyclerView.ViewHolder,
                direction: Int
            ) {
                val fromPosition = viewHolder.bindingAdapterPosition
                mRecentlyDeletedItem = stations!![fromPosition]
                mRecentlyDeletedItemPosition = fromPosition
                runBlocking {
                    stations?.get(fromPosition)?.uuid?.let { stationDaoSaved?.deleteStation(it) }
                }
                // Supprime de la liste et notifie la recyclerView
                stations?.removeAt(fromPosition)
                stationsRecyclerView.adapter?.notifyItemRemoved(fromPosition)
                showUndoSnackbar()
            }

            // Pour avoir la barre rouge + poubelle en supprimant un favoris
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {

                val itemView = viewHolder.itemView
                val itemHeight = itemView.bottom - itemView.top
                val isCanceled = dX == 0f && !isCurrentlyActive

                if (isCanceled) {
                    clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(), itemView.bottom.toFloat())
                    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    return
                }

                // Draw the red delete background
                background.color = backgroundColor
                background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                background.draw(c)

                // Calculate position of delete icon
                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + intrinsicHeight

                // Draw the delete icon
                deleteIcon?.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom)
                deleteIcon?.draw(c)

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }

            private fun clearCanvas(c: Canvas?, left: Float, top: Float, right: Float, bottom: Float) {
                c?.drawRect(left, top, right, bottom, clearPaint)
            }
        }

    // Snackbar pour annuler la suppression
    private fun showUndoSnackbar() {
        val snackbar: Snackbar = Snackbar.make(
            requireView(), "Annuler la suppression",
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction("Undo") { undoDelete() }
        snackbar.show()
    }

    // Ré-inserer la station supprimée
    private fun undoDelete() {
        stations?.add(
            mRecentlyDeletedItemPosition,
            mRecentlyDeletedItem
        )
        stationsRecyclerView.adapter?.notifyItemInserted(mRecentlyDeletedItemPosition)
        runBlocking {
            stationDaoSaved?.addStation(mRecentlyDeletedItem)
        }
    }
}