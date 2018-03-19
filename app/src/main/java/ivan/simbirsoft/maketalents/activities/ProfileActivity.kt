package ivan.simbirsoft.maketalents.activities

import android.os.Bundle
import ivan.simbirsoft.maketalents.R
import ivan.simbirsoft.maketalents.activities.base.BaseActivity

/**
 * Created by Ivan Kuznetsov
 * on 19.03.2018.
 */
class ProfileActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}