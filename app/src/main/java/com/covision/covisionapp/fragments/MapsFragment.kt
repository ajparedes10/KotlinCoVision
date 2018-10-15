package com.covision.covisionapp.fragments

import android.Manifest
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

import com.covision.covisionapp.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task

import java.io.IOException
import java.util.ArrayList

class MapsFragment : Fragment(), OnMapReadyCallback, LocationListener {
    internal var TAG_CODE_PERMISSION_LOCATION = 2

    //widgets
    private var mSearchText: EditText? = null
    private var mGps: ImageView? = null

    //vars
    private var mLocationPermissionsGranted: Boolean? = false
    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private var mPlaceDetectionClient: PlaceDetectionClient? = null

    lateinit var mapView: MapView
    internal var mMap: GoogleMap? = null
    lateinit var locationManager: LocationManager

    public fun getDeviceLocation(): String {
            val locationMessage = arrayOf("")
            Log.d(TAG, "getDeviceLocation: getting the devices current location")
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity!!)
            try {
                if (mLocationPermissionsGranted!!) {

                    val location: Task<Location>? = mFusedLocationProviderClient!!.lastLocation
                    if(location !=null){
                    location.addOnCompleteListener{task->
                            if (task.isSuccessful) {
                                Log.d(TAG, "onComplete: found location!")
                                val currentLocation = task.result as Location?
                                if(currentLocation != null) {
                                    moveCamera(LatLng(currentLocation!!.latitude, currentLocation.longitude),
                                            DEFAULT_ZOOM,
                                            "My Location")
                                    locationMessage[0] = showCurrentPlace()
                                }else{
                                    Log.d(TAG, "onComplete: current location is null :(")
                                }
                            } else {
                                Log.d(TAG, "onComplete: current location is null")
                                Toast.makeText(activity, "unable to get current location", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                    else{
                        getLocationPermission()
                    }
                }
            } catch (e: SecurityException) {
                Log.e(TAG, "getDeviceLocation: SecurityException: " + e.message)
            }

            return locationMessage[0]
        }


    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_maps, container, false)

        mapView = v.findViewById<View>(R.id.mapview) as MapView
        mapView.onCreate(savedInstanceState)

        // Gets to GoogleMap from the MapView and does initialization stuff
        mapView.getMapAsync(this)

        mSearchText = v.findViewById<View>(R.id.input_search) as EditText
        mGps = v.findViewById<View>(R.id.ic_gps) as ImageView
        getLocationPermission()

        return v
    }

    override fun onResume() {
        mapView.onResume()
        super.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        locationManager.removeUpdates(this)

    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
        locationManager.removeUpdates(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {


        Toast.makeText(activity, "Map is Ready", Toast.LENGTH_SHORT).show()
        Log.d(TAG, "onMapReady: map is ready")
        mMap = googleMap

        if (mLocationPermissionsGranted!!) {
            getDeviceLocation()

            if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context!!,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return
            }
            mMap!!.isMyLocationEnabled = true
            mMap!!.uiSettings.isMyLocationButtonEnabled = false

            init()
        }

        mMap!!.uiSettings.isMyLocationButtonEnabled = false
        if (ContextCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(activity!!, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            googleMap.isMyLocationEnabled = true
            mMap!!.isMyLocationEnabled = true
            googleMap.uiSettings.isMyLocationButtonEnabled = true

            // Needs to call MapsInitializer before doing any CameraUpdateFactory calls
            MapsInitializer.initialize(this.activity!!)
            // Updates the location and zoom of the MapView
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(43.1, -87.9), 10f)
            mMap!!.animateCamera(cameraUpdate)
        } else {
            ActivityCompat.requestPermissions(activity!!, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                    TAG_CODE_PERMISSION_LOCATION)
        }

    }

    private fun init() {
        Log.d(TAG, "init: initializing")
        mSearchText!!.setOnEditorActionListener { textView, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.action == KeyEvent.ACTION_DOWN
                    || keyEvent.action == KeyEvent.KEYCODE_ENTER
                    || keyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                //execute our method for searching
                Log.d(TAG, "trato de buscar")
                geoLocate(mSearchText!!.text.toString())
                Log.d(TAG, "trate de buscar")
            }
            Log.d(TAG, "no se pudo entrar a buscar")
            false
        }
        mGps!!.setOnClickListener {
            Log.d(TAG, "onClick: clicked gps icon")
            getDeviceLocation()
        }
        hideSoftKeyboard()
    }


    fun geoLocate(searchString : String) {
        Log.d(TAG, "geoLocate: geolocating")
        val geocoder = Geocoder(context)
        var list: List<Address> = ArrayList()
        try {
            list = geocoder.getFromLocationName(searchString, 1)
        } catch (e: IOException) {
            Log.e(TAG, "geoLocate: IOException: " + e.message)
        }

        if (list.size > 0) {
            val address = list[0]

            Log.d(TAG, "geoLocate: found a location: " + address.toString())
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

            moveCamera(LatLng(address.latitude, address.longitude), DEFAULT_ZOOM,
                    address.getAddressLine(0))
        }
    }

    private fun moveCamera(latLng: LatLng, zoom: Float, title: String) {
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude)
        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom))

        if (title != "My Location") {
            val options = MarkerOptions()
                    .position(latLng)
                    .title(title)
            mMap!!.addMarker(options)
        }
        hideSoftKeyboard()
    }

    private fun hideSoftKeyboard() {
        activity!!.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }


    private fun getLocationPermission() {
        locationManager = context!!.getSystemService(LOCATION_SERVICE) as LocationManager


        Log.d(TAG, "getLocationPermission: getting location permissions")
        val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (ContextCompat.checkSelfPermission(activity!!.applicationContext,
                        FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(activity!!.applicationContext,
                            COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true

                //verificar si el GPS está encendido
                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    Log.d(TAG, "el GPS no está activado")
                    val intent1 = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent1)
                }
            } else {
                ActivityCompat.requestPermissions(activity!!,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE)
            }
        } else {
            ActivityCompat.requestPermissions(activity!!,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.d(TAG, "onRequestPermissionsResult: called.")
        mLocationPermissionsGranted = false

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.size > 0) {
                    for (i in grantResults.indices) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false
                            Log.d(TAG, "onRequestPermissionsResult: permission failed")
                            return
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted")
                    mLocationPermissionsGranted = true
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        //Hey, a non null location! Sweet!

        //remove location callback:
        locationManager.removeUpdates(this)

    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onProviderDisabled(provider: String) {

    }

    //lugar actual, por ahora este método es llamado por getDeviceLocation() pero puede ser llamado desdde cualquier fragmento
    fun showCurrentPlace(): String {
        val rta = arrayOf("")
        if (mMap == null) {
            return rta[0]
        }
        if (mLocationPermissionsGranted!!) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            if (ActivityCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return rta[0]
            }

            mPlaceDetectionClient = Places.getPlaceDetectionClient(activity!!, null)
            val placeResult = mPlaceDetectionClient!!.getCurrentPlace(null)
            placeResult.addOnCompleteListener { task ->
                val likelyPlaces = task.result
                var max = -1f
                for (placeLikelihood in likelyPlaces!!) {
                    Log.i(TAG, String.format("Place '%s' has likelihood: %g",
                            placeLikelihood.place.name,
                            placeLikelihood.likelihood))
                    if (placeLikelihood.likelihood > max) {
                        max = placeLikelihood.likelihood
                        rta[0] = "Te encuentras en " + placeLikelihood.place.name.toString()
                    }
                }
                likelyPlaces.release()
                Log.i(TAG, "rta es: " + rta[0])
            }
        }
        return rta[0]
    }

    companion object {
        private val TAG = "MapActivity"
        private val FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
        private val COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
        private val LOCATION_PERMISSION_REQUEST_CODE = 1234
        private val DEFAULT_ZOOM = 15f


        fun newInstance(): Fragment {
            return MapsFragment()
        }
    }
}
