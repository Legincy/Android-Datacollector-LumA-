package pl.peth.datacollector.ui.bottomNav

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.peth.datacollector.R
import pl.peth.datacollector.databinding.SensorFragmentBinding
import pl.peth.datacollector.graph.GraphManager
import pl.peth.datacollector.sensor.SensorHandler
import pl.peth.datacollector.ui.MainActivity
import java.util.*


class SensorFragment() : Fragment() {

    private var binding: SensorFragmentBinding? = null
    private val sensorFragmentViewModel: SensorFragmentViewModel by viewModel()

    lateinit var graphManager: GraphManager
    var lgs: LineGraphSeries<DataPoint> = LineGraphSeries()
    var counter: Double = 1.0

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
        setupGraph()
        setUpSensorHandler()
        return binding?.root
    }

    private fun setUpSensorHandler() {
        sensorHandler = SensorHandler(MainActivity.sensorManager, this::handleSensorData)
    }

    private fun handleSensorData(event: SensorEvent) {
        if(sensorHandler.UI_SELECTED_SENSOR_STR == null) return
        sensorFragmentViewModel.dispatchToAPI(event, sensorHandler.UI_SELECTED_SENSOR_STR!!)
        graphManager.addData(sensorHandler.UI_SELECTED_SENSOR, event)
    }

    private fun setupGraph(){
        if(binding?.graphSensor != null){
            graphManager = GraphManager(binding?.graphSensor!!)
        }
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
            when (id) {
                0L -> sensor = Sensor.TYPE_ACCELEROMETER
                1L -> sensor = Sensor.TYPE_GYROSCOPE
                2L -> sensor = Sensor.TYPE_LIGHT
                3L -> sensor = Sensor.TYPE_PROXIMITY
            }

            if (sensor != null) {
                sensorHandler.updateSensor(sensor)
                graphManager.loadSeries(sensor)
            }
        }

        binding?.accuracyDropDownText?.setOnItemClickListener { parent, view, position, id ->
            var delay: Int? = null
            when (id) {
                0L -> delay = SensorManager.SENSOR_DELAY_FASTEST
                1L -> delay = SensorManager.SENSOR_DELAY_NORMAL
                2L -> delay = -1
                3L -> delay = -2
            }

            if (delay != null) sensorHandler.updateAccuracy(delay)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    companion object {
        fun buildIntent(context: Context) = Intent(context, SensorFragment::class.java)
        lateinit var sensorHandler: SensorHandler
    }
}