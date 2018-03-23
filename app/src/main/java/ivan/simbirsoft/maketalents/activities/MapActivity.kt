package ivan.simbirsoft.maketalents.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
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
import com.google.android.gms.maps.model.MarkerOptions
import ivan.simbirsoft.maketalents.R
import ivan.simbirsoft.maketalents.activities.base.BaseActivity
import kotlinx.android.synthetic.main.activity_map_activity.*



/**
 * Created by Ivan Kuznetsov
 * on 23.03.2018.
 */
class MapActivity: BaseActivity(), OnMapReadyCallback {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    private var mMap: GoogleMap? = null

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

    @SuppressLint("MissingPermission")
    private fun startFindMyLocation() {
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
                .requestLocationUpdates(LocationRequest.create(), locationCallBack, null)

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
    }
}