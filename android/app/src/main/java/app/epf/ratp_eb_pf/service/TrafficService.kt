package app.epf.ratp_eb_pf.service

import retrofit2.http.GET
import retrofit2.http.Path

interface TrafficService {

    @GET("traffic/{type}/{code}")
    suspend fun getTrafficService(
        @Path("type") type: String,
        @Path("code") code: String
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
    val line: String,
    val slug: String,
    val title: String,
    val message: String
)