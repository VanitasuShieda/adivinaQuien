package com.mika.adivinaquien.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mika.adivinaquien.databinding.GameMenuBinding
import com.mika.adivinaquien.dialogs.dialogLogin
import com.mika.adivinaquien.dialogs.dialogRegister
import com.mika.adivinaquien.dialogs.dialogUserInfo

class GameMenu: AppCompatActivity() {

    private lateinit var binding: GameMenuBinding
    private var usermail = ""

    private var db = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            binding = GameMenuBinding.inflate(layoutInflater)


        //recivimos array de intent
        intent.getStringExtra("User")?.let { usermail = it }

        binding.btnsologame.setOnClickListener{
            //incia actividad solo game
            finish()
        }

        binding.btnmultigame.setOnClickListener{
            val intent = Intent(this, UsersList::class.java)
            intent.putExtra("User", usermail)
            startActivity(intent)

            finish()
        }

        binding.btnlogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut();
            finish()
        }

        binding.usernick.setOnClickListener{
            val infoDialog = dialogUserInfo()
            infoDialog.show(supportFragmentManager, "anadir dialog")
        }

        setContentView(binding.root)
    }
}