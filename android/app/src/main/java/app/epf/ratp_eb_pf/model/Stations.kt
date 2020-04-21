package app.epf.ratp_eb_pf.model

data class Stations (val name:String, val slug:String){

    override fun toString(): String {
        return "Stations(name='$name', slug='$slug')"
    }
}
