package ivan.simbirsoft.maketalents.viewmodel

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

/**
 * Created by Ivan Kuznetsov
 * on 14.12.2017.
 */
class ViewModelUtils {

    @Suppress("UNCHECKED_CAST")
    companion object {
        lateinit var sApplicationContext: Context

        inline fun <reified VM : BaseViewModel> createViewModel(activity: FragmentActivity, viewModelKey: String, crossinline onCreateViewModel: () -> VM): VM {
            return ViewModelProviders.of(activity, object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return onCreateViewModel() as T
                }
            }).get(viewModelKey, VM::class.java)
        }

        inline fun <reified VM : BaseViewModel> createViewModel(fragment: Fragment, viewModelKey: String, crossinline onCreateViewModel: () -> VM): VM {
            return ViewModelProviders.of(fragment, object : ViewModelProvider.Factory {
                override fun <T : ViewModel?> create(modelClass: Class<T>): T {
                    return onCreateViewModel() as T
                }
            }).get(viewModelKey, VM::class.java)
        }
    }
}

fun Throwable.throwableToError(): ViewModelError {
    return if (this is ViewModelThrowable) {
        error
    } else {
        CommonError.SOME_ERROR
    }
}