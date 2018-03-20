package ivan.simbirsoft.maketalents.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import ivan.simbirsoft.maketalents.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val auth = FirebaseAuth.getInstance()

        b.setOnClickListener {
            startActivityForResult(Intent(this, EditProfileActivity::class.java), 1)
//            if (auth.currentUser == null) {
//                auth.createUserWithEmailAndPassword("12", "213").addOnFailureListener {
//                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
//                }.addOnSuccessListener {
//                    Toast.makeText(this, it.user.uid, Toast.LENGTH_LONG).show()
//                }
//            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
        }
    }
}
