package com.sap.scan2input.service

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import com.sap.cloud.mobile.foundation.model.AppConfig
import com.sap.cloud.android.odata.espmcontainer.ESPMContainer
import com.sap.cloud.mobile.foundation.common.ClientProvider
import com.sap.cloud.mobile.foundation.common.SettingsProvider
import com.sap.cloud.mobile.kotlin.odata.OnlineODataProvider
import com.sap.cloud.mobile.kotlin.odata.http.OKHttpHandler
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.IOException

object SAPServiceManager {
	
    private const val CONNECTION_ID_ESPMCONTAINER: String = "com.sap.edm.sampleservice.v4"
    private const val CONNECTION_ID_DOX_DESTINATION: String = "dox.service.destination" //Please change this if you have used a different destination name
	
	private lateinit var appConfig: AppConfig
	fun initSAPServiceManager(config: AppConfig) {
	        appConfig = config
	}

    var serviceRoot: String = ""
        private set
        get() {
            return (eSPMContainer?.provider as OnlineODataProvider).serviceRoot
        }

    var eSPMContainer: ESPMContainer? = null
        private set
        get() {
            return field ?: throw IllegalStateException("SAPServiceManager was not initialized")
        }

    fun openODataStore(callback: () -> Unit = {}) {
		appConfig.serviceUrl.let { _serviceURL ->
		    eSPMContainer = ESPMContainer (
		        OnlineODataProvider("SAPService", _serviceURL + CONNECTION_ID_ESPMCONTAINER).apply {
		            networkOptions.httpHandler = OKHttpHandler(ClientProvider.get())
		            serviceOptions.checkVersion = false
		            serviceOptions.requiresType = true
		            serviceOptions.cacheMetadata = false
		        }
		    )
		}
        callback.invoke()
    }

    fun uploadDocToDoxService(capturedImage: File?, callback: (String?) -> Unit = { _ -> }) {

        // Real apps should write logic to dynamically fetch the following values
        val clientId = "default"
        val documentType = "invoice" // Pass this information only if you used invoice while creating a schema
        val schemaID = "<Your-Schema-ID>"

        // Create options JSON payload
        val options = JSONObject().apply {
            put("clientId", clientId)
            put("schemaId", schemaID)
        }

        // Handle the case where capturedImage is null
        val file = capturedImage ?: return // Handle the case where capturedImage is null

        val fileExtension = file.extension.lowercase()
        val mediaType = when (fileExtension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            else -> {
                Log.e("API Request", "Unsupported file type: $fileExtension")
                callback("Unsupported file type")
                return
            }
        }.toMediaTypeOrNull()

        val serverUrl = SettingsProvider.get().backendUrl
        val destinationName = CONNECTION_ID_DOX_DESTINATION
        val documentJobsPath = "/document/jobs"
        val finalUrl = "$serverUrl/$destinationName$documentJobsPath"

        // Create multipart request body
        val requestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                file.name, // Use actual file extension here
                file.asRequestBody(mediaType)
            )
            .addFormDataPart("options", options.toString())
            .build()

        val request = Request.Builder()
            .url(finalUrl)
            .post(requestBody)
            .build()

        ClientProvider.get().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API Response", "Request failed: ${e.message}")
                callback(null)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        Log.e("API Response", "Unexpected code $res")
                        callback(null)
                    } else {
                        val responseBody = res.body?.string()
                        callback(responseBody)
                    }
                }
            }
        })
    }

    fun getDocWithIdFromDoxService(documentID: String, callback: (String?) -> Unit = { _ -> }) {
        val serverUrl = SettingsProvider.get().backendUrl
        val destinationName = CONNECTION_ID_DOX_DESTINATION
        val allDocumentsPath = "/document/jobs/"
        val finalUrl = "$serverUrl/$destinationName/$allDocumentsPath$documentID"

        val request = Request.Builder()
            .url(finalUrl)
            .build()

        ClientProvider.get().newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("API Response", "Request failed: ${e.message}")
                callback(null) // or provide a meaningful error message
            }

            override fun onResponse(call: Call, response: Response) {
                response.use { res ->
                    if (!res.isSuccessful) {
                        Log.e("API Response", "Unexpected code $res")
                        callback(null) // or provide a meaningful error message
                    } else {
                        val responseBody = res.body?.string()
                        Log.d("API Response", "Body: $responseBody")
                        callback(responseBody)
                    }
                }
            }
        })
    }

}
