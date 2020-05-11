package app.epf.ratp_eb_pf.service

import retrofit2.http.GET
import retrofit2.http.Path

// Basé sur le JSON des lines d'un type en particulier

interface LinesService {
    
    @GET("lines/{type}")
    suspend fun getLinesService(
        @Path("type") type: String // Type à rentrer. Ex : metro
    ): GetLinesResult
}

data class GetLinesResult(
    val result: ResultLines,
    val metadata: MetadataLines
)

data class MetadataLines(
    val call: String,
    val date: String,
    val version: Long
)

data class ResultLines(
    val metros: List<LineList>
)

data class LineList(
    val code: String,
    val name: String,
    val directions: String,
    val id: String
)