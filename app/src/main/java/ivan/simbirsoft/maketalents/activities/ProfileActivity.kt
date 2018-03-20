package ivan.simbirsoft.maketalents.activities

import android.app.Activity
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

    companion object {
        const val DEFAULT_AVATAR_URL = "https://firebasestorage.googleapis.com/v0/b/simbirsoft-maketalents.appspot.com/o/default_avatar.png?alt=media&token=92bd7949-4bf6-43eb-bb28-98c349890084"
    }

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

            updateAvatars(it.avatarUrl)
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

    private fun updateAvatars(url: String) {
        if (url.isEmpty()) {
            Picasso.get().load(ProfileActivity.DEFAULT_AVATAR_URL).into(avatarImageView)
            Picasso.get().load(ProfileActivity.DEFAULT_AVATAR_URL).into(blurredAvatarImageView)
        } else {
            //todo need refactoring
            Picasso.get().load(url).into(avatarImageView)
            Picasso.get().load(url).into(blurredAvatarImageView)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        viewModel.inputs.actionBarBackButtonClicked()
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            viewModel.inputs.profileWasEdited()
        }
    }
}