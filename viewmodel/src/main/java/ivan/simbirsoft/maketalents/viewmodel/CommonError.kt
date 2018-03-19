package ivan.simbirsoft.maketalents.viewmodel

import android.annotation.SuppressLint

/**
 * Created by Ivan Kuznetsov
 * on 14.12.2017.
 */
@SuppressLint("StaticFieldLeak")
private val mContext = ViewModelUtils.sApplicationContext

enum class CommonError(private val message: String): ViewModelError {
    OK("ok"),
    SOME_ERROR(mContext.getString(R.string.viewmodel_some_error));

    override fun message() = message
}