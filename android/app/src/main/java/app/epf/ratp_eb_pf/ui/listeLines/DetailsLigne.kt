package app.epf.ratp_eb_pf.ui.listeLines

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import app.epf.ratp_eb_pf.R
import app.epf.ratp_eb_pf.model.Line
import kotlinx.android.synthetic.main.activity_details_ligne.*

class DetailsLigne : AppCompatActivity() {

    private var line: Line? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_ligne)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        line = intent.getSerializableExtra("line") as Line

        testEditText.text = line?.name
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
}
