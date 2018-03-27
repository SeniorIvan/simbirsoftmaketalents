package ivan.simbirsoft.maketalents.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import ivan.simbirsoft.maketalents.R
import ivan.simbirsoft.maketalents.TrackingPositionService
import ivan.simbirsoft.maketalents.activities.base.ViewModelActivity
import ivan.simbirsoft.maketalents.viewmodels.MapViewModel
import kotlinx.android.synthetic.main.activity_map_activity.*






/**
 * Created by Ivan Kuznetsov
 * on 23.03.2018.
 */
class MapActivity : ViewModelActivity<MapViewModel>(), OnMapReadyCallback {

    private val mMarkers: MutableMap<String, Marker> = mutableMapOf()
    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var mMap: GoogleMap? = null

    override fun onCreateViewModel(): MapViewModel {
        return MapViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        findMyLocationButton.setOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            } else {
                startFindMyLocation()
            }
        }

        plusButton.setOnClickListener {
            mMap?.animateCamera(CameraUpdateFactory.zoomIn())
        }

        minusButton.setOnClickListener {
            mMap?.animateCamera(CameraUpdateFactory.zoomOut())
        }
    }

    override fun onStop() {
        super.onStop()
        stopFindMyLocation()
    }

    private val mLocationRequest = LocationRequest.create().also {
        it.interval = 10000
        it.fastestInterval = 10000
        it.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    private var mMyMarker: Marker? = null

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation = locationResult.lastLocation
            val longitude = lastLocation.longitude
            val latitude = lastLocation.latitude
            val latLng = LatLng(longitude, latitude)

            val myMarker = mMyMarker
            if (myMarker == null) {
                mMyMarker = mMap?.addMarker(MarkerOptions().position(latLng))
            } else {
                myMarker.position = latLng
            }
        }
    }

    private fun getMarker(userId: String): Marker {
        val marker = mMarkers[userId]

        if (marker == null) {
            val m = mMap!!.addMarker(MarkerOptions().position(LatLng(0.0, 0.0)))
            mMarkers[userId] = m
            return m
        }

        return marker
    }

    @SuppressLint("MissingPermission")
    private fun startFindMyLocation() {
        startService(Intent(this, TrackingPositionService::class.java))
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
    }

    private fun stopFindMyLocation() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onMapReady(map: GoogleMap) {
        mMap = map

        viewModel.outputs.allMarkers().compose(bindToLifecycle()).subscribe {
            it.forEach {
                val marker = getMarker(it.key)
                marker.position = it.value
            }
        }

        viewModel.outputs.drawMarker().compose(bindToLifecycle()).subscribe {
            val marker = getMarker(it.first)
            marker.position = it.second
        }

        viewModel.outputs.allMovingHistory().compose(bindToLifecycle()).subscribe {
            it.forEach {
                mMap?.addPolyline(PolylineOptions()
                        .addAll(it.value)
                        .width(5f)
                        .color(Color.RED))
            }
        }

        viewModel.outputs.userWasMoved().compose(bindToLifecycle()).subscribe {
            mMap?.addPolyline(PolylineOptions()
                    .add(it.first, it.second)
                    .width(5f)
                    .color(Color.RED))
        }
    }
}