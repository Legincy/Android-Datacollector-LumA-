package pl.peth.datacollector.ui.bottomNav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.peth.datacollector.R
import pl.peth.datacollector.databinding.SensorFragmentBinding


class SensorFragment() : Fragment() {

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
                viewModel = sensorFragmentViewModel
            }
        setUpDropDowns()
        return binding?.root
    }

    private fun setUpDropDowns() {
        val accuracyArray = resources.getStringArray(R.array.dropDownAccuracyItems)
        val sensorArray = resources.getStringArray(R.array.dropDownSensorItems)
        val accuracyArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.drop_down_item, accuracyArray)
        val sensorArrayAdapter =
            ArrayAdapter(requireContext(), R.layout.drop_down_item, sensorArray)

        binding?.accuracyDropDownText?.setAdapter(accuracyArrayAdapter)
        binding?.sensorDropDownText?.setAdapter(sensorArrayAdapter)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        sensorFragmentViewModel.sensorMenu.observe(viewLifecycleOwner, {
            sensorFragmentViewModel.i.value = 1
            sensorFragmentViewModel.onSensorUpdate()
            Toast.makeText(activity, "sensor", Toast.LENGTH_SHORT).show()
        })
        sensorFragmentViewModel.accuracyMenu.observe(viewLifecycleOwner, {
            sensorFragmentViewModel.onAccuracyUpdate()
            Toast.makeText(activity, "accuracy", Toast.LENGTH_SHORT).show()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorFragmentViewModel.unregisterSensors()
        binding = null
    }
}