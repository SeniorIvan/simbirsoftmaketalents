package ivan.simbirsoft.maketalents.activities

import android.app.Activity
import android.os.Bundle
import com.jakewharton.rxbinding2.widget.RxTextView
import ivan.simbirsoft.maketalents.R
import ivan.simbirsoft.maketalents.activities.base.ViewModelActivity
import ivan.simbirsoft.maketalents.fragments.SimpleMessageDialog
import ivan.simbirsoft.maketalents.rx.SafeFragmentTransactionOperator
import ivan.simbirsoft.maketalents.utils.hideGone
import ivan.simbirsoft.maketalents.utils.setText
import ivan.simbirsoft.maketalents.viewmodels.AuthorizationViewModel
import kotlinx.android.synthetic.main.activity_authorization.*
import kotlinx.android.synthetic.main.layout_progress_bar_with_darkened_background.*

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

        viewModel.outputs.emailValue().compose(bindToLifecycle()).take(1).subscribe {
            emailTextInputLayout.setText(it)
        }

        viewModel.outputs.passwordValue().compose(bindToLifecycle()).take(1).subscribe {
            passwordTextInputLayout.setText(it)
        }

        viewModel.outputs.loginProgressBarVisibilityState().compose(bindToLifecycle()).subscribe {hiden ->
            progress_bar_with_darkened_background.hideGone(!hiden)
        }

        viewModel.outputs.finishActivity().compose(bindToLifecycle()).subscribe {
            finish()
        }

        viewModel.outputs.finishActivityWithResult().compose(bindToLifecycle()).subscribe {
            setResult(Activity.RESULT_OK)
            finish()
        }

        viewModel.outputs.showMessage().compose(bindToLifecycle()).lift(SafeFragmentTransactionOperator(lifecycle)).subscribe {
            SimpleMessageDialog.createInstance(message = it)
                    .show(supportFragmentManager, SimpleMessageDialog.TAG)
        }

        loginButton.setOnClickListener {
            viewModel.inputs.loginButtonClicked()
        }

        RxTextView.textChanges(emailTextInputLayout.editText!!).compose(bindToLifecycle())
                .skip(1).subscribe {
                    viewModel.inputs.emailWasChanged(it.toString())
                }

        RxTextView.textChanges(passwordTextInputLayout.editText!!).compose(bindToLifecycle())
                .skip(1).subscribe {
                    viewModel.inputs.passwordWasChanged(it.toString())
                }
    }

    override fun onSupportNavigateUp(): Boolean {
        viewModel.inputs.actionBarBackButtonCliked()
        return false
    }
}