package app.epf.ratp_eb_pf.data


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import app.epf.ratp_eb_pf.model.Line

@Dao
interface LineDao {

    @Query("select * from lines")
    suspend fun getLines() : List<Line>

    @Insert
    suspend fun addLine(line: Line)

    @Delete
    suspend fun deleteLine(line: Line)

    @Query("delete from lines")
    suspend fun deleteLines()

    @Query("select * from lines where id = :idLine")
    suspend fun getLines(idLine: Int) : Line
}