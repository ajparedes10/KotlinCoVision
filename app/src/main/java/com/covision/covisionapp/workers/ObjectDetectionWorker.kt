package com.covision.covisionapp.workers

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64

import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.covision.covisionapp.fragments.ObjectDetectionFragment
import com.covision.covisionapp.structures.BoundingBox
import com.covision.covisionapp.structures.ObjectDetectionResult

import org.json.JSONException
import org.json.JSONObject

import java.io.ByteArrayOutputStream

class ObjectDetectionWorker(private val context: Context?, private val image: Bitmap, private val callback: ObjectDetectionFragment.ObjectDetectionCallback) : Thread("ObjectDetection") {

    override fun run() {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val byteCount = byteArray.size

        val requestBody = JSONObject()
        try {
            requestBody.put("mode", "navigation")
            requestBody.put("image", Base64.encode(byteArray, Base64.DEFAULT))
        } catch (e: JSONException) {
            this.callback.onError(e.message!!)
        }

        val request = JsonObjectRequest(Request.Method.GET, SERVER_URL, requestBody, Response.Listener { response ->
            try {
                val result = ObjectDetectionResult()
                val text = response.getString("count_text")
                result.addText(text)
                val classes = response.getJSONArray("objects")
                for (i in 0 until classes.length()) {
                    val currentClass = classes.getJSONObject(i)
                    val className = currentClass.getString("class")
                    val boxes = currentClass.getJSONArray("boxes")
                    for (j in 0 until boxes.length()) {
                        val box = boxes.getJSONObject(j)
                        val score = box.getDouble("score")
                        val points = box.getJSONArray("box")
                        val boxPoints = DoubleArray(points.length())
                        for (k in 0 until points.length()) {
                            boxPoints[k] = points.getDouble(k)
                        }
                        result.addBox(BoundingBox(className, score, boxPoints))
                    }
                }
                this@ObjectDetectionWorker.callback.onDetectionResult(result)
            } catch (e: JSONException) {
                this@ObjectDetectionWorker.callback.onError(e.message!!)
            }
        }, Response.ErrorListener { error -> this@ObjectDetectionWorker.callback.onError(error.message!!) })

        RestRequestQueue.getInstance(context!!).addToRequestQueue(request)
    }

    companion object {
        private val SERVER_URL = "http://201.244.214.60:5000"
    }

}
