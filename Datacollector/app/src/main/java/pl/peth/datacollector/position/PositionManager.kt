package pl.peth.datacollector.position

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.peth.datacollector.api.APIHandler
import pl.peth.datacollector.ui.MainActivity

class PositionManager {
    private val apiHandler: APIHandler = MainActivity.apiHandler
    var routeId: Int? = null
    var longitude: Double? = null
    var latitude: Double? = null
    private var typeId: Int? = null
    private var marked: Int = 0

    constructor()

    @SuppressLint("MissingPermission")
    fun setUp() {
        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                sendData(location.longitude, location.latitude)
            }
        }
        initFusedLocationClient()

        if (permissionCheck()) {
            fusedLocationClient.lastLocation.addOnSuccessListener {
                OnSuccessListener<Location> { location ->
                    if (location != null) {
                        println("addOnSuccessListener: " + location.latitude + ", " + location.longitude)
                    }
                }
            }
        }
    }

    private fun sendData(longitude: Double, latitude: Double) {
        Log.e("longitude", "$longitude")
        val data: HashMap<String, String> = hashMapOf(
            "longitude" to "$longitude",
            "latitude" to "$latitude",
            "type" to "$typeId",
            "route" to "$routeId",
            "marked" to "$marked"
        )
        this.longitude = longitude
        this.latitude = latitude

        if((marked == 1) && (routeId != null)){
            marked = 0
            GlobalScope.launch {
                val res = apiHandler.postData("position/add", data)
                println(res?.body?.string())
                res?.close();
            }
        }
    }

    fun setMarked() {
        this.marked = 1
    }

    private fun initFusedLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainContext)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationRes: LocationResult) {
                super.onLocationResult(locationRes)
                val loc = locationRes.lastLocation
                sendData(loc.longitude, loc.latitude)
            }
        }
    }

    private fun permissionCheck(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                mainContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                    mainContext,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    @SuppressLint("MissingPermission")
    fun update(posTech: Long?, posMode: Long?, routeId: Int?) {
        if((posTech == null) && (posMode == null)){
            this.routeId = routeId;
        }else {
            stopLocationManager()
            fusedLocationClient.removeLocationUpdates(locationCallback)

            when (posTech) {
                // 0: LocationManager 1: FusedLocationProv
                0L -> {
                    when (posMode) {
                        // 0: Network Provider 1: GPS Provider 2: Stop
                        0L -> {
                            this.typeId = 1
                            locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                0L,
                                0f,
                                locationListener!!
                            )
                        }
                        1L -> {
                            this.typeId = 2
                            locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                0L,
                                0f,
                                locationListener!!
                            )
                        }
                        2L -> {
                            stopLocationManager()
                        }
                    }
                }
                1L -> {
                    var locationRequest: LocationRequest? = null

                    when (posMode) {
                        // 0: Balanced Power Accuracy 1: High Accuracy 2: Low Power 3: No Power 4: Stop
                        0L -> {
                            this.typeId = 3
                            locationRequest =
                                LocationRequest().setFastestInterval(1000).setInterval(2000)
                                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                            fusedLocationClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                null
                            )
                        }
                        1L -> {
                            this.typeId = 4
                            locationRequest =
                                LocationRequest().setFastestInterval(1000).setInterval(2000)
                                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                            fusedLocationClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                null
                            )
                        }
                        2L -> {
                            this.typeId = 5
                            locationRequest =
                                LocationRequest().setFastestInterval(1000).setInterval(2000)
                                    .setPriority(LocationRequest.PRIORITY_LOW_POWER)
                            fusedLocationClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                null
                            )
                        }
                        3L -> {
                            this.typeId = 6
                            locationRequest =
                                LocationRequest().setFastestInterval(1000).setInterval(2000)
                                    .setPriority(LocationRequest.PRIORITY_NO_POWER)
                            fusedLocationClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                null
                            )
                        }
                        4L -> {
                            fusedLocationClient.removeLocationUpdates(locationCallback)
                        }
                    }
                }
            }
        }
    }

    fun stopLocationManager() {
        locationListener?.let { locationManager.removeUpdates(it) }
    }

    companion object {
        private val locationManager: LocationManager = MainActivity.locationManager
        private var locationListener: LocationListener? = null
        private var locationCallback: LocationCallback? = null

        @SuppressLint("StaticFieldLeak")
        private var positionManager: PositionManager? = null

        @SuppressLint("StaticFieldLeak")
        private lateinit var mainContext: Context
        private lateinit var fusedLocationClient: FusedLocationProviderClient

        fun init(context: Context): PositionManager {
            if (positionManager == null) {
                positionManager = PositionManager()
                mainContext = context
            }

            return positionManager as PositionManager
        }
    }
}
