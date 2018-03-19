package ivan.simbirsoft.maketalents.viewmodel

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.ViewModel
import com.trello.rxlifecycle2.LifecycleTransformer
import com.trello.rxlifecycle2.RxLifecycle
import io.reactivex.subjects.BehaviorSubject

/**
 * Created by Ivan Kuznetsov
 * on 14.12.2017.
 */
open class BaseViewModel: ViewModel(), LifecycleObserver {

    private val mLifecycleSubject = BehaviorSubject.create<Lifecycle.Event>()
    private var mCreated = false

    open protected fun onCreated() {}

    override fun onCleared() {
        mLifecycleSubject.onComplete()
    }

    internal fun dispatchLifecycleEvent(state: Lifecycle.State, event: Lifecycle.Event) {
        val startState = lazy { state == Lifecycle.State.STARTED }
        val startEvent = lazy { event == Lifecycle.Event.ON_START }
        if (!mCreated && startState.value && startEvent.value) {
            onCreated()
            mCreated = true
        }
    }

    fun <T> bindToLifecycle(): LifecycleTransformer<T> = RxLifecycle.bind(mLifecycleSubject)
}