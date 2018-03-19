package ivan.simbirsoft.maketalents.viewmodel

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent

/**
 * Created by Ivan Kuznetsov
 * on 14.12.2017.
 */
class ViewModelLifecycleDispatcher private constructor(private val viewModel:
                            BaseViewModel, private val lifecycle: Lifecycle): LifecycleObserver {

    companion object {
        fun attachTo(viewModel: BaseViewModel, lifecycle: Lifecycle) {
            ViewModelLifecycleDispatcher(viewModel, lifecycle)
        }
    }

    init {
        lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
    private fun onAny(source: LifecycleOwner, event: Lifecycle.Event) {
        viewModel.dispatchLifecycleEvent(source.lifecycle.currentState, event)

        if (event == Lifecycle.Event.ON_DESTROY) {
            lifecycle.removeObserver(this)
        }
    }
}