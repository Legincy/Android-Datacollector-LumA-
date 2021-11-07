package pl.peth.datacollector.ui.bottomNav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.peth.datacollector.R
import pl.peth.datacollector.databinding.SensorFragmentBinding


class SensorFragment() : Fragment(), AdapterView.OnItemClickListener {

    private var binding: SensorFragmentBinding? = null
    private val sensorFragmentViewModel: SensorFragmentViewModel by viewModel()

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
        return binding?.root
    }

    private fun setupDropDowns() {
        val accuracyArray = resources.getStringArray(R.array.dropDownAccuracyItems)
        val sensorArray = resources.getStringArray(R.array.dropDownSensorItems)
        val accuracyArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.drop_down_item, accuracyArray)
        val sensorArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.drop_down_item, sensorArray)

        // binding?.accuracyDropDownText?.setAdapter(accuracyArrayAdapter)
        //binding?.sensorDropDownText?.setAdapter(sensorArrayAdapter)

        with(binding?.accuracyDropDownText) {
            this?.setAdapter(accuracyArrayAdapter)
            this?.onItemClickListener = this@SensorFragment
        }
        with(binding?.sensorDropDownText) {
            this?.setAdapter(sensorArrayAdapter)
            this?.onItemClickListener = this@SensorFragment
        }


    }

    override fun onItemClick(array: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val item = array?.getItemAtPosition(position).toString()
        if (item.equals("Normal"))
            Toast.makeText(activity, "Normal", Toast.LENGTH_SHORT).show()
        else
            Toast.makeText(activity, "else", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding = null
    }

    /**
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
    private var lastUpdate: Long = System.currentTimeMillis()

    private val apiHandler: APIHandler = pagerAdapter.apiHandler

    //View
    private lateinit var rootView: View

    //Maps
    private val sensors: HashMap<Int, String> =
    hashMapOf(  Sensor.TYPE_LIGHT to "light", Sensor.TYPE_ACCELEROMETER to "accelerometer",
    Sensor.TYPE_GYROSCOPE to "gyroscope", Sensor.TYPE_PROXIMITY to "proximity")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    // Inflate the layout for this fragment
    rootView = inflater!!.inflate(R.layout.sensor_fragment, container, false)

    if(inflater != null){
    return rootView
    }
    return null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    initComponents()
    }

    private fun updateSensorManager(sensor: String?){
    var delay = UI_SELECTED_ACCURACY
    if(sensor != null){
    when(sensor){
    "Beschleunigungssensor" -> UI_SELECTED_SENSOR = Sensor.TYPE_ACCELEROMETER
    "Gyroskop" -> UI_SELECTED_SENSOR = Sensor.TYPE_GYROSCOPE
    "Lichtsensor" -> UI_SELECTED_SENSOR = Sensor.TYPE_LIGHT
    "Annäherungssensor" -> UI_SELECTED_SENSOR = Sensor.TYPE_PROXIMITY
    }
    }

    sensorManager.unregisterListener(sensorListener)
    if(UI_SELECTED_SENSOR != sensor){
    when(UI_SELECTED_ACCURACY){
    -1 -> delay = 1000000
    -2 -> {
    sensorManager.unregisterListener(sensorListener)
    tvRaw.text = "-- STOP --"
    }
    }
    sensorManager.registerListener(sensorListener, sensorManager.getDefaultSensor(UI_SELECTED_SENSOR.toString().toInt()), delay)
    }
    }

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

    private fun updateAccuracy(acc: Any){
    when(acc){
    "Schnell" -> UI_SELECTED_ACCURACY = SensorManager.SENSOR_DELAY_FASTEST
    "Normal" -> UI_SELECTED_ACCURACY = SensorManager.SENSOR_DELAY_NORMAL
    "Langsam" -> UI_SELECTED_ACCURACY = -1
    "Stop" -> UI_SELECTED_ACCURACY = -2
    }
    }

    private fun truncateNum(num: Float): Double{
    return Math.round(num * 100.0) / 100.0
    }

    private fun initComponents(){
    //Spinner
    spinnerSensor = rootView?.findViewById<Spinner>(R.id.spinnerSensor) as Spinner
    spinnerSensor?.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    updateSensorManager(spinnerSensor.selectedItem as String?)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    null
    }

    }

    spinnerAccuracy = rootView?.findViewById<Spinner>(R.id.spinnerAccuracy) as Spinner
    spinnerAccuracy?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
    updateAccuracy(spinnerAccuracy.selectedItem)
    updateSensorManager(null)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
    null
    }

    }

    //TextView
    tvRaw = rootView.findViewById<TextView>(R.id.tvRaw) as TextView

    //Manager
    sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager

    //Listener
    sensorListener = object : SensorEventListener {
    override fun onSensorChanged(event: SensorEvent?) {
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
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }
    }
    }
     **/
}