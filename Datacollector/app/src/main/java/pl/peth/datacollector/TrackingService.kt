package pl.peth.datacollector

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.peth.datacollector.Constants.ACTION_PAUSE_SERVICE
import pl.peth.datacollector.Constants.ACTION_SHOW_POSITION_FRAGMENT
import pl.peth.datacollector.Constants.ACTION_START_OR_RESUME_SERVICE
import pl.peth.datacollector.Constants.ACTION_STOP_SERVICE
import pl.peth.datacollector.Constants.FASTEST_LOCATION_UPDATE_INTERVAL
import pl.peth.datacollector.Constants.LOCATION_UPDATE_INTERVAL
import pl.peth.datacollector.Constants.NOTIFICATION_CHANNEL_ID
import pl.peth.datacollector.Constants.NOTIFICATION_CHANNEL_NAME
import pl.peth.datacollector.Constants.NOTIFICATION_ID
import pl.peth.datacollector.ui.MainActivity
import pl.peth.datacollector.ui.MainActivity.Companion.apiHandler

typealias line = MutableList<LatLng>
typealias lines = MutableList<line>

class TrackingService : LifecycleService() {

    var isFirstRun = true
    private var accuracy = PRIORITY_HIGH_ACCURACY
    private var minTime = 0L
    private var minDistance = 0f
    private var strategy = ""
    private var strategyId = 0
    private var routeId = 0
    private var marked = 0
    private var time = System.currentTimeMillis()
    private var firstPoint = 0
    private var secondPoint = 0

    private data class TrackingData(
        val location: Location = Location(""),
        val time: Long = System.currentTimeMillis(),
    )

    private var lastSent: TrackingData? = null

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationManager: LocationManager

    @SuppressLint("VisibleForTests")
    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = FusedLocationProviderClient(this)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        isTracking.observe(
            this,
            Observer {
                updateLocationTracking(it)
            }
        )
    }

    @SuppressLint("LogNotTimber")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { intent ->
            accuracy = intent.getIntExtra("accuracy", PRIORITY_HIGH_ACCURACY)
            routeId = intent.getIntExtra("routeId", 1)
            strategy = intent.getStringExtra("strategy").toString()
            when (strategy) {
                "Zeit" -> {
                    strategyId = 7
                    minDistance = 0f
                    minTime = intent.getFloatExtra("sliderValue", 0F).toLong() * 1000
                }
                "Abstand" -> {
                    strategyId = 8
                    minTime = 0L
                    minDistance = intent.getFloatExtra("sliderValue", 0F)
                }
                "Geschwindichkeit" -> TODO()
            }
            Log.d(
                "Location Variable",
                "$routeId $strategy $minDistance $minTime"
            )
            when (intent.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                        Toast.makeText(
                            applicationContext,
                            "Started Tracking", Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            applicationContext,
                            "Tracking....",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Toast.makeText(
                        applicationContext,
                        "Paused service", Toast.LENGTH_SHORT
                    ).show()
                }
                ACTION_STOP_SERVICE -> {
                    locationListener.let { locationManager.removeUpdates(it) }
                    isFirstRun = true
                    Toast.makeText(
                        applicationContext,
                        "Stopped service", Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            val request = LocationRequest().apply {
                interval = LOCATION_UPDATE_INTERVAL
                fastestInterval = FASTEST_LOCATION_UPDATE_INTERVAL
                priority = accuracy
            }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            )
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener
            )
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result.locations.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        // collect location if needed
                    }
                }
            }
        }
    }

    private val locationListener = object : LocationListener {
        @SuppressLint("LogNotTimber")
        override fun onLocationChanged(location: Location) {
            if (isTracking.value!!) {
                addPathPoint(location)

                val trackingData = TrackingData().apply {
                    this.location.latitude = location.latitude
                    this.location.longitude = location.longitude
                }

                updateMarkedLocation(trackingData)
                sendData(
                    location.longitude,
                    location.latitude,
                )
                Log.d("Location", "${location.latitude} ${location.longitude}")
            }
        }
    }

    private fun addPathPoint(location: Location?) {
        location?.let {
            val pos = LatLng(
                location.latitude,
                location.longitude,
            )
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private fun addEmptyLine() = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun startForegroundService() {
        addEmptyLine()
        isTracking.postValue(true)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder =
            NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setAutoCancel(false)
                .setOngoing(true)
                .setContentTitle("DC Tracker")
                .setContentText("")
                .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_POSITION_FRAGMENT
        },
        FLAG_UPDATE_CURRENT
    )

    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME, IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    private fun sendData(longitude: Double, latitude: Double) {
        Log.e("longitude", "$longitude")
        val data: HashMap<String, String> = hashMapOf(
            "longitude" to "$longitude",
            "latitude" to "$latitude",
            "type" to "$strategyId",
            "route" to "$routeId",
            "marked" to "$marked"
        )

        if ((routeId != null)) {
            GlobalScope.launch {
                val res = apiHandler.postData("position/add", data)
                println(res?.body?.string())
                res?.close()
            }
            Log.d("Location", "-------------------$marked")
            marked = 0
        }
    }

    private fun updateMarkedLocation(data: TrackingData) {
        if (this.lastSent == null) {
            this.marked = 1
            this.lastSent = data
            return
        }

        when (strategy) {
            "Zeit" -> {
                val def = data.time - lastSent!!.time
                Log.d("Time", "$def")
                if (def >= minTime) {
                    marked = 1
                    lastSent = data
                }
            }
            "Abstand" -> {
                val distance = getDistance(data, lastSent!!)
                if(distance >= minDistance){
                    marked = 1
                    lastSent = data
                }
                Log.d("Distance", "---------$distance")
            }
            "Geschwindichkeit" -> TODO()
        }
    }

    private fun getDistance(newDate: TrackingData, oldData: TrackingData): Float {
        return oldData.location.distanceTo(newDate.location)
    }

    companion object {

        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<lines>()
    }
}
