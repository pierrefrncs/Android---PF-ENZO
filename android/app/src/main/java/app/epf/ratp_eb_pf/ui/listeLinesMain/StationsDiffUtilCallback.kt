package app.epf.ratp_eb_pf.ui.listeLines

import androidx.recyclerview.widget.DiffUtil
import app.epf.ratp_eb_pf.model.Stations

// Pour pouvoir filtrer les stations de la recyclerView de la page d'accueil

class StationsDiffUtilCallback (private val oldList: MutableList<Stations>, private val newList: MutableList<Stations>) : DiffUtil.Callback() {
    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = true
}