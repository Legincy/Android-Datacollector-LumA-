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