package pl.peth.datacollector.ui.bottomNav

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import pl.peth.datacollector.ui.LocationLiveData

class PositionFragmentViewModel(context: Context) : ViewModel() {

    private val locationLiveDate : LocationLiveData = LocationLiveData(context = context)
    val getLocationLiveData = locationLiveDate
    var latitude = MutableLiveData<String>()
    var longitude = MutableLiveData<String>()
}