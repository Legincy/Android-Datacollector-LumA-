package pl.peth.datacollector.api

import android.content.Context
import android.provider.Settings
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException


class APIHandler{
    private lateinit var API_ADDRESS: String
    private lateinit var API_PORT: String

    private lateinit var context: Context
    public lateinit var uniqueID: String

    private lateinit var getResponse: Response

    constructor(context: Context){
        API_PORT = "3000"
        API_ADDRESS = "http://192.168.0.159:%s".format(API_PORT)
        this.context = context
        prepareAPI()
    }

    public fun prepareAPI(){
        uniqueID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID)
        val data: HashMap<String, String> = hashMapOf("deviceid" to uniqueID)
        postData("device",data)
    }

    private fun initRequest(data: String, targetAddr: String, methodType: String) {
        val client: OkHttpClient = OkHttpClient().newBuilder().build()
        val mediaType: MediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()!!
        var body: RequestBody? = null

        if(methodType == "POST"){
            body = RequestBody.create(mediaType, data)
        }

        val request: Request = Request.Builder()
            .url(targetAddr)
            .method(methodType, body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API-Err", "%s - %s".format(e, call))
            }

            override fun onResponse(call: Call, response: Response) {
                Log.e("API-Resp", "%s - %s".format(response, call))
            }

        })
    }

    public fun getData(dest: String, id: String){
        val target: String =  "${API_ADDRESS}/${dest}/${id}"
        initRequest("", target, "GET")
    }

    public fun postData(dest: String, data: HashMap<String, String>){
        val target: String =  "${API_ADDRESS}/${dest}"
        var content: String = ""
        data.forEach { key, value ->
            content = content + "%s=%s&".format(key, value)
        }
        if(content.isNotEmpty()){
            initRequest(content.dropLast(1), target, "POST")
        }
    }
}