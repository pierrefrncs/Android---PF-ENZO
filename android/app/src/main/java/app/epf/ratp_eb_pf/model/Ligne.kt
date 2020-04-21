package app.epf.ratp_eb_pf.model

data class Ligne(val code:Int, val name: String, val direction: String, val id: Int, val stationsA: List<Stations>, val stationsR: List<Stations>){

    override fun toString(): String {
        return "Ligne(code=$code, name='$name', direction='$direction', id=$id, stations Aller=$stationsA , stations Retour=$stationsR)"
    }

}
