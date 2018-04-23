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

// TODO какое-то странное название. Может, всё-таки на 2 разных метода разбить
// TODO а в таком виде назвать как-то типа showIf(condition: Boolean)
fun View.hideGone(hide: Boolean) {
    visibility =
            if (hide) {
                View.GONE
            } else {
                View.VISIBLE
            }
}

fun TextInputLayout.setError(error: Boolean, errorMessage: String) {
    if (error) {
        this.error = errorMessage
    } else {
        // TODO тут точно не this.error = null должно быть?
        isErrorEnabled = false
    }
}