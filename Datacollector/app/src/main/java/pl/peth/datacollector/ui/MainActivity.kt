package pl.peth.datacollector.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import pl.peth.datacollector.R
import pl.peth.datacollector.api.APIHandler
import pl.peth.datacollector.databinding.MainActivityBinding
import pl.peth.datacollector.sensor.SensorHandler

class MainActivity : AppCompatActivity() {

    private var binding: MainActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiHandler = APIHandler(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        binding = DataBindingUtil.setContentView<MainActivityBinding>(
            this, R.layout.main_activity)
            .apply {
                lifecycleOwner = this@MainActivity
            }
        setupNavigation()
        checkPermissions()
    }

    private fun setupNavigation() {
        val bottomNavigationView = binding?.bottomNav
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container)
                    as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView?.setupWithNavController(navController)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
            ) {
            } else {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.READ_PHONE_STATE),
                    2)
            }
        } else {
            // Permission has already been granted
        }
    }

    companion object {
        fun buildIntent(context: Context) = Intent(context, MainActivity::class.java)
        lateinit var apiHandler: APIHandler
        lateinit var sensorManager: SensorManager
    }
}