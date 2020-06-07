package app.epf.ratp_eb_pf

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager


const val KEY_THEME = "prefs.theme"
const val THEME_LIGHT = 0
const val THEME_DARK = 1
const val THEME_SYSTEM = 2
const val THEME_BATTERY = 3

// Activity contenant le fragment des préférences

class PreferencesActivity : AppCompatActivity(),
    SharedPreferences.OnSharedPreferenceChangeListener {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_preferences)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.container_layout, PreferencesFragment())
            .commit()

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    // En cas de changement d'un paramètre
    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

        // Set le thème de l'application
        when (sharedPreferences?.getString("theme_display", "themeSystem")) {
            "themeLight" -> setTheme(AppCompatDelegate.MODE_NIGHT_NO, THEME_LIGHT)
            "themeDark" -> setTheme(AppCompatDelegate.MODE_NIGHT_YES, THEME_DARK)
            "themeSystem" -> setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, THEME_SYSTEM)
        }
        restartPage()
    }

    private fun setTheme(themeMode: Int, prefsMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        saveTheme(prefsMode)
    }

    private fun saveTheme(theme: Int) = prefs.edit().putInt(KEY_THEME, theme).apply()

    // Reload la page en cas de changement de thème
    private fun restartPage() {
        val k = Intent(this, PreferencesActivity::class.java)

        k.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        startActivity(k)
        overridePendingTransition(0, 0)
        finish()
        overridePendingTransition(0, 0)
    }
}