package com.mika.adivinaquien.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mika.adivinaquien.databinding.GameMenuBinding
import com.mika.adivinaquien.dialogs.dialogUserInfo
import com.mika.adivinaquien.models.Game
import com.mika.adivinaquien.models.User
import java.util.*

class GameMenu: AppCompatActivity() {

    private lateinit var binding: GameMenuBinding
    private var usermail = ""

    private var db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            binding = GameMenuBinding.inflate(layoutInflater)


        //recivimos array de intent
        intent.getStringExtra("User")?.let { usermail = it }


        val refnick = db.collection("users").document(usermail).get()

        refnick.addOnSuccessListener { document ->
            if(document != null){
                binding.usernick.text =  document.data?.get("nick").toString()
            }else{
                println("Este es print de error")
            }
        }.addOnFailureListener{ exeption ->
            println(exeption)
        }

//
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

            val ref = db.collection("users").document(usermail).get()

            ref.addOnSuccessListener { document ->
                if(document != null){
                    println("Este es print de data ${document.data}")
                    val userinfo = User(
                        online = document.data?.get("online") as Boolean,
                        id = document.data?.get("id").toString(),
                        nick = document.data?.get("nick").toString(),
                        email = document.data?.get("email").toString(),
                        wins = document.getLong("wins")?.toInt()!!,
                        loses = document.getLong("loses")?.toInt()!!,
                        multiplayergames = emptyList()
                    )

                    val infoDialog = dialogUserInfo(userinfo)
                    infoDialog.show(supportFragmentManager, "anadir dialog")
                }else{
                    println("Este es print de error")
                }
            }.addOnFailureListener{ exeption ->
                println(exeption)
            }


        }

        setContentView(binding.root)
    }
}