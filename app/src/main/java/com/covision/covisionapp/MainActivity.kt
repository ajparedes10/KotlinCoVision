package com.covision.covisionapp

import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout

import com.covision.covisionapp.fragments.MapsFragment
import com.covision.covisionapp.fragments.ObjectDetectionFragment
import com.covision.covisionapp.fragments.VoiceFragment
import android.net.ConnectivityManager
import android.net.NetworkInfo

import kotlinx.android.synthetic.main.activity_main.*;


class MainActivity : AppCompatActivity(), View.OnClickListener {

    val REQUEST_ALL = 100
    val REQUEST_CAMERA = 200
    val REQUEST_RECORD = 300
    val REQUEST_LOCATION = 400

    lateinit var voice: VoiceFragment
    lateinit var maps: MapsFragment
    lateinit var objectDetection: ObjectDetectionFragment
    lateinit var fragmentManager: FragmentManager

    //private var speakButton: Button? = null
    private var detectionView: FrameLayout? = null
    private var mapView: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Boton principal
        btnMic.setOnClickListener(this)

        // Fragmentos
        fragmentManager = supportFragmentManager

        if (savedInstanceState == null) {
            voice = VoiceFragment()
            fragmentManager.beginTransaction().add(R.id.voiceFragment, voice).commit()
            if(checkInternet()) {
                maps = MapsFragment()
                fragmentManager.beginTransaction().add(R.id.mapsFragment, maps).commit()
                mapView = findViewById(R.id.mapsFragment) as FrameLayout
                mapView?.setVisibility(View.INVISIBLE)
                objectDetection = ObjectDetectionFragment()
                fragmentManager.beginTransaction().add(R.id.objectDetectionFragment, objectDetection).commit()
                detectionView = findViewById(R.id.objectDetectionFragment)
                detectionView?.setVisibility(View.INVISIBLE)
            }
            else{
                voice.textToVoice("No tienes conexión a internet. Intenta más tarde")
            }
        }

        val PERMISSIONS = arrayOf(android.Manifest.permission.RECORD_AUDIO, android.Manifest.permission.CAMERA, android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION)

        if (!hasPermissions(this, *PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_ALL)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Granted
            } else {
                // Not granted
            }
            REQUEST_RECORD -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Granted
            } else {
                // Not granted
            }
            REQUEST_LOCATION -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Granted
            } else {
                // Not granted
            }
        }
    }

    /*
    onClick del boton principal
     */
    override fun onClick(v: View) {
        if (v.id == R.id.btnMic) {
            if(checkInternet()) {

                    voice.recordSpeak(object : VoiceFragment.VoiceCallback {
                        override fun onSpeechResult(result: VoiceFragment.VoiceResult, vararg params: String) {
                            when (result) {
                                VoiceFragment.VoiceResult.Location -> {
                                    voice.textToVoice(maps.showCurrentPlace())
                                    showMaps()
                                }
                                VoiceFragment.VoiceResult.Route -> {
                                    voice.textToVoice("Calculando ruta hacia " + params[0])
                                    showMaps()
                                    maps.geoLocate(params.get(0))
                                }
                                VoiceFragment.VoiceResult.Detection -> {
                                    voice.textToVoice("Iniciando análisis de imagen")
                                    showObjectDetection()
                                    objectDetection.detect()
                                }
                            }
                        }

                        override fun onError(message: String) {
                            voice.textToVoice(message)
                        }
                    })
                }else{
                voice.textToVoice("No tienes conexión a internet. Intenta más tarde")
            }
        }
    }

    private fun showMaps() {
        detectionView = findViewById(R.id.objectDetectionFragment)
        detectionView?.setVisibility(View.INVISIBLE)
        mapView = findViewById(R.id.mapsFragment)
        mapView?.setVisibility(View.VISIBLE)
        val ft = fragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.slide_in_right, 0)
        ft.show(maps)
        ft.commit()
    }

    private fun showObjectDetection() {
        detectionView = findViewById(R.id.objectDetectionFragment)
        detectionView?.setVisibility(View.VISIBLE)
        val ft = fragmentManager.beginTransaction()
        ft.setCustomAnimations(R.anim.slide_in_right, 0)
        ft.show(objectDetection)
        ft.commit()
    }

    companion object {

        val REQUEST_ALL = 100
        val REQUEST_CAMERA = 200
        val REQUEST_RECORD = 300
        val REQUEST_LOCATION = 400

        fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
            if (context != null && permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false
                    }
                }
            }
            return true
        }
    }

    private fun checkInternet (): Boolean{
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if(connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED){
            return true
        }
        return false
    }

}
