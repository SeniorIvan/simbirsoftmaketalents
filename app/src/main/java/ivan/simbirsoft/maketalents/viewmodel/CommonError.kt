package ivan.simbirsoft.maketalents.viewmodel

import android.annotation.SuppressLint
import ivan.simbirsoft.maketalents.R

/**
 * Created by Ivan Kuznetsov
 * on 14.12.2017.
 */
@SuppressLint("StaticFieldLeak")
private val mContext = ViewModelUtils.sApplicationContext

enum class CommonError(private val message: String): ViewModelError {
    OK("ok"),
    SOME_ERROR(mContext.getString(R.string.viewmodel_some_error)),
    NOT_AUTH(mContext.getString(R.string.not_auth));

    override fun message() = message
}