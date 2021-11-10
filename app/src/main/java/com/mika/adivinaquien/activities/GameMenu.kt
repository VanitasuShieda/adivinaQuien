package com.mika.adivinaquien.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mika.adivinaquien.R
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mika.adivinaquien.databinding.GameMenuBinding

class GameMenu: AppCompatActivity() {

    private lateinit var binding: GameMenuBinding
    private var usermail = ""

   // private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = GameMenuBinding.inflate(layoutInflater)


        //recivimos array de intent
        intent.getStringExtra("user")?.let {
            usermail = it
            binding.userinfo.text = usermail
        }

        binding.btnsologame.setOnClickListener{
            //incia actividad solo game
            finish()
        }
        binding.btnmultigame.setOnClickListener{
            //inicia actividad multiplayer
            finish()
        }

        setContentView(binding.root)
    }
}