package pl.peth.datacollector.api

import android.content.Context
import android.provider.Settings
import android.util.Log
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import ru.gildor.coroutines.okhttp.await
import java.io.IOException
import java.lang.Exception
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import ru.gildor.coroutines.okhttp.await
import java.net.SocketTimeoutException

class APIHandler {
    private lateinit var API_ADDRESS: String
    private lateinit var API_PORT: String

    private lateinit var context: Context
    public lateinit var uniqueID: String
    public var connection: Boolean = false
    private var lastUpdate: Long = System.currentTimeMillis()

    constructor(context: Context) {
        API_PORT = "3000"
        API_ADDRESS = "http://192.168.0.158:%s".format(API_PORT)
        this.context = context
        prepareAPI()
    }

    fun prepareAPI() {
        uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
        val data: HashMap<String, String> = hashMapOf("deviceid" to uniqueID)

        postData("device", data)
        connection = true
    }

    private suspend fun initRequest(data: String, targetAddr: String, methodType: String): Response? {
        var result: Response? = null

        if (connection) {
            val client: OkHttpClient = OkHttpClient().newBuilder().build()
            val mediaType: MediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()!!
            var body: RequestBody? = null

            if (methodType == "POST") {
                body = RequestBody.create(mediaType, data)
            }

            val request: Request = Request.Builder()
                .url(targetAddr)
                .method(methodType, body)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build()

            try {
                result = client.newCall(request).await()
            } catch (e: SocketTimeoutException) {
                connection = false
            }
        } else {
            val now: Long = System.currentTimeMillis()
            if (now - lastUpdate > 5000) {

                prepareAPI()
                lastUpdate = now
            }
        }
        return result
    }

    fun getData(dest: String, id: String) {
        val target: String = "${API_ADDRESS}/${dest}/${id}"
        GlobalScope.launch { val response: Response? = initRequest("", target, "GET") }
    }

    fun postData(dest: String, data: HashMap<String, String>) {
        val target: String = "${API_ADDRESS}/${dest}"
        var content: String = ""
        data.forEach { key, value ->
            content = content + "%s=%s&".format(key, value)
        }
        if (content.isNotEmpty()) {
            GlobalScope.launch { val response: Response? = initRequest(content.dropLast(1), target, "POST") }
        }
    }
}