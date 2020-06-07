package app.epf.ratp_eb_pf.service

import retrofit2.http.GET
import retrofit2.http.Path

// Basé sur le JSON du traffic de toutes les lignes d'un type en particulier

interface TrafficService {

    @GET("traffic/{type}")
    suspend fun getTrafficService(
        @Path("type") type: String // Type de ligne. Ex :metro

    ): GetTrafficResult
}

data class GetTrafficResult(
    val result: ResultTraffic,
    val metadata: MetadataTraffic
)

data class MetadataTraffic(
    val call: String,
    val date: String,
    val version: Long
)

data class ResultTraffic(
    val metros: List<Traffic>
)

data class Traffic(
    val line: String,
    val slug: String,
    val title: String,
    val message: String
)