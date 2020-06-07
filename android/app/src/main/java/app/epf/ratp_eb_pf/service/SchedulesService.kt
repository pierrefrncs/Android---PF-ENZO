package app.epf.ratp_eb_pf.service

import retrofit2.http.GET
import retrofit2.http.Path

// Basé sur le JSON des horaires d'une station dans un sens en particulier

interface SchedulesService {

    @GET("schedules/{type}/{code}/{station}/{way}")
    suspend fun getScheduleService(
        @Path("type") type: String,
        @Path("code") code: String,
        @Path("station") station: String,
        @Path("way") way: String // Direction
    ): GetScheduleResult
}

data class GetScheduleResult(
    val result: ResultSchedule,
    val metadata: MetadataSchedule
)

data class MetadataSchedule(
    val call: String,
    val date: String,
    val version: Long
)

data class ResultSchedule(
    val schedules: List<SchedulesData>
)

data class SchedulesData(
    var message: String,
    val destination: String
)
