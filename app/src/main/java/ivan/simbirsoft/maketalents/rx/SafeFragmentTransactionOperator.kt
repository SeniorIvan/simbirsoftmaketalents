package ivan.simbirsoft.maketalents.rx

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import io.reactivex.ObservableOperator
import io.reactivex.Observer
import io.reactivex.disposables.Disposable
import io.reactivex.internal.disposables.DisposableHelper

/**
 * Created by Ivan Kuznetsov
 * on 27.11.2017.
 */

/**
 * emit signal only if activity or fragment is resumed
 * Example usage:
 * ```
 * viewModel?.outputs?.showMessage?.compose(bindToLifecycle())?.lift(SafeFragmentTransactionOperator(lifecycle))?.subscribe {
 *     SimpleMessageDialog.createInstance(message = it).show(childFragmentManager, SimpleMessageDialog.TAG)
 * }
 * ```
 */
class SafeFragmentTransactionOperator<D>(private val mLifecycle: Lifecycle) : ObservableOperator<D, D> {

    override fun apply(observer: Observer<in D>): Observer<in D> =
            InnerObserver(observer, mLifecycle)

    inner class InnerObserver(private val mOriginalObserver: Observer<in D>,
                              private val mLifecycle: Lifecycle) : Observer<D>, Disposable,
            LifecycleObserver {

        private val mDeferredSignals = mutableListOf<D>()

        private var mDisposable: Disposable? = null

        override fun onSubscribe(d: Disposable) {
            if (DisposableHelper.validate(mDisposable, d)) {
                mDisposable = d
                mOriginalObserver.onSubscribe(this)
                mLifecycle.addObserver(this)
            }
        }

        override fun onNext(t: D) {
            if (mLifecycle.currentState == Lifecycle.State.RESUMED) {
                mOriginalObserver.onNext(t)
            } else {
                mDeferredSignals.add(t)
            }
        }

        override fun onError(e: Throwable) {
            mOriginalObserver.onError(e)
        }

        override fun onComplete() {
            mOriginalObserver.onComplete()
        }

        override fun isDisposed(): Boolean = mDisposable!!.isDisposed

        override fun dispose() {
            mDisposable!!.dispose()
            mLifecycle.removeObserver(this)
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        private fun onResume() {
            val iterator = mDeferredSignals.iterator()

            while (iterator.hasNext()) {
                val signal = iterator.next()
                if (mDisposable!!.isDisposed) {
                    return
                }
                mOriginalObserver.onNext(signal)
                iterator.remove()
            }
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        private fun onDestroy() {
            dispose()
        }
    }
}