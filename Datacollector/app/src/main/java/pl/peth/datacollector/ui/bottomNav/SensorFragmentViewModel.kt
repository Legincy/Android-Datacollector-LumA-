package pl.peth.datacollector.ui.bottomNav

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import java.math.RoundingMode
import java.text.DecimalFormat

class SensorFragmentViewModel(application: Application) : AndroidViewModel(application),
    SensorEventListener {

    private var sensorManager: SensorManager
    private lateinit var sensor: Sensor
    private var sensorType: Int = Sensor.TYPE_ACCELEROMETER
    private var accuracy: Int = SensorManager.SENSOR_DELAY_NORMAL
    val sensorLiveData = MutableLiveData<String>()
    val sensorMenu = MutableLiveData<String>()
    val accuracyMenu = MutableLiveData<String>()
    var valid = MutableLiveData<Boolean>()


    init {
        sensorManager =
            getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    private fun setUpSensor() {

        sensorManager.unregisterListener(this)
        sensor = sensorManager.getDefaultSensor(sensorType)
        sensorManager.registerListener(this, sensor, accuracy)
    }

    fun onSensorUpdate() {
        when (sensorMenu.value) {
            "ACCELEROMETER" -> sensorType = Sensor.TYPE_ACCELEROMETER
            "GYROSCOPE" -> sensorType = Sensor.TYPE_GYROSCOPE
            "TYPE_LIGHT" -> sensorType = Sensor.TYPE_LIGHT
            "TYPE_PROXIMITY" -> sensorType = Sensor.TYPE_PROXIMITY
        }
        setUpSensor()
    }

    fun onAccuracyUpdate() {
        when (accuracyMenu.value) {
            "Fast" -> accuracy = SensorManager.SENSOR_DELAY_FASTEST
            "Normal" -> accuracy = SensorManager.SENSOR_DELAY_FASTEST
            "slow" -> accuracy = 1000000
            "Stop" -> {
                unregisterSensors()
                return
            }
        }
        setUpSensor()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (valid.value == null || accuracyMenu.value == "Stop") {
            unregisterSensors()
            return
        }

        if (sensorType == Sensor.TYPE_LIGHT || sensorType == Sensor.TYPE_PROXIMITY)
            sensorLiveData.value =
                sensorMenu.value +
                        "\nX = " + (event?.values?.get(0))
        else {
            sensorLiveData.value =
                sensorMenu.value +
                        "\nX =" + (round(number = event?.values?.get(0))) +
                        "\nY =" + (round(number = event?.values?.get(1))) +
                        "\nZ =" + (round(number = event?.values?.get(2))) +
                        "\n" + accuracyMenu.value
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    fun unregisterSensors() {
        sensorManager.unregisterListener(this)
        sensorLiveData.value = "Stopped"
    }

    private fun round(number: Float?): Double {
        val df = DecimalFormat("#.##")
        df.roundingMode = RoundingMode.FLOOR
        return df.format(number).toDouble()
    }
}