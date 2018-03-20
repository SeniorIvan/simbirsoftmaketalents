package ivan.simbirsoft.maketalents.activities

import android.content.Intent
import android.os.Bundle
import com.squareup.picasso.Picasso
import ivan.simbirsoft.maketalents.R
import ivan.simbirsoft.maketalents.activities.base.ViewModelActivity
import ivan.simbirsoft.maketalents.viewmodel.ViewModelContentState
import ivan.simbirsoft.maketalents.viewmodels.ProfileViewModel
import ivan.simbirsoft.maketalents.views.CompositeView
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.layout_profile.*

/**
 * Created by Ivan Kuznetsov
 * on 19.03.2018.
 */
class ProfileActivity: ViewModelActivity<ProfileViewModel>() {

    override fun onCreateViewModel() = ProfileViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val compositeView = view_animator.getChildAt(ViewModelContentState.ERROR_PAGE.index) as CompositeView

        compositeView.setOnButtonClickListener {
            viewModel.repeatButtonClicked()
        }

        viewModel.baseOutputs().currentContentState().compose(bindToLifecycle()).subscribe { contentState ->
            view_animator.displayedChild = contentState.index
        }

        viewModel.baseOutputs().contentErrorText().compose(bindToLifecycle()).subscribe {
            compositeView.setMessageText(it)
        }

        viewModel.baseOutputs().data().compose(bindToLifecycle()).subscribe {
            nameTextView.text = it.name
            phoneNumberTextView.text = it.phoneNumber
            emailTextView.text = it.email

            Picasso.get().load(R.drawable.default_avatar).into(avatarImageView)
            Picasso.get().load(R.drawable.default_avatar).into(blurredAvatarImageView)
        }

        editProfileTextView.setOnClickListener {
            viewModel.inputs.editProfileButtonClicked()
        }

        logoutTextView.setOnClickListener {
            viewModel.inputs.logoutButtonClicked()
        }

        viewModel.outputs.finishActivity().compose(bindToLifecycle()).subscribe {
            finish()
        }

        viewModel.outputs.openEditProfileActivity().compose(bindToLifecycle()).subscribe {
            startActivityForResult(Intent(this, EditProfileActivity::class.java), 1)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        viewModel.inputs.actionBarBackButtonClicked()
        return false
    }
}