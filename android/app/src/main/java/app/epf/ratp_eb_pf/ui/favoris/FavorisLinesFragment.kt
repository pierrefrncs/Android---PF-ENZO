package app.epf.ratp_eb_pf.ui.favoris

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Parcelable
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
import app.epf.ratp_eb_pf.daoTraf
import app.epf.ratp_eb_pf.data.AppDatabase
import app.epf.ratp_eb_pf.data.LineDao
import app.epf.ratp_eb_pf.data.TrafficDao
import app.epf.ratp_eb_pf.model.Line
import app.epf.ratp_eb_pf.model.Traffic
import app.epf.ratp_eb_pf.ui.listeLines.LinesAdapter
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_favoris_lines.view.*
import kotlinx.coroutines.runBlocking
import java.util.*

// Sous-fragment des favoris pour les lines

class FavorisLinesFragment : Fragment() {

    private var traffic: MutableList<Traffic>? = null
    private var trafficDao: TrafficDao? = null
    private var lineDaoSaved: LineDao? = null
    private lateinit var linesRecyclerView: RecyclerView
    private var lines: MutableList<Line>? = null

    private var mRecentlyDeletedItemPosition = 0
    private lateinit var mRecentlyDeletedItem: Line

    private var mBundleRecyclerViewState: Bundle? = null
    private var mListState: Parcelable? = null


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
        trafficDao = daoTraf(requireContext())


        runBlocking {
            lines = lineDaoSaved?.getLines()
            traffic = trafficDao?.getTraffic()

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
        linesRecyclerView.adapter = LinesAdapter(lines ?: mutableListOf(), traffic!!, view)

        // Attache à la recyclerView
        val itemTouchHelper = ItemTouchHelper(simpleCallback())
        itemTouchHelper.attachToRecyclerView(linesRecyclerView)

        return view
    }

    override fun onResume() {
        super.onResume()

        // Pour récupèrer la position de la recyclerView
        if (mBundleRecyclerViewState != null) {
            mListState = mBundleRecyclerViewState!!.getParcelable("keyR")
            linesRecyclerView.layoutManager?.onRestoreInstanceState(mListState)
        }
    }

    override fun onPause() {
        super.onPause()

        mBundleRecyclerViewState = Bundle()

        // Pour enregistrer la position de la recyclerView
        mListState = linesRecyclerView.layoutManager?.onSaveInstanceState()
        mBundleRecyclerViewState!!.putParcelable("keyR", mListState)
    }

    // CallBak pour drag and swipe les lignes favorites (déplacer et supprimer)

    // https://www.youtube.com/watch?v=H9D_HoOeKWM&t=225s
    // https://medium.com/@kitek/recyclerview-swipe-to-delete-easier-than-you-thought-cff67ff5e5f6
    private fun simpleCallback(): ItemTouchHelper.SimpleCallback =
        object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            ItemTouchHelper.LEFT
        ) {

            private val deleteIcon =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete_white_24dp)
            private val intrinsicWidth = deleteIcon?.intrinsicWidth!!
            private val intrinsicHeight = deleteIcon?.intrinsicHeight!!
            private val background = ColorDrawable()
            private val backgroundColor = Color.parseColor("#f44336")
            private val clearPaint =
                Paint().apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }

            // Pour déplacer un favoris
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {

                val fromPosition = viewHolder.bindingAdapterPosition
                val toPosition = target.bindingAdapterPosition
                // Enregistre le déplacement dans la liste et notifie la recyclerView
                Collections.swap(lines!!, fromPosition, toPosition)
                recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)

                runBlocking {
                    lineDaoSaved?.deleteLines()
                }
                // Update BDD
                var idValue = 1
                lines?.map {
                    it.id = idValue
                    runBlocking {
                        lineDaoSaved?.addLine(it)
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
                mRecentlyDeletedItem = lines!![fromPosition]
                mRecentlyDeletedItemPosition = fromPosition
                runBlocking {
                    lines?.get(fromPosition)?.idRatp?.let { lineDaoSaved?.deleteLine(it) }
                }
                // Supprime de la liste et notifie la recyclerView
                lines?.removeAt(fromPosition)
                linesRecyclerView.adapter?.notifyItemRemoved(fromPosition)
                if (lines.isNullOrEmpty()) {
                    view?.layoutNoSavedLine?.visibility = View.VISIBLE
                }
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
                    clearCanvas(
                        c,
                        itemView.right + dX,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                    )
                    super.onChildDraw(
                        c,
                        recyclerView,
                        viewHolder,
                        dX,
                        dY,
                        actionState,
                        isCurrentlyActive
                    )
                    return
                }

                // Draw the red delete background
                background.color = backgroundColor
                background.setBounds(
                    itemView.right + dX.toInt(),
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
                background.draw(c)

                // Calculate position of delete icon
                val deleteIconTop = itemView.top + (itemHeight - intrinsicHeight) / 2
                val deleteIconMargin = (itemHeight - intrinsicHeight) / 2
                val deleteIconLeft = itemView.right - deleteIconMargin - intrinsicWidth
                val deleteIconRight = itemView.right - deleteIconMargin
                val deleteIconBottom = deleteIconTop + intrinsicHeight

                // Draw the delete icon
                deleteIcon?.setBounds(
                    deleteIconLeft,
                    deleteIconTop,
                    deleteIconRight,
                    deleteIconBottom
                )
                deleteIcon?.draw(c)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

            private fun clearCanvas(
                c: Canvas?,
                left: Float,
                top: Float,
                right: Float,
                bottom: Float
            ) {
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

    // Ré-inserer la line supprimée
    private fun undoDelete() {
        lines?.add(
            mRecentlyDeletedItemPosition,
            mRecentlyDeletedItem
        )
        linesRecyclerView.adapter?.notifyItemInserted(mRecentlyDeletedItemPosition)
        runBlocking {
            lineDaoSaved?.addLine(mRecentlyDeletedItem)
        }
        if (!lines.isNullOrEmpty()) {
            view?.layoutNoSavedLine?.visibility = View.GONE
        }
    }
}