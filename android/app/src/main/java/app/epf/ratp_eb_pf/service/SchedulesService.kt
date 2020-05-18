package app.epf.ratp_eb_pf.service

import app.epf.ratp_eb_pf.model.Schedules
import retrofit2.http.GET
import retrofit2.http.Path

// Basé sur le JSON des horaires d'une station et destination en particulier

interface SchedulesService {

        @GET("schedules/{type}/{code}/{station}/{way}")
        suspend fun getScheduleService(
                @Path("type") type: String,
                @Path("code") code: String,
                @Path("station") station: String,
                @Path("way") way: String
        ): GetScheduleResult
}

data class GetScheduleResult (
    val result: ResultSchedule,
    val metadata: MetadataSchedule
)

data class ResultSchedule (
    val schedule: List<Schedules>
)

data class MetadataSchedule (
    val call: String,
    val date: String,
    val version: Long
)

data class Schedules(
    val message: String,
    val destination: String
)
