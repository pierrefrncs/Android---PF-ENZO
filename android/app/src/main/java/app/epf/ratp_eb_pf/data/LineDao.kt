package app.epf.ratp_eb_pf.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import app.epf.ratp_eb_pf.model.Line

@Dao
interface LineDao {

    @Query("select * from lines")
    suspend fun getLines() : MutableList<Line>

    @Insert
    suspend fun addLine(line: Line)

    @Query("delete from lines where idRatp = :idRatp")
    suspend fun deleteLine(idRatp: Int)

    @Query("delete from lines")
    suspend fun deleteLines()

    @Query("select * from lines where idRatp = :idRatp")
    suspend fun getLine(idRatp: Int) : Line
}