package pl.peth.datacollector.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import pl.peth.datacollector.R
import pl.peth.datacollector.api.APIHandler
import pl.peth.datacollector.databinding.MainActivityBinding
import pl.peth.datacollector.ui.manager.PermissionManager


class MainActivity : AppCompatActivity() {

    private var binding: MainActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiHandler = APIHandler(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        binding = DataBindingUtil.setContentView<MainActivityBinding>(
            this, R.layout.main_activity
        )
            .apply {
                lifecycleOwner = this@MainActivity
            }
        preparePermissions()
        setupNavigation()
    }

    private fun preparePermissions() {
        val permissions = listOf<String>(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        //            Manifest.permission.ACCESS_COARSE_LOCATION,

        val managePermissions = PermissionManager(this, permissions, 2)

        managePermissions.checkPermissions()
    }

    private fun setupNavigation() {
        val bottomNavigationView = binding?.bottomNav
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_container)
                    as NavHostFragment
        val navController = navHostFragment.navController
        bottomNavigationView?.setupWithNavController(navController)
    }


    companion object {
        fun buildIntent(context: Context) = Intent(context, MainActivity::class.java)
        lateinit var apiHandler: APIHandler
        lateinit var sensorManager: SensorManager
    }
}