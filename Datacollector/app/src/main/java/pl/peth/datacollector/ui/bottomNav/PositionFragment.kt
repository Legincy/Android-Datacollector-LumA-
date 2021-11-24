package pl.peth.datacollector.ui.bottomNav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import pl.peth.datacollector.databinding.PositionFragmentBinding

class PositionFragment : Fragment() {
    var binding: PositionFragmentBinding? = null
    private val positionViewModel: PositionFragmentViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = PositionFragmentBinding.inflate(layoutInflater).apply {
            lifecycleOwner = this@PositionFragment
            viewModel = positionViewModel
        }
        observeViewModel()
        return binding?.root
    }

    private fun observeViewModel() {
        positionViewModel.getLocationLiveData.observe(this, {
            positionViewModel.latitude.value = it.latitude
            positionViewModel.longitude.value = it.longitude
        })
    }
}