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
import androidx.core.content.ContextCompat.getSystemService
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnSuccessListener
import pl.peth.datacollector.databinding.MainActivityBinding
import pl.peth.datacollector.ui.MainActivity
import pl.peth.datacollector.ui.bottomNav.PositionFragment

class PositionManager {
    constructor()

    @SuppressLint("MissingPermission")
    fun setUp(){
        locationListener = object: LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.e("Position", String.format("Lat: %s \t Long: %s", location.latitude, location.longitude))
            }
        }

        initFusedLocationClient()

        if(permissionCheck()){
            fusedLocationClient.lastLocation.addOnSuccessListener {
                OnSuccessListener<Location> {
                        location ->if (location != null) {
                    println("addOnSuccessListener: " + location.latitude + ", " + location.longitude)
                }
                }
            }
        }
    }

    private fun initFusedLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(mainContext);

        locationCallback = object : LocationCallback(){
            override fun onLocationResult(locationRes: LocationResult) {
                println("onLocationResult")
                super.onLocationResult(locationRes)
                val loc = locationRes.lastLocation
                println("locationCallback: " + loc.latitude + ", " + loc.longitude)
            }
        }
    }

    private fun permissionCheck(): Boolean {
        if (ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    @SuppressLint("MissingPermission")
    fun update(posTech: Long, posMode: Long){

        stopLocationManager();
        fusedLocationClient.removeLocationUpdates(locationCallback);

        when(posTech){
            // 0: LocationManager 1: FusedLocationProv
            0L -> {
                when(posMode){
                    //0: Network Provider 1: GPS Provider 2: Stop
                    0L -> {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,0L,0f, locationListener!!)
                    }
                    1L -> {
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0L,0f, locationListener!!)
                    }
                    2L -> {
                        stopLocationManager();
                    }
                }
            }
            1L -> {
                var locationRequest: LocationRequest? = null;

                when(posMode){
                    //0: Balanced Power Accuracy 1: High Accuracy 2: Low Power 3: No Power 4: Stop
                    0L -> {
                        locationRequest = LocationRequest().setFastestInterval(7000).setInterval(5000).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                    }
                    1L -> {
                        locationRequest = LocationRequest().setFastestInterval(7000).setInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                    }
                    2L -> {
                        locationRequest = LocationRequest().setFastestInterval(7000).setInterval(5000).setPriority(LocationRequest.PRIORITY_LOW_POWER)
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                    }
                    3L -> {
                        locationRequest = LocationRequest().setFastestInterval(7000).setInterval(5000).setPriority(LocationRequest.PRIORITY_NO_POWER)
                        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
                    }
                    4L -> {
                        fusedLocationClient.removeLocationUpdates(locationCallback)
                    }
                }
            }
        }
    }

    fun stopLocationManager(){
        locationListener?.let { locationManager.removeUpdates(it) };
    }


    companion object {
        private val locationManager: LocationManager = MainActivity.locationManager;
        private var locationListener: LocationListener? = null;
        private var locationCallback: LocationCallback? = null;
        @SuppressLint("StaticFieldLeak")
        private var positionManager: PositionManager? = null;
        @SuppressLint("StaticFieldLeak")
        private lateinit var mainContext: Context;
        private lateinit var fusedLocationClient: FusedLocationProviderClient;

        fun init(context: Context): PositionManager{
            if(positionManager == null){
                    positionManager = PositionManager();
                    mainContext = context;
            }

            return positionManager as PositionManager;
        }
    }
}