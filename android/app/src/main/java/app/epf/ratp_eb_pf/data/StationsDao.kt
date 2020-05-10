package app.epf.ratp_eb_pf.data


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import app.epf.ratp_eb_pf.model.Stations

@Dao
interface StationsDao {

    @Query("select * from stations")
    suspend fun getStations() : MutableList<Stations>

    @Insert
    suspend fun addStation(line: Stations)

    @Delete
    suspend fun deleteStation(line: Stations)

    @Query("delete from stations")
    suspend fun deleteStations()

    @Query("select * from stations where id = :idStation")
    suspend fun getStation(idStation: Int) : Stations
}