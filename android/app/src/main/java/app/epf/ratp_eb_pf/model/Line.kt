package app.epf.ratp_eb_pf.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "lines")
data class Line(
    @PrimaryKey(autoGenerate = true) val id: Int,
    val code: String,
    val name: String,
    val directions: String,
    val idRatp: Int,
    var favoris: Boolean
) : Serializable