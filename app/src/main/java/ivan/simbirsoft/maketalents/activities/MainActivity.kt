package ivan.simbirsoft.maketalents.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import ivan.simbirsoft.maketalents.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val auth = FirebaseAuth.getInstance()

        b.setOnClickListener {
            startActivity(Intent(this, AuthorizationActivity::class.java))
//            if (auth.currentUser == null) {
//                auth.createUserWithEmailAndPassword("12", "213").addOnFailureListener {
//                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
//                }.addOnSuccessListener {
//                    Toast.makeText(this, it.user.uid, Toast.LENGTH_LONG).show()
//                }
//            }
        }


//        val providers = listOf((AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build()))
//
//        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).build(), 1)
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        if (requestCode == 1) {
//            if (resultCode == Activity.RESULT_OK) {
//                val user = FirebaseAuth.getInstance().currentUser
//
//                if (user != null) {
//                    Toast.makeText(this, "ok", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
//                }
//            } else {
//                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
}
