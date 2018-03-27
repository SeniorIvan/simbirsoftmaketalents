package ivan.simbirsoft.maketalents.viewmodels

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ivan.simbirsoft.maketalents.entities.UserEntity
import ivan.simbirsoft.maketalents.viewmodel.BaseViewModel

/**
 * Created by Ivan Kuznetsov
 * on 27.03.2018.
 */

interface MapInputs {
}

interface MapOutputs {

    fun allMarkers(): Observable<Map<String, LatLng>>

    fun drawMarker(): Observable<Pair<String, LatLng>>

    fun allMovingHistory(): Observable<Map<String, Set<LatLng>>>

    fun userWasMoved(): Observable<Pair<LatLng, LatLng>>
}

class MapViewModel : BaseViewModel(), MapInputs, MapOutputs {

    private val mAllMarkersObservable = BehaviorSubject.createDefault(mutableMapOf<String, LatLng>())
    private val mAllMovingHistoryObservable = BehaviorSubject.createDefault<MutableMap<String, MutableSet<LatLng>>>(mutableMapOf())

    private val mDrawMarkersObservable = PublishSubject.create<Pair<String, LatLng>>()
    private val mUserWasMovedObservable = PublishSubject.create<Pair<LatLng, LatLng>>()


    val inputs: MapInputs = this
    val outputs: MapOutputs = this

    private var mListener: ChildEventListener? = null

    override fun onCreated() {
        super.onCreated()
        mListener = object : ChildEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                //nothing
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val user = snapshot.getValue(UserEntity::class.java)

                val lat = user?.latitude
                val lon = user?.longitude

                if (lat != null && lon != null) {
                    val element = Pair(snapshot.key, LatLng(lat, lon))
                    mAllMarkersObservable.value[snapshot.key] = element.second
                    mDrawMarkersObservable.onNext(element)

                    val history = getMovingHistory(snapshot.key)

                    val lastItem = history.lastOrNull()

                    val add = history.add(element.second)
                    if (lastItem != null && add) {
                        mUserWasMovedObservable.onNext(Pair(lastItem, element.second))
                    }
                }
            }

            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val user = snapshot.getValue(UserEntity::class.java)

                val lat = user?.latitude
                val lon = user?.longitude

                if (lat != null && lon != null) {
                    val element = Pair(snapshot.key, LatLng(lat, lon))
                    mAllMarkersObservable.value[snapshot.key] = element.second
                    mDrawMarkersObservable.onNext(element)

                    val history = getMovingHistory(snapshot.key)
                    history.add(element.second)
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                //nothing
            }

        }
        FirebaseDatabase.getInstance().getReference("users").addChildEventListener(mListener)
    }

    override fun onCleared() {
        super.onCleared()
        mListener?.let {
            FirebaseDatabase.getInstance().getReference("users").removeEventListener(it)
        }
    }

    private fun getMovingHistory(userId: String): MutableSet<LatLng> {
        val history = mAllMovingHistoryObservable.value[userId]

        if (history == null) {
            val h = mutableSetOf<LatLng>()
            mAllMovingHistoryObservable.value.put(userId, h)
            return h
        }
        return history
    }

    override fun allMarkers(): Observable<Map<String, LatLng>> {
        return mAllMarkersObservable.map { it.toMap() }
    }

    override fun drawMarker(): Observable<Pair<String, LatLng>> {
        return mDrawMarkersObservable
    }

    override fun allMovingHistory(): Observable<Map<String, Set<LatLng>>> {
        return mAllMovingHistoryObservable.map { it.toMap() }
    }

    override fun userWasMoved(): Observable<Pair<LatLng, LatLng>> {
        return mUserWasMovedObservable
    }
}