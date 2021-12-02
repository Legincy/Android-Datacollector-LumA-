package pl.peth.datacollector.ui.bottomNav

import android.hardware.SensorEvent
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import pl.peth.datacollector.api.APIHandler
import pl.peth.datacollector.sensor.SensorHandler
import pl.peth.datacollector.ui.MainActivity
import java.util.jar.Manifest

class SensorFragmentViewModel : ViewModel() {
    private val API_DELAY: Int = 5 //Send every 5 seconds an update to endpoint
    private var API_LAST_UPDATE: Long = System.currentTimeMillis()
    private val apiHandler: APIHandler = MainActivity.apiHandler

    fun dispatchToAPI(event: SensorEvent?, dest: String) {
        if (apiHandler == null) {

        }

        val keyMap: HashMap<Int, String> = hashMapOf(0 to "x", 1 to "y", 2 to "z")
        var data: HashMap<String, String> = hashMapOf<String, String>()
        if (event != null) {
            data.put("deviceid", apiHandler.uniqueID)
            when (event.values.size) {
                1 -> data.put("value", event.values[0].toString())
                3 -> {
                    event.values.forEachIndexed { key, value ->
                        keyMap.get(key)
                            ?.let { data.put(it, value.toString()) }
                    }
                }
            }
        }

        val now: Long = System.currentTimeMillis()
        if (now - API_LAST_UPDATE > API_DELAY * 1000) {
            GlobalScope.launch {
                apiHandler.postData(dest, data)
            }
            API_LAST_UPDATE = now
        }
    }
}