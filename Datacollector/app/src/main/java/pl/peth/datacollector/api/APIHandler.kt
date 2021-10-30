package pl.peth.datacollector.api

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException


class APIHandler {
    private lateinit var API_ADDRESS: String
    private lateinit var API_PORT: String

    constructor(){
        API_PORT = "3000"
        API_ADDRESS = "http://192.168.0.159:%s".format(API_PORT)
    }

    private fun initRequest(data: String, targetAddr: String, methodType: String){
        val client: OkHttpClient = OkHttpClient().newBuilder().build()
        val mediaType: MediaType = "application/x-www-form-urlencoded".toMediaTypeOrNull()!!
        val body: RequestBody = RequestBody.create(mediaType, data)
        val request: Request = Request.Builder()
            .url(targetAddr)
            .method(methodType, body)
            .addHeader("Content-Type", "application/x-www-form-urlencoded")
            .build()
        client.newCall(request).enqueue(object : Callback{
            override fun onFailure(call: Call, e: IOException) {
                println("[API-Err]: %s - %s".format(e, call))
            }

            override fun onResponse(call: Call, response: Response) {
                println("[API-Resp]: %s - %s".format(response, call))
            }
        })
    }

    public fun postData(dest: String, data: HashMap<String, String>){
        val target: String = API_ADDRESS + "/%s".format(dest)
        var content: String = ""
        data.forEach { key, value ->
            content = content + "%s=%s&".format(key, value)
        }
        if(content.isNotEmpty()){
            initRequest(content.dropLast(1), target, "POST")
        }
    }

}