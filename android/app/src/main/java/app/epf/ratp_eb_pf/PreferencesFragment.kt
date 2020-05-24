package app.epf.ratp_eb_pf

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat


class PreferencesFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.prefs)

    }
}

