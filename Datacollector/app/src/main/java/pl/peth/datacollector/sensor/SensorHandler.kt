package pl.peth.datacollector.sensor

import android.annotation.SuppressLint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log

class SensorHandler {
    //Manager
    private lateinit var sensorManager: SensorManager

    //Listener
    lateinit var sensorListener: SensorEventListener

    //Const
    var UI_SELECTED_SENSOR: Int = -1
    var UI_SELECTED_SENSOR_STR: String? = null
    var UI_SELECTED_ACCURACY: Int? = null

    //Var
    private var outputFunc: ((msg: SensorEvent) -> Unit?)? = null

    @SuppressLint("ServiceCast")
    constructor(sensorManager: SensorManager, outputFunc: ((event: SensorEvent) -> Unit?)?) {
        this.sensorManager = sensorManager
        this.outputFunc = outputFunc
        setUpSensorListener()
    }

    private fun setUpSensorListener(){
        this.sensorListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                val sensorType = event?.sensor?.type
                lateinit var output: String

                when (sensorType) {
                    Sensor.TYPE_ACCELEROMETER -> output = "X:\t%s\nY:\t%s\nZ:\t%s".format(
                        truncateNum(event.values[0]),
                        truncateNum(event.values[1]),
                        truncateNum(event.values[2])
                    )
                    Sensor.TYPE_GYROSCOPE -> output = "X:\t%s\nY:\t%s\nZ:\t%s".format(
                        truncateNum(event.values[0]),
                        truncateNum(event.values[1]),
                        truncateNum(event.values[2])
                    )
                    Sensor.TYPE_LIGHT -> output = "\t%s lx".format(event.values[0])
                    Sensor.TYPE_PROXIMITY -> output = "\t%s".format(event.values[0])
                }
                Log.e("SensorHandler", output.replace("\n", " | "))
                if (outputFunc != null) event?.let { outputFunc?.invoke(it) }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Log.e("Sensor", "Accuracy for ${sensor} changed to ${accuracy}")
            }
        }
    }

    private fun truncateNum(num: Float): Double{
        return Math.round(num * 100.0) / 100.0
    }

    fun updateSensor(sensor: Int){
        var delay = UI_SELECTED_ACCURACY

        when(sensor){
            Sensor.TYPE_ACCELEROMETER -> {
                UI_SELECTED_SENSOR = Sensor.TYPE_ACCELEROMETER
                UI_SELECTED_SENSOR_STR = "accelerometer"
            }
            Sensor.TYPE_GYROSCOPE ->{
                UI_SELECTED_SENSOR = Sensor.TYPE_GYROSCOPE
                UI_SELECTED_SENSOR_STR = "gyroscope"

            }
            Sensor.TYPE_LIGHT -> {
                UI_SELECTED_SENSOR = Sensor.TYPE_LIGHT
                UI_SELECTED_SENSOR_STR = "light"
            }
            Sensor.TYPE_PROXIMITY -> {
                UI_SELECTED_SENSOR = Sensor.TYPE_PROXIMITY
                UI_SELECTED_SENSOR_STR = "proximity"
            }
        }

        Log.e("SensorHandler", "" + UI_SELECTED_SENSOR + " " + UI_SELECTED_ACCURACY)

        unregisterListener()
        if(delay != null) {
            when(UI_SELECTED_ACCURACY){
                -1 -> delay = 1000000
                -2 -> unregisterListener()
                else -> sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(UI_SELECTED_SENSOR), delay)
            }
            Log.e("SensorHandler", "Register ${sensor} with delay ${delay}")
        }
    }

    fun updateAccuracy(delay: Int){
        UI_SELECTED_ACCURACY = delay
        if(UI_SELECTED_SENSOR != -1) updateSensor(UI_SELECTED_SENSOR)
    }

    fun unregisterListener(){
        sensorManager.unregisterListener(sensorListener)
    }
}