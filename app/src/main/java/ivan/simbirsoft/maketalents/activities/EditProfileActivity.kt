package ivan.simbirsoft.maketalents.activities

import android.os.Bundle
import com.jakewharton.rxbinding2.widget.RxTextView
import com.squareup.picasso.Picasso
import ivan.simbirsoft.maketalents.R
import ivan.simbirsoft.maketalents.activities.base.ViewModelActivity
import ivan.simbirsoft.maketalents.fragments.SimpleMessageDialog
import ivan.simbirsoft.maketalents.rx.SafeFragmentTransactionOperator
import ivan.simbirsoft.maketalents.utils.hideGone
import ivan.simbirsoft.maketalents.utils.setText
import ivan.simbirsoft.maketalents.viewmodel.ViewModelContentState
import ivan.simbirsoft.maketalents.viewmodels.EditProfileViewModel
import ivan.simbirsoft.maketalents.views.CompositeView
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.layout_edit_profile.*
import kotlinx.android.synthetic.main.layout_progress_bar_with_darkened_background.*

/**
 * Created by Ivan Kuznetsov
 * on 20.03.2018.
 */
class EditProfileActivity : ViewModelActivity<EditProfileViewModel>() {

    override fun onCreateViewModel() = EditProfileViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
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
            nameTextInputLayout.setText(it.name)
            phoneNumberTextInputLayout.setText(it.phoneNumber)
            emailTextInputLayout.setText(it.email)

            Picasso.get().load(R.drawable.default_avatar).into(avatarImageView)
            Picasso.get().load(R.drawable.default_avatar).into(blurredAvatarImageView)

            RxTextView.textChanges(nameTextInputLayout.editText!!).compose(bindToLifecycle())
                    .skip(1).subscribe {
                        viewModel.inputs.nameWasChanged(it.toString())
                    }

            RxTextView.textChanges(phoneNumberTextInputLayout.editText!!).compose(bindToLifecycle())
                    .skip(1).subscribe {
                        viewModel.inputs.phoneNumberWasChanged(it.toString())
                    }

            RxTextView.textChanges(emailTextInputLayout.editText!!).compose(bindToLifecycle())
                    .skip(1).subscribe {
                        viewModel.inputs.emailWasChanged(it.toString())
                    }
        }

        viewModel.outputs.showMessage().compose(bindToLifecycle()).lift(SafeFragmentTransactionOperator(lifecycle)).subscribe {
            SimpleMessageDialog.createInstance(message = it)
                    .show(supportFragmentManager, SimpleMessageDialog.TAG)
        }

        viewModel.outputs.savingProgressBarVisibilityState().compose(bindToLifecycle()).subscribe { hiden ->
            progress_bar_with_darkened_background.hideGone(!hiden)
        }

        viewModel.outputs.finishActivity().compose(bindToLifecycle()).subscribe {
            finish()
        }

        saveButton.setOnClickListener {
            viewModel.inputs.saveButtonClicked()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        viewModel.inputs.actionBarBackButtonClicked()
        return false
    }
}