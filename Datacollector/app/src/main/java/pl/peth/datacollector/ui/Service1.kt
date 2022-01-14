package pl.peth.datacollector.ui

import android.annotation.SuppressLint
import android.app.* // ktlint-disable no-wildcard-imports
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.hardware.* // ktlint-disable no-wildcard-imports
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import android.os.IBinder
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.* // ktlint-disable no-wildcard-imports
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import pl.peth.datacollector.R
import java.io.IOException
import kotlin.math.sqrt

class Service1 : Service() {

    // General needed objects
    private var isServiceStarted = false
    private var cancelService: CancellationSignal = CancellationSignal()
    private var wakeLock: PowerManager.WakeLock? = null
    private var locationManager: LocationManager? = null
    private var userID: String? = ""
    private var fixCount = 0

    // Itent Stop Key
    private var ACTION_STOP_SERVICE: String = "STOPME"

    // Intent Data
    private var type = ""
    private var data0 = 15
    private var data1 = 10

    // Distance Aware
    private var lastLocation: Location? = null

    // Sleep Aware
    private var lastMovement = 0L

    override fun onBind(intent: Intent): IBinder? {
        // We don't provide binding, so return null
        return null
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (ACTION_STOP_SERVICE == intent?.action) {
            stopService()
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent != null) {
            type = intent.extras?.getString("TYPE")!!
            data0 = intent.extras?.getInt("DATA0")!!
            data1 = intent.extras?.getInt("DATA1")!!

            val action = intent.action
            when (action) {
                Actions.Actions.START.name -> startService()
                Actions.Actions.STOP.name -> stopService()
            }
        } else {
            Log.d(
                "onStartCommand",
                "with a null intent. It has been probably restarted by the system."
            )
        }
        // by returning this we make sure the service is restarted if the system kills the service
        return START_STICKY
    }

    @SuppressLint("HardwareIds")
    override fun onCreate() {
        super.onCreate()
        startForeground(1, createNotification())
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        userID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun startService() {
        if (isServiceStarted) return
        isServiceStarted = true
        setServiceState(this, ServiceState.STARTED)

        // we need this lock so our service gets not affected by Doze Mode
        wakeLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
                newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Service1::lock").apply {
                    acquire(10 * 60 * 1000L /*10 minutes*/)
                }
            }

        if (type == ServiceType.ServiceType.SLEEP_AWARE.name || type == ServiceType.ServiceType.SLEEP_AWARE_MOTION.name) {
            val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
            if (type == ServiceType.ServiceType.SLEEP_AWARE.name) {
                val aSensor: Sensor? =
                    sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
                if (aSensor == null) {
                    Toast.makeText(this, "LINEAR_ACCELERATION not supported", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val eHandler = object : SensorEventListener {
                        override fun onSensorChanged(event: SensorEvent?) {
                            // https://stackoverflow.com/a/14574992/5605489
                            val x = event?.values!![0]
                            val y = event.values[1]
                            val z = event.values[2]

                            val mAccelCurrent: Float = sqrt(x * x + y * y + z * z)
                            if (mAccelCurrent > 6) {
                                lastMovement = System.currentTimeMillis()
                                Log.d("onSensorChanged", mAccelCurrent.toString())
                            }
                        }

                        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                        }
                    }
                    sensorManager.registerListener(
                        eHandler,
                        aSensor,
                        SensorManager.SENSOR_DELAY_UI
                    )
                }
            } else if (type == ServiceType.ServiceType.SLEEP_AWARE_MOTION.name) {
                // https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-significant
                /*
                The significant motion sensor triggers an event each time significant motion is detected
                and then it disables itself. A significant motion is a motion that might lead to a change
                in the user's location; for example walking, biking, or sitting in a moving car.
                 */
                val mSensor: Sensor? =
                    sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION)

                if (mSensor == null) {
                    Toast.makeText(this, "SIGNIFICANT_MOTION not supported", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    val triggerEventListener = object : TriggerEventListener() {
                        override fun onTrigger(event: TriggerEvent?) {
                            lastMovement = System.currentTimeMillis()
                            sensorManager.requestTriggerSensor(this, mSensor)
                        }
                    }
                    mSensor.also { sensor ->
                        sensorManager.requestTriggerSensor(triggerEventListener, sensor)
                    }
                }
            }
        }

        GlobalScope.launch(Dispatchers.IO) {
            while (isServiceStarted) {
                val x = when (type) {
                    ServiceType.ServiceType.PERIODIC.name -> periodic()
                    ServiceType.ServiceType.DISTANCE.name -> distance()
                    ServiceType.ServiceType.SPEED.name -> distance()
                    ServiceType.ServiceType.SLEEP_AWARE.name -> sleepAware()
                    ServiceType.ServiceType.SLEEP_AWARE_MOTION.name -> sleepAware()
                    else -> stopService()
                }

                val delayTime: Long =
                    // data0 = Sleep Time
                    if (type != ServiceType.ServiceType.DISTANCE.name) data0.toLong()
                    // SPEED Awware: data0 = Max Speed filter: data1 = Distance filter
                    else ((data1.toLong() / 1000) / data0.toLong()) * 3600
                if (x) delay(delayTime * 1000)
                else delay(1 * 1000)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    private fun periodic(): Boolean {
        Log.d("periodic()", "call")
        locationManager!!.getCurrentLocation(
            LocationManager.GPS_PROVIDER,
            cancelService,
            mainExecutor,
            { l ->
                fixCount++
                sendToHttp(l)
            }
        )
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    private fun distance(): Boolean {
        Log.d("distance()", "call")
        locationManager!!.getCurrentLocation(
            LocationManager.GPS_PROVIDER,
            cancelService,
            mainExecutor,
            { l ->
                fixCount++
                if (lastLocation != null) Log.d(
                    "distance()",
                    "distance to last messurement is " + l.distanceTo(
                        lastLocation
                    )
                )
                if (lastLocation == null) {
                    lastLocation = l
                    sendToHttp(l)
                } else if (l.distanceTo(lastLocation) >= data1) {
                    lastLocation = l
                    sendToHttp(l)
                }
            }
        )
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("MissingPermission")
    private fun sleepAware(): Boolean {
        Log.d("sleepAware()", "call")
        if (System.currentTimeMillis() - lastMovement > 20000) return false
        locationManager!!.getCurrentLocation(
            LocationManager.GPS_PROVIDER,
            cancelService,
            mainExecutor,
            { l ->
                fixCount++
                if (lastLocation != null) Log.d(
                    "sleepAware()",
                    "distance to last messurement is " + l.distanceTo(
                        lastLocation
                    )
                )
                if (lastLocation == null) {
                    lastLocation = l
                    sendToHttp(l)
                } else if (l.distanceTo(lastLocation) >= data1) {
                    lastLocation = l
                    sendToHttp(l)
                }
            }
        )
        return true
    }

    private fun sendToHttp(loc: Location?) {
        val client = OkHttpClient()
        val jsonData = JSONObject()
        if (loc != null) {
            jsonData.put("lat", loc.latitude)
            jsonData.put("lng", loc.longitude)
            jsonData.put("accuracy", loc.accuracy)
            jsonData.put("bearing", loc.bearing)
            jsonData.put("speed", loc.speed)
            jsonData.put("provider", loc.provider)
            if (loc.extras.get("satellites") != null) jsonData.put(
                "satellites",
                loc.extras.getInt("satellites")
            )
            jsonData.put("gpsfix", fixCount)
        }
        val mediaTypeJson = "application/json; charset=utf-8".toMediaType()
        val request =
            Request.Builder().url("https://api.sensormap.ga/haewo/$userID/$type")
                .post(jsonData.toString().toRequestBody(mediaTypeJson))
                .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                Log.d("sendToHttp()", "Status: " + response.code)
                if (!response.isSuccessful) throw IOException("http error $response")
                response.close()
            }
        })
    }

    private fun stopService(): Boolean {
        cancelService.cancel()
        try {
            wakeLock?.let {
                if (it.isHeld) {
                    it.release()
                }
            }
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            Log.d("stopService()", "Service stopped without being started: ${e.message}")
        }
        isServiceStarted = false
        setServiceState(this, ServiceState.STOPPED)
        return true
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "Hae!Wo?_SERVICE_CHANNEL"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            notificationChannelId, "Hae!Wo? Service", NotificationManager.IMPORTANCE_LOW
        ).let {
            it.description = "Hae!Wo? Service Channel"
            it
        }
        notificationManager.createNotificationChannel(channel)

        val stopSelf = Intent(this, Service1::class.java)
        stopSelf.action = ACTION_STOP_SERVICE
        val closeIntent: PendingIntent = PendingIntent.getService(
            this,
            0,
            stopSelf,
            PendingIntent.FLAG_CANCEL_CURRENT
        )

        val closeAction: Notification.Action =
            Notification.Action.Builder(R.drawable.x_icon, "Beenden", closeIntent).build()

        val openIntent: PendingIntent =
            Intent(this, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(this, 0, notificationIntent, 0)
            }

        val builder: Notification.Builder =
            Notification.Builder(
                this,
                notificationChannelId
            )

        return builder
            .setContentTitle("Hae!Wo?")
            .setContentText("Service läuft im Hintergrund")
            .setContentIntent(openIntent)
            // .setSmallIcon(R.drawable.add_location)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            // .setColor(resources.getColor(R.color.ic_new_background))
            .setTicker("Ticker text")
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(closeAction)
            .build()
    }
}
