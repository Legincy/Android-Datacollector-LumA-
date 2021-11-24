package pl.peth.datacollector.ui.bottomNav

import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import pl.peth.datacollector.R
import pl.peth.datacollector.databinding.PositionFragmentBinding
import pl.peth.datacollector.position.PositionManager
import pl.peth.datacollector.ui.MainActivity

class PositionFragment : Fragment(){
    private var positionManager: PositionManager? = null;
    private var binding: PositionFragmentBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val pm: PositionManager = MainActivity.positionManager;
        pm.setUp();

        binding = PositionFragmentBinding.inflate(layoutInflater)
            .apply {
                lifecycleOwner = this@PositionFragment
                viewModel = SensorFragmentViewModel()
            }

        setupDropDowns()
        return binding?.root;
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
            when(posModeId){
                0L -> {
                    when(id){
                        0L -> { lmMode = LocationManager.NETWORK_PROVIDER }
                        1L -> { lmMode = LocationManager.GPS_PROVIDER }
                    }
                }
                1L -> { null }
            }

            if(lmMode != null){
                positionManager?.update(lmMode!!, 0L, 0f);
            }
        }
    }
}