package pl.peth.datacollector

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
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
import java.util.function.Consumer
import kotlin.math.sqrt

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
    private var abstand = 0f
    private val sensorManager = MainActivity.sensorManager
    private var lastMovement: Long = 0
    private var isRunning: Boolean = false
    private var cancelService: CancellationSignal = CancellationSignal()

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

    val aSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) // m/s²

    val eHandler = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            val x = event?.values!![0]
            val y = event.values[1]
            val z = event.values[2]

            val mAccelCurrent: Float = sqrt(x * x + y * y + z * z)
            Log.d("M-ACCEL_CURRENT", mAccelCurrent.toString())
            if (mAccelCurrent > 6) {
                Log.d("BEWEGUNG!", "$x $y $z")
                lastMovement = System.currentTimeMillis()
                Log.d("onSensorChanged", mAccelCurrent.toString())
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        }
    }

    @SuppressLint("LogNotTimber")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { intent ->
            accuracy = intent.getIntExtra("accuracy", PRIORITY_HIGH_ACCURACY)
            routeId = intent.getIntExtra("routeId", 1)
            strategy = intent.getStringExtra("strategy").toString()
            abstand = intent.getFloatExtra("strategy2", 0f)
            sensorManager.unregisterListener(eHandler)

            Log.d("STRATEGY", strategy)

            when (strategy) {
                "Zeit" -> {
                    strategyId = 7
                    minDistance = 0f
                    minTime = intent.getFloatExtra("sliderValue", 0F).toLong() * 1000
                }
                "Abstand" -> {
                    strategyId = 8
                    minTime = 1000L
                    minDistance = intent.getFloatExtra("sliderValue", 0F)
                }
                "Geschwindichkeit" -> {
                    strategyId = 9
                    minDistance = intent.getFloatExtra("sliderValue", 0F)
                    val maxSpeed = 2.0 // in kmh
                    minTime = (((maxSpeed / 3.6) * 50) * 1000).toLong()
                    Log.d("DELAY", minTime.toString())
                }
                "Bewegung" -> {
                    strategyId = 10
                    minTime = 1000L
                    minDistance = intent.getFloatExtra("sliderValue", 0F)
                    sensorManager.registerListener(
                        eHandler,
                        aSensor,
                        SensorManager.SENSOR_DELAY_FASTEST
                    )
                    Log.d("xd", "OKKAAAAY LETS GO")
                }
            }

            Log.d(
                "Location Variable",
                "$routeId $strategy $minDistance $minTime"
            )
            when (intent.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    isRunning = true
                    if (isFirstRun) {
                        startForegroundService()
                        startRoutine(minTime)
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
                    isRunning = false
                    // locationListener.let { locationManager.removeUpdates(it) }
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
            /*
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                0L,
                0f,
                locationListener
            )
            */
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

    /*
    private val locationListener = object : LocationListener {
        @SuppressLint("LogNotTimber")
        override fun onLocationChanged(location: Location) {
            if (isTracking.value!!) {
                addPathPoint(location)

                val trackingData = TrackingData().apply {
                    this.location.latitude = location.latitude
                    this.location.longitude = location.longitude
                }

                //updateMarkedLocation(trackingData)
                sendData(
                    location.longitude,
                    location.latitude,
                )
                Log.d("Location", "${location.latitude} ${location.longitude}")
            }
        }
    }
    */

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    private fun getLocation(callback: Consumer<Location>) {
        locationManager.getCurrentLocation(
            LocationManager.GPS_PROVIDER,
            cancelService,
            mainExecutor,
            callback
        )
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun startRoutine(delay: Long) {
        GlobalScope.launch(Dispatchers.IO) {
            while (isRunning) {
                when (strategy) {
                    "Zeit" -> {
                        getLocation { loc ->
                            val trackingData = TrackingData().apply {
                                this.location.latitude = loc.latitude
                                this.location.longitude = loc.longitude
                            }
                            marked = 1

                            sendData(trackingData)
                        }
                    }
                    "Abstand", "Geschwindichkeit" -> {
                        getLocation { loc ->
                            val trackingData = TrackingData().apply {
                                this.location.latitude = loc.latitude
                                this.location.longitude = loc.longitude
                            }

                            if (lastSent != null) Log.d(
                                "Ausgerechnete Distanz",
                                lastSent!!.location.distanceTo(loc).toString()
                            )
                            if (lastSent == null || lastSent!!.location.distanceTo(loc) > minDistance) {
                                marked = 1
                            } else {
                                marked = 0
                            }
                            sendData(trackingData)
                        }
                    }
                    "Bewegung" -> {
                        if (lastMovement == 0L) continue

                        if (System.currentTimeMillis() - lastMovement <= 10000) {
                            getLocation { loc ->
                                val trackingData = TrackingData().apply {
                                    this.location.latitude = loc.latitude
                                    this.location.longitude = loc.longitude
                                }

                                if (lastSent == null || lastSent!!.location.distanceTo(loc) > minDistance) {
                                    marked = 1
                                } else {
                                    marked = 0
                                }

                                sendData(trackingData)
                            }
                        }
                    }
                }
                delay(delay); // Delay aus UI
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

    private fun sendData(trackingData: TrackingData) {
        Log.e("longitude", "${trackingData.location.longitude}")
        val data: HashMap<String, String> = hashMapOf(
            "longitude" to "${trackingData.location.longitude}",
            "latitude" to "${trackingData.location.latitude}",
            "type" to "$strategyId",
            "route" to "$routeId",
            "marked" to "$marked"
        )

        if ((routeId != null)) {
            if (marked == 1) lastSent = trackingData
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
                if (distance >= minDistance) {
                    marked = 1
                    lastSent = data
                }
                Log.d("Distance", "---------$distance")
            }
            "Geschwindichkeit" -> {
                val maxSpeed = 2.0 // in kmh
                val reqDistance = 50 // in m
                val calcTimePeriod = (maxSpeed / 3.6) * reqDistance
                val timeDiff = data.time - lastSent!!.time
                if (timeDiff >= calcTimePeriod) {
                    val distance = getDistance(data, lastSent!!)
                    if (distance >= reqDistance) {
                        marked = 1
                        lastSent = data
                    }
                    return
                }
            }
            "Bewegung" -> {
                if (lastMovement == 0L) return
                if (data.time - lastMovement <= 10000) {
                    marked = 1
                    lastSent = data
                }
                return
            }
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
