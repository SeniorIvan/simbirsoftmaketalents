package ivan.simbirsoft.maketalents.viewmodels

import android.net.Uri
import android.util.Patterns
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import ivan.simbirsoft.maketalents.R
import ivan.simbirsoft.maketalents.entities.UserEntity
import ivan.simbirsoft.maketalents.utils.FirebaseUtils
import ivan.simbirsoft.maketalents.utils.Signal
import ivan.simbirsoft.maketalents.utils.emit
import ivan.simbirsoft.maketalents.viewmodel.BaseDataLoadingViewModel
import ivan.simbirsoft.maketalents.viewmodel.ViewModelUtils

/**
 * Created by Ivan Kuznetsov
 * on 20.03.2018.
 */

interface EditProfileInputs {

    fun actionBarBackButtonClicked()
    fun saveButtonClicked()

    fun emailWasChanged(email: String)
    fun nameWasChanged(name: String)
    fun phoneNumberWasChanged(phoneNumber: String)

    fun avatarWasSelected(photoUri: Uri)
}

interface EditProfileOutputs {
    fun finishActivity(): Observable<Signal>
    fun finishActivityWithResult(): Observable<Signal>
    fun savingProgressBarVisibilityState(): Observable<Boolean>
    fun showMessage(): Observable<String>
    fun updateAvatar(): Observable<Uri>

    fun nameFieldErrorState(): Observable<Boolean>
    fun emailFieldErrorState(): Observable<Boolean>
}

class EditProfileViewModel : BaseDataLoadingViewModel<UserEntity>(), EditProfileInputs, EditProfileOutputs {

    private val mFinishActivityObservable = PublishSubject.create<Signal>()
    private val mFinishActivityWithResultObservable = PublishSubject.create<Signal>()
    private val mSavingProgressBarVisibilityObservable = BehaviorSubject.createDefault(false)
    private val mShowMessageObservable = PublishSubject.create<String>()
    private val mUpdateAvatarObservable = PublishSubject.create<Uri>()

    private val mNameFieldErrorStateObservable = BehaviorSubject.createDefault(false)
    private val mEmailFieldErrorStateObservable = BehaviorSubject.createDefault(false)

    private var mSavingUserInfoDisposable: Disposable? = null

    val inputs: EditProfileInputs = this
    val outputs: EditProfileOutputs = this

    override fun onCleared() {
        super.onCleared()
        mSavingUserInfoDisposable?.dispose()
    }

    override fun onCreateDataObservable(): Observable<UserEntity> {
        return FirebaseUtils.fetchUser()
    }

    override fun actionBarBackButtonClicked() {
        mFinishActivityObservable.emit()
    }

    override fun avatarWasSelected(photoUri: Uri) {
        mSavingUserInfoDisposable?.dispose()
        mSavingProgressBarVisibilityObservable.onNext(true)
        mSavingUserInfoDisposable = FirebaseUtils.uploadAvatar(photoUri)
                .doFinally {
                    mSavingProgressBarVisibilityObservable.onNext(false)
                }.subscribe({result ->
                    mData?.let {
                        it.avatarUrl = result.toString()
                    }
                    mUpdateAvatarObservable.onNext(result)
                }, { error ->
                    val m = error.message
                            ?: ViewModelUtils.sApplicationContext.getString(R.string.viewmodel_some_error)
                    mShowMessageObservable.onNext(m)
                })
    }

    override fun saveButtonClicked() {
        mData?.let {

            val isValidName  = !it.name.isEmpty()
            val isValidEmail = Patterns.EMAIL_ADDRESS.matcher(it.email).matches()

            mNameFieldErrorStateObservable.onNext(!isValidName)
            mEmailFieldErrorStateObservable.onNext(!isValidEmail)

            if (!isValidName || !isValidEmail) {
                return
            }

            mSavingUserInfoDisposable?.dispose()
            mSavingProgressBarVisibilityObservable.onNext(true)
            FirebaseUtils
                    .updateUserInformation(it.name, it.phoneNumber, it.email, it.avatarUrl)
                    .doFinally {
                        mSavingProgressBarVisibilityObservable.onNext(false)
                    }
                    .subscribe({
                        mFinishActivityWithResultObservable.onNext(Signal.Instance)
                    }, { error ->
                        val m = error.message
                                ?: ViewModelUtils.sApplicationContext.getString(R.string.viewmodel_some_error)
                        mShowMessageObservable.onNext(m)
                    })
        }
    }

    override fun emailWasChanged(email: String) {
        mData?.email = email
    }

    override fun nameWasChanged(name: String) {
        mData?.name = name
    }

    override fun phoneNumberWasChanged(phoneNumber: String) {
        mData?.phoneNumber = phoneNumber
    }

    override fun finishActivity(): Observable<Signal> {
        return mFinishActivityObservable
    }

    override fun finishActivityWithResult(): Observable<Signal> {
        return mFinishActivityWithResultObservable
    }

    override fun savingProgressBarVisibilityState(): Observable<Boolean> =
            mSavingProgressBarVisibilityObservable

    override fun showMessage(): Observable<String> = mShowMessageObservable

    override fun updateAvatar(): Observable<Uri> = mUpdateAvatarObservable

    override fun nameFieldErrorState(): Observable<Boolean> = mNameFieldErrorStateObservable

    override fun emailFieldErrorState(): Observable<Boolean> = mEmailFieldErrorStateObservable
}