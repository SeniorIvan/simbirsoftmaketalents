package ivan.simbirsoft.maketalents.viewmodels

import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ivan.simbirsoft.maketalents.utils.Signal
import ivan.simbirsoft.maketalents.utils.emit
import ivan.simbirsoft.maketalents.viewmodel.BaseViewModel

/**
 * Created by Ivan Kuznetsov
 * on 19.03.2018.
 */

interface AuthorizationInputs {

    fun actionBarBackButtonCliked()
    fun loginButtonClicked()

    fun emailWasChanged(email: String)
    fun passwordWasChanged(password: String)
}

interface AuthorizationOutputs {

    fun emailValue(): Observable<String>
    fun passwordValue(): Observable<String>
    fun loginProgressBarVisibilityState(): Observable<Boolean>
    fun finishActivity(): Observable<Signal>
    fun finishActivityWithResult(): Observable<Signal>
    fun showMessage(): Observable<String>
}

class AuthorizationViewModel : BaseViewModel(), AuthorizationInputs, AuthorizationOutputs {

    val mFirebaseAuth = FirebaseAuth.getInstance()

    private val mEmailValueObservable = BehaviorSubject.createDefault<String>("")
    private val mPasswordValueObservable = BehaviorSubject.createDefault<String>("")
    private val mLoginProgressBarVisibilityObservable = BehaviorSubject.createDefault(false)
    private val mFinishActivityObservable = PublishSubject.create<Signal>()
    private val mFinishActivityWithResultObservable = PublishSubject.create<Signal>()
    private val mShowMessageObservable = PublishSubject.create<String>()

    val inputs: AuthorizationInputs = this
    val outputs: AuthorizationOutputs = this

    override fun onCreated() {
        super.onCreated()
        if (mFirebaseAuth.currentUser != null) {
            mFinishActivityWithResultObservable.emit()
        }
    }

    override fun actionBarBackButtonCliked() {
        mFinishActivityObservable.emit()
    }

    override fun loginButtonClicked() {
        val email = mEmailValueObservable.value
        val password = mPasswordValueObservable.value

        if (email.isNullOrEmpty()) {
            mShowMessageObservable.onNext("email is empty")
            return
        }

        if (password.isNullOrEmpty()) {
            mShowMessageObservable.onNext("password is empty")
            return
        }

        mLoginProgressBarVisibilityObservable.onNext(true)
        mFirebaseAuth.createUserWithEmailAndPassword(email, password).addOnFailureListener {
            mShowMessageObservable.onNext(it.message ?: "error")
        }.addOnSuccessListener {
            mFinishActivityWithResultObservable.emit()
        }.addOnCompleteListener {
            mLoginProgressBarVisibilityObservable.onNext(false)
        }
    }

    override fun emailWasChanged(email: String) {
        mEmailValueObservable.onNext(email)
    }

    override fun passwordWasChanged(password: String) {
        mPasswordValueObservable.onNext(password)
    }

    override fun emailValue(): Observable<String> {
        return mEmailValueObservable
    }

    override fun passwordValue(): Observable<String> {
        return mPasswordValueObservable
    }

    override fun loginProgressBarVisibilityState(): Observable<Boolean> {
        return mLoginProgressBarVisibilityObservable
    }

    override fun finishActivity(): Observable<Signal> {
        return mFinishActivityObservable
    }

    override fun finishActivityWithResult(): Observable<Signal> {
        return mFinishActivityWithResultObservable
    }

    override fun showMessage(): Observable<String> {
        return mShowMessageObservable
    }
}