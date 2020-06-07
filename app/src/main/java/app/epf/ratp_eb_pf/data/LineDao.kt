package app.epf.ratp_eb_pf.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import app.epf.ratp_eb_pf.model.Line

// Dao des lines

@Dao
interface LineDao {

    @Query("select * from lines")
    suspend fun getLines(): MutableList<Line>

    @Insert
    suspend fun addLine(line: Line)

    @Query("delete from lines where idRatp = :idRatp")
    suspend fun deleteLine(idRatp: Int)

    @Query("delete from lines")
    suspend fun deleteLines()

    @Query("select * from lines where idRatp = :idRatp")
    suspend fun getLine(idRatp: Int): Line

    @Query("select * from lines where code = :codeL")
    suspend fun getLineSpec(codeL: String): Line
}