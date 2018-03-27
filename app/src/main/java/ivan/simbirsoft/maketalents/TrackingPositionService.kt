package ivan.simbirsoft.maketalents

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.IBinder
import android.os.Looper
import android.support.v4.app.NotificationCompat
import android.util.Log
import com.google.android.gms.location.*
import io.reactivex.disposables.Disposable
import ivan.simbirsoft.maketalents.utils.FirebaseUtils

/**
 * Created by Ivan Kuznetsov
 * on 27.03.2018.
 */
class TrackingPositionService: Service() {

    private lateinit var mFusedLocationClient: FusedLocationProviderClient

    override fun onBind(intent: Intent?): IBinder? = null

    @SuppressLint("MissingPermission")
    override fun onCreate() {
        super.onCreate()
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
        startForeground(1, createServiceNotification(getString(R.string.app_name)))
    }

    override fun onDestroy() {
        super.onDestroy()
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
        mLastDisposable?.dispose()
    }

    private fun createServiceNotification(contentText: String): Notification {
        val builder = NotificationCompat.Builder(applicationContext, "my")
        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                .setContentText(contentText)
                .addAction(R.mipmap.ic_launcher, getString(R.string.stop), stopPendingIntent())
                .setPriority(NotificationCompat.PRIORITY_MIN)

        return builder.build()
    }

    private fun stopPendingIntent(): PendingIntent {
        val intent = Intent(applicationContext, TrackingPositionReceiver::class.java)
        return PendingIntent.getBroadcast(this, 0, intent, 0)
    }

    private val mLocationRequest = LocationRequest.create().also {
        it.interval = 2000
        it.fastestInterval = 2000
        it.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
    }

    private val mUser = FirebaseUtils.fetchUser().cache()

    private var mLastDisposable: Disposable? = null

    private var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val lastLocation = locationResult.lastLocation
            val latitude = lastLocation.latitude
            val longitude = lastLocation.longitude

            mLastDisposable?.dispose()
            mLastDisposable = mUser.flatMap {
                FirebaseUtils.updateUserInformation(it.name, it.phoneNumber, it.email, it.avatarUrl, latitude, longitude)
            }.subscribe({
                Log.e("tag", "ok")
            }, {
                it.printStackTrace()
            })

        }
    }
}