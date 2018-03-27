package ivan.simbirsoft.maketalents.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
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

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)
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

        val locationRequest = LocationRequest.create()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        val locationCallBack: LocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation
                val map = mMap

                if (location != null && map != null) {
                    val myLatLng = LatLng(location.latitude, location.longitude)
                    map.addMarker(MarkerOptions().position(myLatLng).title("you"))
                    map.moveCamera(CameraUpdateFactory.newLatLng(myLatLng))
                }
            }
        }
        val requestLocationTask = mFusedLocationClient
                .requestLocationUpdates(locationRequest, locationCallBack, null)

        requestLocationTask.addOnCompleteListener {
            mFusedLocationClient.removeLocationUpdates(locationCallBack)
        }.addOnFailureListener {
            Toast.makeText(this, it.toString(), Toast.LENGTH_LONG).show()
        }
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