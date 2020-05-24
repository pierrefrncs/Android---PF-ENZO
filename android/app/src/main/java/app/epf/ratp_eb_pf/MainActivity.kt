package app.epf.ratp_eb_pf

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.google.android.material.bottomnavigation.BottomNavigationView

// Activity principale contenant les fragments de la page d'accueil

class MainActivity : AppCompatActivity() {

    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {

        // Récupère les préférences
        prefs = PreferenceManager.getDefaultSharedPreferences(this)

        // Set les préférences pour le thème au lancement de l'appli
        when (prefs.getString("theme_display", "themeSystem")) {
            "themeLight" -> setTheme(AppCompatDelegate.MODE_NIGHT_NO, THEME_LIGHT)
            "themeDark" -> setTheme(AppCompatDelegate.MODE_NIGHT_YES, THEME_DARK)
            "themeBattery" -> setTheme(AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY, THEME_BATTERY)
            "themeSystem" -> setTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM, THEME_SYSTEM)
        }


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        // Configure la barre de navigation
        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_list_lignes, R.id.navigation_favoris, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        // Set la page d'accueil de l'application
        if (prefs.getString("start_page", "listLines") == "favoris") {
            navController.navigate(R.id.navigation_favoris)
        }

    }

    // Clear focus et hide keyboard automatiquement en quittant un input
    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is AutoCompleteTextView || v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    hideKeyboard()
                    v.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


    private fun setTheme(themeMode: Int, prefsMode: Int) {
        AppCompatDelegate.setDefaultNightMode(themeMode)
        saveTheme(prefsMode)
    }

    private fun saveTheme(theme: Int) = prefs.edit().putInt(KEY_THEME, theme).apply()


    // Pour cacher le keyboard d'une activité
    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    // Pour cacher le keyboard avec uniquement un context
    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    // Affiche bouton settings
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, PreferencesActivity::class.java)
                startActivity(intent)
                true
            }
            else -> onOptionsItemSelected(item)
        }
    }
}
