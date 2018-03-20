package ivan.simbirsoft.maketalents.viewmodels

import com.google.firebase.auth.FirebaseAuth
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import ivan.simbirsoft.maketalents.entities.UserEntity
import ivan.simbirsoft.maketalents.utils.FirebaseUtils
import ivan.simbirsoft.maketalents.utils.Signal
import ivan.simbirsoft.maketalents.utils.emit
import ivan.simbirsoft.maketalents.viewmodel.BaseDataLoadingViewModel

/**
 * Created by Ivan Kuznetsov
 * on 20.03.2018.
 */

interface ProfileInputs {

    fun actionBarBackButtonCliked()
    fun logoutButtonClicked()
    fun editProfileButtonClicked()
}

interface ProfileOutputs {
    fun finishActivity(): Observable<Signal>
    fun openEditProfileActivity(): Observable<Signal>
}

class ProfileViewModel: BaseDataLoadingViewModel<UserEntity>(), ProfileInputs, ProfileOutputs {

    private val mFinishActivityObservable = PublishSubject.create<Signal>()
    private val mOpenEditProfileActivityObservable = PublishSubject.create<Signal>()

    val inputs: ProfileInputs = this
    val outputs: ProfileOutputs = this

    override fun onCreateDataObservable(): Observable<UserEntity> {
        return FirebaseUtils.fetchUser()
    }

    override fun actionBarBackButtonCliked() {
        mFinishActivityObservable.emit()
    }

    override fun logoutButtonClicked() {
        FirebaseAuth.getInstance().signOut()
        mFinishActivityObservable.emit()
    }

    override fun editProfileButtonClicked() {
        mOpenEditProfileActivityObservable.emit()
    }

    override fun finishActivity(): Observable<Signal> {
        return mFinishActivityObservable
    }

    override fun openEditProfileActivity(): Observable<Signal> {
        return mOpenEditProfileActivityObservable
    }
}