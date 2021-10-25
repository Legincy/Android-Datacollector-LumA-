package pl.peth.praktikum1

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    //Button
    private lateinit var btnStop: Button

    //Spinner
    private lateinit var spinnerSensor: Spinner
    private lateinit var spinnerAccuracy: Spinner

    //TextView
    private lateinit var tvRaw: TextView

    //Manager
    private lateinit var sensorManager: SensorManager

    //Listener
    private lateinit var sensorListener: SensorEventListener

    //Variables
    private var UI_SELECTED_ACCURACY: Int = SensorManager.SENSOR_DELAY_FASTEST
    private var UI_SELECTED_SENSOR: Any = Sensor.TYPE_ACCELEROMETER


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initComponents()

    }

    private fun updateSensorManager(){
        var sensorType: Int? = null
        var delay = UI_SELECTED_ACCURACY
        when(UI_SELECTED_SENSOR){
            "Beschleunigungssensor" -> sensorType = Sensor.TYPE_ACCELEROMETER
            "Gyroskop" -> sensorType = Sensor.TYPE_GYROSCOPE
            "Lichtsensor" -> sensorType= Sensor.TYPE_LIGHT
            "Annäherungssensor" -> sensorType= Sensor.TYPE_PROXIMITY
        }

        sensorManager.unregisterListener(sensorListener)
        if(sensorType != null){
            if(UI_SELECTED_ACCURACY < 0){
                when(UI_SELECTED_ACCURACY){
                    -1 -> delay = 1000000
                    -2 -> {
                        sensorManager.unregisterListener(sensorListener)
                        tvRaw.text = ""
                    }
                }
            }
            sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(sensorType.toString().toInt()), delay)
        }
    }

    private fun updateAccuracy(acc: Any){
        when(acc){
            "Schnell" -> UI_SELECTED_ACCURACY = SensorManager.SENSOR_DELAY_FASTEST
            "Normal" -> UI_SELECTED_ACCURACY = SensorManager.SENSOR_DELAY_NORMAL
            "Langsam" -> UI_SELECTED_ACCURACY = -1
            "Stop" -> UI_SELECTED_ACCURACY = -2
        }
    }

    private fun initComponents(){
        //Button
        btnStop = findViewById(R.id.btnStop)
        btnStop.setOnClickListener {

        }

        //Spinner
        spinnerSensor = findViewById(R.id.spinnerSensor)
        spinnerSensor?.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                UI_SELECTED_SENSOR = spinnerSensor.selectedItem
                updateSensorManager()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                null
            }

        }

        spinnerAccuracy = findViewById(R.id.spinnerAccuracy)
        spinnerAccuracy?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateAccuracy(spinnerAccuracy.selectedItem)
                updateSensorManager()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                null
            }

        }

        //TextView
        tvRaw = findViewById(R.id.tvRaw)

        //Manager
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        //Listener
        sensorListener = object : SensorEventListener{
            override fun onSensorChanged(event: SensorEvent?) {
                val sensorType = event?.sensor?.type
                var output: String = "null"
                when(sensorType){
                    Sensor.TYPE_ACCELEROMETER -> output = "X:\t%s\nY:\t%s\nZ:\t%s".format(event.values[0], event.values[1], event.values[2])
                    Sensor.TYPE_GYROSCOPE ->  output = "X:\t%s\nY:\t%s\nZ:\t%s".format(event.values[0], event.values[1], event.values[2])
                    Sensor.TYPE_LIGHT -> output = "\t%s lx".format(event.values[0])
                    Sensor.TYPE_PROXIMITY -> output = "\t%s".format(event.values[0])
                }
                tvRaw.text = output
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            }
        }
    }
}