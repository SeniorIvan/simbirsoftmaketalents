package ivan.simbirsoft.maketalents.utils

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import io.reactivex.Observable
import ivan.simbirsoft.maketalents.entities.UserEntity
import ivan.simbirsoft.maketalents.viewmodel.CommonError
import ivan.simbirsoft.maketalents.viewmodel.ViewModelError
import ivan.simbirsoft.maketalents.viewmodel.ViewModelThrowable

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
                        it.onError(ViewModelThrowable(CommonError.NOT_AUTH))
                    } else {
                        // TODO почему d-то? database? это вроде reference
                        val d = FirebaseDatabase.getInstance().reference
                        val uid = fireBaseUser.uid


                        d.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
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
            return create
        }

        fun uploadAvatar(avatarUri: Uri): Observable<Uri> {
            return Observable.create({ emitter ->
                try {
                    val fireBaseUser = FirebaseAuth.getInstance().currentUser

                    if (fireBaseUser == null) {
                        emitter.onError(ViewModelThrowable(CommonError.NOT_AUTH))
                    } else {
                        val uid = fireBaseUser.uid

                        val storageRef = FirebaseStorage.getInstance().reference
                        val mountainsRef = storageRef.child("avatars").child("$uid.jpg")

                        val uploadTask = mountainsRef.putFile(avatarUri)
                        uploadTask.addOnFailureListener({
                            emitter.onError(it)
                        }).addOnSuccessListener({ taskSnapshot ->
                            val downloadUrl = taskSnapshot.downloadUrl
                            if (downloadUrl != null) {
                                emitter.onNext(downloadUrl)
                                emitter.onComplete()
                            } else {
                                emitter.onError(ViewModelThrowable(object : ViewModelError {
                                    override fun message(): String {
                                        return "downloadUrl == null"
                                    }
                                }))
                            }
                        })
                    }
                } catch (e: Exception) {
                    val message = e.message
                    if (message != null) {
                        emitter.onError(ViewModelThrowable(object : ViewModelError {
                            override fun message(): String {
                                return message
                            }
                        }))
                    } else {
                        emitter.onError(ViewModelThrowable(CommonError.SOME_ERROR))
                    }
                }
            })
        }

        fun updateUserInformation(name: String,
                                  phoneNumber: String,
                                  email: String,
                                  avatarUrl: String,
                                  latitude: Double = 0.0,
                                  longitude: Double = 0.0): Observable<UserEntity> {
            return Observable.create({ emitter ->
                try {
                    val fireBaseUser = FirebaseAuth.getInstance().currentUser

                    if (fireBaseUser == null) {
                        emitter.onError(ViewModelThrowable(CommonError.NOT_AUTH))
                    } else {
                        val dataBase = FirebaseDatabase.getInstance().reference
                        val uid = fireBaseUser.uid

                        val user = UserEntity().also {
                            it.name = name
                            it.phoneNumber = phoneNumber
                            it.email = email
                            it.avatarUrl = avatarUrl
                            it.latitude = latitude
                            it.longitude = longitude
                        }

                        dataBase.child("users").child(uid).setValue(user).addOnSuccessListener {
                            emitter.onNext(user)
                            emitter.onComplete()
                        }.addOnFailureListener {
                            emitter.onError(it)
                        }
                    }
                } catch (e: Exception) {
                    val message = e.message
                    if (message != null) {
                        emitter.onError(ViewModelThrowable(object : ViewModelError {
                            override fun message(): String {
                                return message
                            }
                        }))
                    } else {
                        emitter.onError(ViewModelThrowable(CommonError.SOME_ERROR))
                    }
                }
            })
        }
    }
}