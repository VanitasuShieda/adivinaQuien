package com.mika.adivinaquien.activities

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mika.adivinaquien.BuildConfig
import com.mika.adivinaquien.R
import com.mika.adivinaquien.databinding.ActivityFullscreenBinding
import com.mika.adivinaquien.databinding.DialogLoginBinding
import com.mika.adivinaquien.databinding.DialogRegisterBinding
import com.mika.adivinaquien.dialogs.dialogLogin
import com.mika.adivinaquien.dialogs.dialogRegister
import com.mika.adivinaquien.models.User
import java.util.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() , dialogRegister.dialgoRegisterListener,
    dialogLogin.dialogLoginListener{
    private lateinit var bindinglogin: DialogLoginBinding
    private lateinit var bindingregistro: DialogRegisterBinding
    private val auth = Firebase.auth
    private var db = Firebase.firestore
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mReference: StorageReference


    private lateinit var binding: ActivityFullscreenBinding
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
        bindinglogin = DialogLoginBinding.inflate(layoutInflater)
        bindingregistro = DialogRegisterBinding.inflate(layoutInflater)
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
            val loginDialog =     dialogLogin()
            loginDialog.show(supportFragmentManager, "anadir dialog")

        }


        binding.registeractivity.setOnClickListener {
            val registerDialog = dialogRegister()
            registerDialog.show(supportFragmentManager, "anadir dialog")
        }


        checkUser()

    }

    override fun applyLogin(email: String, pass: String) {
        loginUser(email, pass)
        val currentUser = auth.currentUser

        if (currentUser != null) {
            Toast.makeText(baseContext, "Inicio Exitoso", Toast.LENGTH_LONG).show()
        }
    }

    override fun applyReg(nick: String, email: String, pass: String, uri:  Uri) {

        val Uid = UUID.randomUUID().toString()
        val userinfo = User(
                    online = true,
                    id = Uid,
                    nick = nick,
                    email = email,
                    solowins = 0,
                    sololoses = 0,
                    multiwins = 0,
                    multiloses = 0,
                    multiplayergames = listOf("Sin Partidas", "Jugadas")
                )

                db.collection("users").document(email.lowercase()).set(userinfo)
                // intent.putExtra("ID", Uid)


                createUser(email.lowercase(), pass, uri)

    }

    private fun checkUser() {
        val currentUser = auth.currentUser

        if (currentUser != null) {

            val intent = Intent(this, GameMenu::class.java)
            intent.putExtra("User", currentUser.email)

            startActivity(intent)

            finish()
        }
    }

    private fun checkUser(uri: Uri, email: String) {
        val currentUser = auth.currentUser



        if (currentUser != null) {

            mStorage = FirebaseStorage.getInstance()
            mReference = mStorage.reference

            val imgRef = mReference.child("images/$email")

            imgRef.putFile(uri).addOnSuccessListener {
                Toast.makeText(this, "Succesfuly upload", Toast.LENGTH_SHORT)
                    .show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to Upload", Toast.LENGTH_SHORT)
                    .show()
            }

            val intent = Intent(this, GameMenu::class.java)
            intent.putExtra("User", currentUser.email)

            startActivity(intent)

            finish()
        }
    }

    private fun createUser(email: String, pass: String, uri: Uri) {

        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                checkUser(uri, email)
            } else {
                task.exception?.let {
                    Toast.makeText(baseContext, it.message, Toast.LENGTH_LONG).show()
                }
            }

        }
    }

    private fun loginUser(email: String, password: String) {

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
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