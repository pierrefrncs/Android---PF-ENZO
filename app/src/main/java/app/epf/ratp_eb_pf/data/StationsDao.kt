package app.epf.ratp_eb_pf.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import app.epf.ratp_eb_pf.model.Stations

// Dao des stations

@Dao
interface StationsDao {

    @Query("select * from stations")
    suspend fun getStations(): MutableList<Stations>

    @Insert
    suspend fun addStation(station: Stations)

    @Query("delete from stations where uuid = :uuid")
    suspend fun deleteStation(uuid: String)

    @Query("delete from stations")
    suspend fun deleteStations()

    @Query("select * from stations where uuid = :uuid")
    suspend fun getStation(uuid: String): Stations
}