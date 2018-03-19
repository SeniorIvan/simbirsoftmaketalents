package ivan.simbirsoft.maketalents.utils

import android.support.design.widget.TextInputLayout
import android.view.View
import io.reactivex.subjects.PublishSubject

/**
 * Created by Ivan Kuznetsov
 * on 19.03.2018.
 */

fun PublishSubject<Signal>.emit() {
    onNext(Signal.Instance)
}

fun TextInputLayout.setText(text: String) {
    editText?.setText(text)
}

fun View.hideGone(hide: Boolean) {
    visibility =
            if (hide) {
                View.GONE
            } else {
                View.VISIBLE
            }
}