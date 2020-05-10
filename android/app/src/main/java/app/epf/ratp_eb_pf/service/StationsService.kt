package app.epf.ratp_eb_pf.service

import retrofit2.http.GET
import retrofit2.http.Path

interface StationsService {

    @GET("stations/{type}/{code}")
    suspend fun getStationsService(
        @Path("type") type: String,
        @Path("code") code: String
    ): GetStationsResult
}

data class GetStationsResult(
    val result: ResultStations,
    val metadata: MetadataStations
)

data class MetadataStations(
    val call: String,
    val date: String,
    val version: Long
)

data class ResultStations(
    val stations: List<Station>
)

data class Station(
    val name: String,
    val slug: String
)
