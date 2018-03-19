package ivan.simbirsoft.maketalents.activities

import android.os.Bundle
import ivan.simbirsoft.maketalents.R
import ivan.simbirsoft.maketalents.activities.base.ViewModelActivity
import ivan.simbirsoft.maketalents.viewmodels.AuthorizationViewModel

/**
 * Created by Ivan Kuznetsov
 * on 19.03.2018.
 */
class AuthorizationActivity: ViewModelActivity<AuthorizationViewModel>() {
    
    override fun onCreateViewModel(): AuthorizationViewModel {
        return AuthorizationViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setContentView(R.layout.activity_authorization)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}