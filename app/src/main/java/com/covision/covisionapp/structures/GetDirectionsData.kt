package com.covision.covisionapp.structures

import android.os.AsyncTask

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

import java.io.IOException
import java.util.HashMap

class GetDirectionsData : AsyncTask<Any, String, String>() {

    internal lateinit var mMap: GoogleMap
    internal lateinit var url: String
    internal lateinit var googleDirectionsData: String
    internal lateinit var latLng: LatLng

    override fun doInBackground(vararg objects: Any): String {
        val ob = objects.get(0) as Array <*>
        mMap = ob.get(0) as GoogleMap
        url = ob.get(1) as String
        val dwnu = DownloadUrl()
        latLng = ob.get(2) as LatLng
        try {

            googleDirectionsData = dwnu.readUrl(url)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return googleDirectionsData
    }

    var duration = ""

    override fun onPostExecute(s: String) {
        var directionList: HashMap<String, String>? = null
        val parser = DataParser()
        directionList = parser.parseDirections(s)
        duration = directionList.get("duration")!!;
        val distance = directionList["distance"]!!
        mMap.clear()
        val mop = MarkerOptions()
        mop.position(latLng)
        mop.draggable(true)
        mop.title("Duration=" + duration)
        mop.snippet("Distance=" + distance)
        mMap.addMarker(mop)

    }

}
