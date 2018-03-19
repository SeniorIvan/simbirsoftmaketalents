package ivan.simbirsoft.maketalents

import android.app.Application
import ivan.simbirsoft.maketalents.viewmodel.ViewModelUtils

/**
 * Created by Ivan Kuznetsov
 * on 19.03.2018.
 */
class MaketalentsApplication: Application() {

    override fun onCreate() {
        ViewModelUtils.sApplicationContext = applicationContext
        super.onCreate()
    }
}