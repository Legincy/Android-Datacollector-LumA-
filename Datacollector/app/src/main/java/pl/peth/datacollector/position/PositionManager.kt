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
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import pl.peth.datacollector.databinding.MainActivityBinding
import pl.peth.datacollector.ui.MainActivity
import pl.peth.datacollector.ui.bottomNav.PositionFragment

class PositionManager {
    constructor()

    fun setUp(){
        locationListener = object: LocationListener {
            override fun onLocationChanged(location: Location) {
                Log.e("Position", String.format("Lat: %s \t Long: %s", location?.latitude, location?.longitude))
            }

        }

        if (ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(mainContext, Manifest.permission.ACCESS_COARSE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f,
                locationListener as LocationListener
            );
        }
    }

    @SuppressLint("MissingPermission")
    fun setUpFLP(){
        println("DONE2");
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(mainContext)
        println("DONE1");
        /*
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                location -> if (location != null) {
                    Log.e("GMS", "" + location.latitude + " " + location.longitude);
                }
        }*/
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationRes: LocationResult) {
                println("onLocationResult")
                super.onLocationResult(locationRes)
                val loc = locationRes.lastLocation
                println("locationCallback: " + loc.latitude + ", " + loc.longitude)
            }
        }

        var locationRequest: LocationRequest = LocationRequest().setFastestInterval(11000).setInterval(10000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback,  null)
        fusedLocationProviderClient.lastLocation.addOnSuccessListener {
            OnSuccessListener<Location> {
                    location ->if (location != null) {
                println("addOnSuccessListener: " + location.latitude + ", " + location.longitude)
            }
            }
        }

        println("DONE");
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
    fun update(mode: String, minTime: Long, minDistanceM: Float){
        Log.e("Mode", mode);
        if(permissionCheck()){
            locationManager.requestLocationUpdates(mode, minTime, minDistanceM, locationListener as LocationListener);
        }
    }

    fun stop(){
        locationListener?.let { locationManager.removeUpdates(it) };
    }


    companion object {
        private val locationManager: LocationManager = MainActivity.locationManager;
        private var locationListener: LocationListener? = null;
        @SuppressLint("StaticFieldLeak")
        private var positionManager: PositionManager? = null;
        @SuppressLint("StaticFieldLeak")
        private lateinit var mainContext: Context;

        fun init(context: Context): PositionManager{
            if(positionManager == null){
                    positionManager = PositionManager();
                    mainContext = context;
            }

            return positionManager as PositionManager;
        }
    }
}