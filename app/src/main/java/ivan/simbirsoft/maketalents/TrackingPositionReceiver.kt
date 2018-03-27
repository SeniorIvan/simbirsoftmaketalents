package ivan.simbirsoft.maketalents

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Created by Ivan Kuznetsov
 * on 27.03.2018.
 */
class TrackingPositionReceiver: BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        context.stopService(Intent(context, TrackingPositionService::class.java))
    }
}