package app.epf.ratp_eb_pf.service

import retrofit2.http.GET
import retrofit2.http.Path

interface AllLines {

    /**
     * Génère une requète HTTP du type : /api?results=<size>
     */
    @GET("lines/{type}")
    suspend fun getLinesService(
        @Path("type") type: String
    ): GetLinesResult
}

data class GetLinesResult (
    val result: ResultLines,
    val metadata: MetadataLines
)

data class MetadataLines (
    val call: String,
    val date: String,
    val version: Long
)

data class ResultLines (
    val metros: List<LineList>
)

data class LineList (
    val code: String,
    val name: String,
    val directions: String,
    val id: String
)