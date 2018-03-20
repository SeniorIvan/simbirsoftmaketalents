package ivan.simbirsoft.maketalents.utils

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.Observable
import ivan.simbirsoft.maketalents.R
import ivan.simbirsoft.maketalents.entities.UserEntity
import ivan.simbirsoft.maketalents.viewmodel.CommonError
import ivan.simbirsoft.maketalents.viewmodel.ViewModelError
import ivan.simbirsoft.maketalents.viewmodel.ViewModelThrowable
import ivan.simbirsoft.maketalents.viewmodel.ViewModelUtils

/**
 * Created by Ivan Kuznetsov
 * on 20.03.2018.
 */
class FirebaseUtils {

    companion object {

        fun fetchUser(): Observable<UserEntity> {
            val create = Observable.create<UserEntity>({
                try {
                    val fireBaseUser = FirebaseAuth.getInstance().currentUser

                    if (fireBaseUser == null) {
                        it.onError(ViewModelThrowable(object : ViewModelError {
                            override fun message(): String {
                                return ViewModelUtils.sApplicationContext.getString(R.string.not_auth)
                            }
                        }))
                    } else {
                        val d = FirebaseDatabase.getInstance().reference
                        val uid = fireBaseUser.uid


                        d.child("users").child(uid).addListenerForSingleValueEvent(object  : ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                                it.onError(error.toException())
                            }

                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                val u = dataSnapshot.getValue(UserEntity::class.java)

                                if (u == null) {
                                    it.onNext(UserEntity())
                                } else {
                                    it.onNext(u)
                                }
                                it.onComplete()
                            }
                        })
                    }

                } catch (e: Exception) {
                    val message = e.message
                    if (message != null) {
                        it.onError(ViewModelThrowable(object : ViewModelError {
                            override fun message(): String {
                                return message
                            }
                        }))
                    } else {
                        it.onError(ViewModelThrowable(CommonError.SOME_ERROR))
                    }
                }
            })
            return create.flatMap {
                val missingText = lazy { ViewModelUtils.sApplicationContext.getString(R.string.missing) }

                if (it.name.isEmpty()) {
                    it.name = missingText.value
                }

                if (it.email.isEmpty()) {
                    it.email = missingText.value
                }

                if (it.phoneNumber.isEmpty()) {
                    it.phoneNumber = missingText.value
                }

                return@flatMap Observable.just(it);
            }
        }
    }
}