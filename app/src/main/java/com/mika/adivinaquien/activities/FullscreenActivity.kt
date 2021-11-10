package com.mika.adivinaquien.activities

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mika.adivinaquien.databinding.ActivityFullscreenBinding
import android.app.Dialog
import android.net.Uri
import android.view.Window
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.firestore.ktx.firestore
import com.mika.adivinaquien.R
import com.mika.adivinaquien.databinding.DialogLoginBinding
import com.mika.adivinaquien.databinding.DialogRegisterBinding
import com.mika.adivinaquien.models.user
import java.util.*


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullscreenBinding
    private lateinit var bindinglogin: DialogLoginBinding
    private lateinit var bindingregistro: DialogRegisterBinding
    private val auth = Firebase.auth
    private var db = Firebase.firestore

    lateinit var  ImageURI: Uri

    private lateinit var fullscreenContent: TextView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        bindinglogin =  DialogLoginBinding.inflate(layoutInflater)
        bindingregistro =  DialogRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = binding.fullscreenContent
        fullscreenContent.setOnClickListener { toggle() }

        fullscreenContentControls = binding.fullscreenContentControls

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        binding.dummyButton.setOnTouchListener(delayHideTouchListener)


        binding.loginactivity.setOnClickListener {

            val builddialog =  AlertDialog.Builder(this)
            val viewdialog =  layoutInflater.inflate( R.layout.dialog_login, null)

            builddialog.setView(viewdialog)
            val dialog = builddialog.create()
            dialog.show()


            }




        binding.registeractivity.setOnClickListener {

            val buldialreg =  AlertDialog.Builder(this)
            val viewdialreg =  layoutInflater.inflate( R.layout.dialog_register, null)

            buldialreg.setView(viewdialreg)
            val dialogreg = buldialreg.create()
            dialogreg.show()

            bindingregistro.btnselectimg.setOnClickListener {
                    val intentimg = Intent()
                intentimg.type = "images/*"
                intent.action = Intent.ACTION_GET_CONTENT

                startActivity(intentimg)

            }

            bindingregistro.registercheck.setOnClickListener {
                if(bindingregistro.newemail.text.toString() != ""
                    && bindingregistro.newpassword.text.toString() != ""
                    && bindingregistro.newnick.text.toString() != ""
                    && bindingregistro.newpassword.text != bindingregistro.newpassword2.text ){
                    createUser()
                }else{
                    if(bindingregistro.newnick.text.toString() == ""){
                        Toast.makeText(baseContext, "El Usuario No puede Estar En blanco", Toast.LENGTH_LONG).show()
                    }else if(bindingregistro.newpassword.text.toString() != bindingregistro.newpassword2.text.toString()){
                        Toast.makeText(baseContext, "Las contrasenias no Coinciden", Toast.LENGTH_LONG).show()
                    }else if(bindingregistro.newemail.text.toString() == ""){
                        Toast.makeText(baseContext, "El Correo No puede Estar En blanco", Toast.LENGTH_LONG).show()
                    }
                }
            }

        }


        checkUser()
    }//end onCreate

    private fun checkUser(){
        val currentUser = auth.currentUser

        if(currentUser != null){
            //manda a llamar al juego
            val intent = Intent(this, GameMenu::class.java)

            //creamos array

            intent.putExtra("user", currentUser.email)
            //se manda
            startActivity(intent)

            finish()
        }
    }

    private fun createUser(){
        val newemail = bindingregistro.newemail.text.toString()
        val password = bindingregistro.newpassword.text.toString()

        auth.createUserWithEmailAndPassword(newemail, password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                val Uid = UUID.randomUUID().toString()
                val userinfo = user(
                    id = Uid,
                    nick = bindingregistro.newnick.text.toString(),
                    email = newemail,
                    wins = 0,
                    loses = 0,
                    multiplayergames = listOf("Sin Partidas","Jugadas"))

                db.collection("Users").document(newemail).set(userinfo)
                checkUser()
            } else {
                task.exception?.let {
                    Toast.makeText(baseContext, it.message, Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    private fun loginUser(email: String, password: String){

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                checkUser()
            } else {
                task.exception?.let {
                    Toast.makeText(baseContext, it.message, Toast.LENGTH_LONG).show()
                }
            }

        }
    }




    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}