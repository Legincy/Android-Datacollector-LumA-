package pl.peth.datacollector.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.SensorManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import pl.peth.datacollector.R
import pl.peth.datacollector.api.APIHandler
import pl.peth.datacollector.databinding.MainActivityBinding
import pl.peth.datacollector.position.PositionManager
import pl.peth.datacollector.ui.manager.PermissionManager

class MainActivity : AppCompatActivity() {

    private var binding: MainActivityBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        apiHandler = APIHandler(this)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        positionManager = PositionManager.init(this)
        val bundle = intent.extras
        println("------------------------> ONCREAT")
        println(intent.getStringExtra("INTENT_TYPE"))
        if (bundle?.getString("INTENT_TYPE") != null) {
            actionOnService(Actions.Actions.STOP)
            println("YES <<-------------------------------------------------------")
        }

        binding = DataBindingUtil.setContentView<MainActivityBinding>(
            this, R.layout.main_activity
        )
            .apply {
                lifecycleOwner = this@MainActivity
            }

        preparePermissions()
        setupNavigation()
        actionOnService(Actions.Actions.START)
    }

    private fun preparePermissions() {
        val permissions = listOf<String>(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            Manifest.permission.READ_EXTERNAL_STORAGE
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

    fun actionOnService(action: Actions.Actions) {
        if (getServiceState(this) == ServiceState.STOPPED && action == Actions.Actions.STOP) return
        Intent(this, Service1::class.java).also {
            it.action = action.name
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                println("Starting the service in >=26 Mode")
                startForegroundService(it)
                return
            }
            println("Starting the service in < 26 Mode")
            startService(it)
        }
    }

    companion object {
        fun buildIntent(context: Context) = Intent(context, MainActivity::class.java)
        lateinit var apiHandler: APIHandler
        lateinit var locationManager: LocationManager
        lateinit var positionManager: PositionManager
        lateinit var sensorManager: SensorManager
    }
}
