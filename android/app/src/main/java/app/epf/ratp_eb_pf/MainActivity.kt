package app.epf.ratp_eb_pf

<<<<<<< Updated upstream
import android.os.Bundle
=======
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.Toast
>>>>>>> Stashed changes
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
<<<<<<< Updated upstream
import app.epf.ratp_eb_pf.service.RetrofitFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
=======
import com.google.zxing.integration.android.IntentIntegrator
>>>>>>> Stashed changes

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        val navController = findNavController(R.id.nav_host_fragment)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        val appBarConfiguration = AppBarConfiguration(setOf(
                R.id.navigation_home, R.id.navigation_dashboard))

        setupActionBarWithNavController(navController, appBarConfiguration)

<<<<<<< Updated upstream
        navView.setupWithNavController(navController)
=======


>>>>>>> Stashed changes
    }

    private fun getLines(){
        // tache asynchrone
        val service = RetrofitFactory.retrofitRATP()
        CoroutineScope(Dispatchers.IO).launch {
            val response = service.getLines()

            withContext(Dispatchers.Main) {
                try {
                    if (response.isSuccessful) {

                    } else {
                        //
                    }
                } catch (e: HttpException) {
                    //
                } catch (e: Throwable) {
                    //
                }
            }
        }
    }
}
