package app.epf.ratp_eb_pf

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

// Fragment contenant les préférences de l'utilisateur

class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs)

    }
}

