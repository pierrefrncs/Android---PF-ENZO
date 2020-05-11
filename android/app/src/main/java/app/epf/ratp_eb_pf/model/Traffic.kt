package app.epf.ratp_eb_pf.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

// Modèle du traffic. Ex : Etat du traffic sur le metro 1

@Entity(tableName = "traffic")
data class Traffic(
    @PrimaryKey(autoGenerate = false) val id: Int,
    val line: String,
    val slug: String,
    val title: String,
    val message: String
) : Serializable