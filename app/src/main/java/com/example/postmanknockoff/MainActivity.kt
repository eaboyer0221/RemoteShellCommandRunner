package com.example.postmanknockoff


import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var urlEditText: EditText
    private lateinit var bodyEditText: EditText
    private lateinit var contentTypeEditText: EditText
    private lateinit var portEditText: EditText
    private lateinit var endpointEditText: EditText
    private lateinit var sendButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        endpointEditText = findViewById(R.id.endpointEditText)
        urlEditText = findViewById(R.id.urlEditText)
        bodyEditText = findViewById(R.id.bodyEditText)
        contentTypeEditText = findViewById(R.id.contentTypeEditText)
        portEditText = findViewById(R.id.portEditText)


        endpointEditText.setText("api/run-shell-command")
        urlEditText.setText("http://192.168.?.?")
        bodyEditText.setText("{\"command\":\"echo hello world!\"}")
        contentTypeEditText.setText("application/json")
        portEditText.setText("8080")


        sendButton = findViewById(R.id.sendButton)

        sendButton.setOnClickListener {
            sendRequest()
        }
    }


    private fun sendRequest() {
        val endpoint = endpointEditText.text.toString().trim()
        val ipAddress = urlEditText.text.toString().trim()
        val port = portEditText.text.toString().trim().toIntOrNull()

        val client = OkHttpClient()

        val urlBuilder = StringBuilder(ipAddress)
        if (port != null) {
            urlBuilder.append(":").append(port)
        }
        urlBuilder.append("/").append(endpoint)

        val url = urlBuilder.toString()
        val requestBody = bodyEditText.text.toString().trim()
        val contentType = contentTypeEditText.text.toString().trim()

        val requestBuilder = Request.Builder()
            .url(url)

        if (requestBody.isNotEmpty()) {
            val body = RequestBody.create(contentType.toMediaTypeOrNull(), requestBody)
            requestBuilder.post(body)
        }

        val request = requestBuilder.build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                showToast("Request failed: ${e.message}")
                copyToClipboard("${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                GlobalScope.launch(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        showToast(responseBody)
                    } else {
                        showToast("Request unsuccessful: ${response.code} ${response.message}")
                    }
                }
            }
        })
    }

    private fun showToast(message: String?) {
        runOnUiThread {
            Toast.makeText(this@MainActivity, message ?: "null", Toast.LENGTH_SHORT).show()
        }
    }

    // Copy a string to the clipboard
    private fun copyToClipboard(text: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("Copied Text", text)
        clipboardManager.setPrimaryClip(clipData)

        showToast("Text copied to clipboard")
    }

    private val PERMISSION_REQUEST_CODE = 123

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            for (i in grantResults.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Permissions", "${permissions[i]} granted.")
                } else {
                    Log.d("Permissions", "${permissions[i]} denied.")
                }
            }
        }
    }
}
