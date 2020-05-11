package app.epf.ratp_eb_pf.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// Modèle d'une station. Ex : Station La Défense du metro 1

@Entity(tableName = "stations")
data class Stations(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val name: String,
    val slug: String,
    val line: String, // Pour savoir à quelle ligne appartient la station
    var favoris: Boolean,
    val uuid: String = slug + line // Pour avoir un uuid unique
) : Serializable