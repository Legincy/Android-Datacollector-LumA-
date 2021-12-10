package pl.peth.datacollector.ui.bottomNav

import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.Response
import org.json.JSONObject
import org.koin.core.component.getScopeId
import pl.peth.datacollector.R
import pl.peth.datacollector.api.APIHandler
import pl.peth.datacollector.databinding.PositionFragmentBinding
import pl.peth.datacollector.position.PositionManager
import pl.peth.datacollector.ui.MainActivity

class PositionFragment : Fragment(){
    private var positionManager: PositionManager? = null;
    private var binding: PositionFragmentBinding? = null;
    private val API_DELAY: Int = 5; //Send every 5 seconds an update to endpoint
    private var API_LAST_UPDATE: Long = System.currentTimeMillis();
    private val apiHandler: APIHandler = MainActivity.apiHandler;
    private var routeId: Int? = null;

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        positionManager = MainActivity.positionManager;
        positionManager!!.setUp()

        binding = PositionFragmentBinding.inflate(layoutInflater)
            .apply {
                lifecycleOwner = this@PositionFragment
                viewModel = SensorFragmentViewModel()
            }

        setupDropDowns()
        setupButtons()
        return binding?.root;
    }

    private fun createNewRoute() {
        GlobalScope.launch {
            var res = apiHandler.postData("route/get", null)

            var json = JSONObject(res?.body?.string())
            routeId = json.getString("routeid").toInt()
        }
    }

    private fun setupButtons(){
        val btnSnap = binding?.btnSnap;
        val btnNewRoute = binding?.btnNewRoute;

        btnNewRoute?.setOnClickListener {
            createNewRoute();
        }

        btnSnap?.setOnClickListener {
            positionManager?.setMarked();
        }
    }

    private fun setupDropDowns(){
        val positionTechArray = resources.getStringArray(R.array.dropDownPositionTechnologies)
        val positionTechAdapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, positionTechArray)

        binding?.positionTechDropDownText?.setAdapter(positionTechAdapter)

        binding?.positionTechDropDownText?.setOnItemClickListener { parent, view, position, id ->
            updateModeDropDown(id)
        }
    }

    private fun updateModeDropDown(posModeId: Long){
        //0=Location Manager || 1=Fused Location Provider
        var positionModeTechArray: Array<out String>? = null;
        var positionModeAdapter: ArrayAdapter<String>? = null;
        var lmMode: String? = null;

        when(posModeId){
            0L -> {
                positionModeTechArray = resources.getStringArray(R.array.dropDownPositionModeLM)
                positionModeAdapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, positionModeTechArray)
            }
            1L -> {
                positionModeTechArray = resources.getStringArray(R.array.dropDownPositionModeFLP)
                positionModeAdapter = ArrayAdapter(requireContext(), R.layout.drop_down_item, positionModeTechArray)
            }
        }

        binding?.positionModeDropDownText?.setAdapter(positionModeAdapter)

        binding?.positionModeDropDownText?.setOnItemClickListener { parent, view, position, id ->
            /*  -- POS-MODE-ID
                0: Location Manager
                1: FusedLocationProvider
            */
            if(routeId != null){
                positionManager?.update(posModeId, id, routeId!!);
            }
        }
    }
}