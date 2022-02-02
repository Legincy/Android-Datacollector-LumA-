package pl.peth.datacollector.ui.bottomNav

import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import pl.peth.datacollector.Constants.ACTION_START_OR_RESUME_SERVICE
import pl.peth.datacollector.Constants.ACTION_STOP_SERVICE
import pl.peth.datacollector.R
import pl.peth.datacollector.TrackingService
import pl.peth.datacollector.api.APIHandler
import pl.peth.datacollector.databinding.PositionFragmentBinding
import pl.peth.datacollector.position.PositionManager
import pl.peth.datacollector.ui.MainActivity

class PositionFragment : Fragment(), OnMapReadyCallback {
    private var positionManager: PositionManager? = null
    private var binding: PositionFragmentBinding? = null
    private val API_DELAY: Int = 5; // Send every 5 seconds an update to endpoint
    private var API_LAST_UPDATE: Long = System.currentTimeMillis()
    private val apiHandler: APIHandler = MainActivity.apiHandler
    private var routeId: Int? = null
    private var longitude: Double? = null
    private var latitude: Double? = null
    private var lastLocation = Location("dummyProvider")
    private var firstPoint = true
    private lateinit var mMap: GoogleMap
    private lateinit var mapFragment: SupportMapFragment
    private var isTracking = false
    private var strategy = ""
    private var sliderValue = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        positionManager = MainActivity.positionManager
        positionManager!!.setUp()

        binding = PositionFragmentBinding.inflate(layoutInflater)
            .apply {
                lifecycleOwner = this@PositionFragment
                viewModel = SensorFragmentViewModel()
            }
        setupDropDowns()
        setupButtons()
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?)!!
        setUpCallBack()
    }

    private fun setUpCallBack() {
        val callback = OnMapReadyCallback { googleMap ->
            val firstPoint = LatLng(51.42779, 6.88152)
            googleMap.addMarker(MarkerOptions().position(firstPoint).title(""))
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(firstPoint))
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        firstPoint.latitude,
                        firstPoint.longitude
                    ),
                    19f
                )
            )
            onMapReady(googleMap)
        }
        mapFragment?.getMapAsync(callback)
    }

    private fun createNewRoute() {
        GlobalScope.launch {
            var res = apiHandler.postData("route/get", null)

            if (res != null) {
                var json = JSONObject(res?.body?.string())
                println(json)
                routeId = json.getString("routeid").toInt()
                positionManager?.update(null, null, routeId!!)
            }
            res?.close()
        }
    }

    private fun setupButtons() {
        val btnSnap = binding?.btnSnap
        val btnNewRoute = binding?.btnNewRoute

        btnNewRoute?.setOnClickListener {
            createNewRoute()
            Toast.makeText(activity, "$routeId", Toast.LENGTH_SHORT).show()
            setUpCallBack()
        }

        btnSnap?.setOnClickListener {
            positionManager?.setMarked()
            longitude = positionManager?.longitude ?: 0.0
            latitude = positionManager?.latitude ?: 0.0
            Toast.makeText(activity, "$latitude  $longitude", Toast.LENGTH_SHORT).show()
            addRedCircle()
        }

        binding?.trackMe?.setOnClickListener {
            if (isTracking) {
                sendCommandToService(ACTION_STOP_SERVICE, PRIORITY_HIGH_ACCURACY)
                isTracking = false
                binding?.trackMe?.text = "Track Me"
            } else {
                sendCommandToService(ACTION_START_OR_RESUME_SERVICE, PRIORITY_HIGH_ACCURACY)
                isTracking = true
                binding?.trackMe?.text = "Stop Tracking"
            }
        }
    }

    private fun setupDropDowns() {
        val positionTechArray = resources.getStringArray(R.array.dropDownPositionTechnologies)
        val positionTechAdapter =
            ArrayAdapter(requireContext(), R.layout.drop_down_item, positionTechArray)

        val strategyArray = resources.getStringArray(R.array.dropDownStrategy)
        val strategyAdapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, strategyArray)

        binding?.positionTechDropDownText?.setAdapter(positionTechAdapter)
        binding?.strategyText?.setAdapter(strategyAdapter)

        binding?.positionTechDropDownText?.setOnItemClickListener { parent, view, position, id ->
            updateModeDropDown(id)
        }
        binding?.strategyText?.setOnItemClickListener { parent, view, position, id ->
            setStrategy(id)
        }
    }

    private fun updateModeDropDown(posModeId: Long) {
        // 0=Location Manager || 1=Fused Location Provider
        var positionModeTechArray: Array<out String>? = null
        var positionModeAdapter: ArrayAdapter<String>? = null
        var lmMode: String? = null

        when (posModeId) {
            0L -> {
                positionModeTechArray = resources.getStringArray(R.array.dropDownPositionModeLM)
                positionModeAdapter =
                    ArrayAdapter(requireContext(), R.layout.drop_down_item, positionModeTechArray)
            }
            1L -> {
                positionModeTechArray = resources.getStringArray(R.array.dropDownPositionModeFLP)
                positionModeAdapter =
                    ArrayAdapter(requireContext(), R.layout.drop_down_item, positionModeTechArray)
            }
        }

        binding?.positionModeDropDownText?.setAdapter(positionModeAdapter)

        binding?.positionModeDropDownText?.setOnItemClickListener { parent, view, position, id ->
            /*  -- POS-MODE-ID
                0: Location Manager
                1: FusedLocationProvider
            */
            positionManager?.update(posModeId, id, null)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        for (location in route) {
            mMap.addCircle(
                CircleOptions().center(
                    LatLng(
                        location.latitude,
                        location.longitude
                    )
                )
                    .radius(1.0)
                    .strokeColor(Color.GRAY)
                    .fillColor(Color.GRAY)
            )
        }
    }

    companion object {
        private val route = arrayOf(
            LatLng(51.42779, 6.88152),
            LatLng(51.42844, 6.88298),
            LatLng(51.42894, 6.88485),
            LatLng(51.42953, 6.88671),
            LatLng(51.43044, 6.88661),
            LatLng(51.43176, 6.88676),
            LatLng(51.43183, 6.8886),
            LatLng(51.43321, 6.88873),
            LatLng(51.427273, 6.880135),
            LatLng(51.426372, 6.881506)
        )
    }

    fun addRedCircle() {

        if (latitude != lastLocation.latitude) {
            mMap.addCircle(
                CircleOptions().center(
                    LatLng(
                        latitude ?: 0.0,
                        longitude ?: 0.0
                    )
                )
                    .radius(1.0)
                    .strokeColor(Color.RED)
                    .fillColor(Color.RED)
            )
            if (!firstPoint) {
                mMap.addPolyline(
                    PolylineOptions().add(
                        LatLng(
                            lastLocation.latitude,
                            lastLocation.longitude
                        )
                    ).add(
                        LatLng(
                            latitude ?: 0.0,
                            longitude ?: 0.0
                        )
                    )
                        .color(Color.RED)
                )
            }
        }

        firstPoint = false
        lastLocation.latitude = latitude ?: 0.0
        lastLocation.longitude = longitude ?: 0.0

        mMap.moveCamera(
            CameraUpdateFactory.newLatLng(
                LatLng(
                    latitude ?: 0.0,
                    longitude ?: 0.0
                )
            )
        )
    }

    private fun setStrategy(id: Long) {
        binding?.slider?.stepSize = 1f
        binding?.slider?.valueFrom = 0f

        when (id) {
            0L -> {
                strategy = "Zeit"
                binding?.slider?.value = 0f
                binding?.slider?.valueTo = 10f
            }
            1L -> {
                strategy = "Abstand"
                binding?.slider?.value = 0f
                binding?.slider?.valueTo = 100f
            }
            2L -> {
                strategy = "Geschwindichkeit"
                binding?.slider?.value = 0f
                binding?.slider?.valueTo = 240f
            }
            3L -> {
                strategy = "Bewegung"
                binding?.slider?.value = 0f
                binding?.slider?.valueTo = 240f
            }
        }
    }

    private fun test() {
        sliderValue = binding?.slider?.value ?: 0f
        Toast.makeText(activity, "$strategy $sliderValue $routeId", Toast.LENGTH_SHORT)
            .show()
    }

    private fun sendCommandToService(action: String, accuracy: Int) {
        sliderValue = binding?.slider?.value ?: 0f
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            it.putExtra("accuracy", accuracy)
            it.putExtra("strategy", strategy)
            it.putExtra("sliderValue", sliderValue)
            it.putExtra("routeId", routeId)
            requireContext().startService(it)
        }
    }
}
