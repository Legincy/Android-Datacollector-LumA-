package pl.peth.datacollector.ui.bottomNav

import android.hardware.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.getScopeId
import org.koin.core.component.getScopeName
import pl.peth.datacollector.R
import pl.peth.datacollector.databinding.SensorFragmentBinding
import pl.peth.datacollector.sensor.SensorHandler
import pl.peth.datacollector.ui.MainActivity


class SensorFragment() : Fragment() {

    private var binding: SensorFragmentBinding? = null
    private val sensorFragmentViewModel: SensorFragmentViewModel by viewModel()
    private lateinit var sensorHandler: SensorHandler
    //private lateinit var sensorListener: SensorEventListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = SensorFragmentBinding.inflate(layoutInflater)
            .apply {
                lifecycleOwner = this@SensorFragment
                viewModel = SensorFragmentViewModel()
            }
        setupDropDowns()
        setUpSensorHandler()
        return binding?.root
    }

    private fun setUpSensorHandler(){
        sensorHandler = SensorHandler(MainActivity.sensorManager, null)
    }

    private fun setupDropDowns() {
        val accuracyArray = resources.getStringArray(R.array.dropDownAccuracyItems)
        val sensorArray = resources.getStringArray(R.array.dropDownSensorItems)
        val accuracyArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.drop_down_item, accuracyArray)
        val sensorArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.drop_down_item, sensorArray)

        binding?.accuracyDropDownText?.setAdapter(accuracyArrayAdapter)
        binding?.sensorDropDownText?.setAdapter(sensorArrayAdapter)

        binding?.sensorDropDownText?.setOnItemClickListener { parent, view, position, id ->
            var sensor: Int? = null
            when(id){
                0L -> sensor = Sensor.TYPE_ACCELEROMETER
                1L -> sensor = Sensor.TYPE_GYROSCOPE
                2L -> sensor = Sensor.TYPE_LIGHT
                3L -> sensor = Sensor.TYPE_PROXIMITY
            }

            if(sensor != null) sensorHandler.updateSensor(sensor)
        }

        binding?.accuracyDropDownText?.setOnItemClickListener { parent, view, position, id ->
            var delay: Int? = null
            when(id){
                0L -> delay = SensorManager.SENSOR_DELAY_FASTEST
                1L -> delay = SensorManager.SENSOR_DELAY_NORMAL
                2L -> delay = -1
                3L -> delay = -2
            }

            if(delay != null) sensorHandler.updateAccuracy(delay)
        }

        /*
        //Listener
        val sensorType = event?.sensor?.type
        var output: String = "null"
        when(sensorType){
            Sensor.TYPE_ACCELEROMETER -> output = "X:\t%s\nY:\t%s\nZ:\t%s".format(truncateNum(event.values[0]), truncateNum(event.values[1]), truncateNum(event.values[2]))
            Sensor.TYPE_GYROSCOPE ->  output = "X:\t%s\nY:\t%s\nZ:\t%s".format(truncateNum(event.values[0]), truncateNum(event.values[1]), truncateNum(event.values[2]))
            Sensor.TYPE_LIGHT -> output = "\t%s lx".format(event.values[0])
            Sensor.TYPE_PROXIMITY -> output = "\t%s".format(event.values[0])
        }
        dispatchToAPI(event)
        tvRaw?.text = output
        */
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }
    /**
    private fun dispatchToAPI(event: SensorEvent?){
    val keyMap: HashMap<Int, String> = hashMapOf(0 to "x", 1 to "y", 2 to "z")
    val sensorReq: String? = sensors.get(UI_SELECTED_SENSOR)
    var data: HashMap<String, String> = hashMapOf<String, String>()
    if (event != null) {
    data.put("deviceid", apiHandler.uniqueID)
    when(event.values.size){
    1 -> data.put("value", event.values[0].toString())
    3 -> { event.values.forEachIndexed { key, value -> keyMap.get(key)
    ?.let { data.put(it, value.toString()) } } }
    }
    }
    if(sensorReq != null){
    val now: Long = System.currentTimeMillis()
    if(now - lastUpdate > 1000){
    apiHandler.postData(sensorReq, data)
    lastUpdate = now
    }
    }
    }
     **/
}