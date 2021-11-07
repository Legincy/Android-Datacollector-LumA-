package pl.peth.datacollector.ui.bottomNav

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class SensorFragmentViewModel(application: Application) : AndroidViewModel(application),
    SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var gyroscope: Sensor
    val sensorLiveData = MutableLiveData<String>()

    init {
        setUpSensor()
    }

    private fun setUpSensor() {

        sensorManager =
            getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent?) {

        sensorLiveData.value =
            "X =" + (event?.values?.get(0) ?: "Null") +
                    "\nY =" + (event?.values?.get(1) ?: "Null") +
                    "\nZ =" + (event?.values?.get(2) ?: "Null")
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        return
    }

    fun unregisterSensors() {
        sensorManager.unregisterListener(this)
    }


}