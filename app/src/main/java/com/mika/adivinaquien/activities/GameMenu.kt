package com.mika.adivinaquien.activities

import android.content.ComponentCallbacks2
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mika.adivinaquien.databinding.GameMenuBinding
import com.mika.adivinaquien.dialogs.dialogUserInfo
import com.mika.adivinaquien.models.User
import java.util.*
import com.bumptech.glide.request.RequestOptions
import com.mika.adivinaquien.R
import java.io.File


class GameMenu: AppCompatActivity(){

    private lateinit var binding: GameMenuBinding
    private var usermail = ""
    private var bitmap: Bitmap? = null
    private var userinfo = User(java.lang.Boolean.TRUE,"","","",0,0,0,0 )

    private lateinit var mStorage: FirebaseStorage
    private lateinit var mReference: StorageReference
    private var db = FirebaseFirestore.getInstance()

    //Reproducción de música
    private lateinit var mp: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = GameMenuBinding.inflate(layoutInflater)

        intent.getStringExtra("User")?.let { usermail = it }

        mStorage = FirebaseStorage.getInstance()
        mReference = mStorage.reference

        //Reproducción de música
        mp = MediaPlayer.create(this, R.raw.menus)
        mp.setVolume(0.5f, 0.5f)
        mp.start()
        mp.isLooping = true

  //imagen del usuario
        val refnick = db.collection("users").document(usermail).get()

        refnick.addOnSuccessListener { document ->
            if(document != null){
                binding.usernick.text =  document.data?.get("nick").toString()
                val imgRef = mReference.child("images/$usermail")
                val localfile = File.createTempFile("tempImg","jpg")

                imgRef.getFile(localfile).addOnSuccessListener {
                    bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                    val options = RequestOptions()
                    options.centerCrop().fitCenter()
                    Glide.with(this@GameMenu).load(bitmap).apply(options).into(binding.myimagemenu)

                }
            }else{
                println("Este es print de error")
            }
        }.addOnFailureListener{ exeption ->
            println(exeption)
        }

//optiene informacion del usuario
        val ref = db.collection("users").document(usermail).get()

        ref.addOnSuccessListener { document ->
            if(document != null){

                 userinfo = User(
                    online = document.data?.get("online") as Boolean,
                    id = document.data?.get("id").toString(),
                    nick = document.data?.get("nick").toString(),
                    email = document.data?.get("email").toString(),
                    solowins = document.getLong("solowins")?.toInt()!!,
                    sololoses = document.getLong("sololoses")?.toInt()!!,
                    multiwins = document.getLong("multiwins")?.toInt()!!,
                    multiloses = document.getLong("multiloses")?.toInt()!!,
                )

            }else{
                println("Este es print de error")
            }
        }.addOnFailureListener{ exeption ->
            println(exeption)
        }


//
        binding.btnsologame.setOnClickListener{
            val intent = Intent(this, GameSolo::class.java)
            intent.putExtra("User", usermail)
            mp.stop()
            startActivity(intent)

            finish()
        }

        binding.btnmultigame.setOnClickListener{
            val intent = Intent(this, UsersList::class.java)
            intent.putExtra("User", usermail)
            intent.putExtra("Nick", userinfo.nick)
            startActivity(intent)

            finish()
        }

        binding.btnlogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut();
            finish()
        }

        binding.usernick.setOnClickListener{
            val infoDialog = dialogUserInfo(userinfo, bitmap)
            infoDialog.show(supportFragmentManager, "anadir dialog")
        }

        setContentView(binding.root)
    }
    override fun onPause() {
        super.onPause()
        mp.stop()
    }

}