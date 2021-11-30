package com.mika.adivinaquien.dialogs

import android.app.ActionBar
import androidx.fragment.app.DialogFragment
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.mika.adivinaquien.BuildConfig
import com.mika.adivinaquien.R
import com.mika.adivinaquien.models.Game

class dialogRegister: DialogFragment() {
    private lateinit var listener:dialgoRegisterListener
    interface dialgoRegisterListener {
        fun applyReg(nick: String, email: String, pass: String, theUri: Uri)
    }

    private val pkN = BuildConfig.APPLICATION_ID
    private var uri = Uri.parse("android.resource://$pkN/${R.drawable.user}")
    private var db = Firebase.firestore



    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val binding = inflater.inflate(R.layout.dialog_register, null)
            var theUri: Uri?=null
            val getContent = registerForActivityResult(ActivityResultContracts.GetContent())  { uri: Uri? ->
                binding.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.newimage).setImageBitmap(null)
                binding.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.newimage).setImageURI(null)
                binding.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.newimage).setImageURI(uri)
                if (uri != null) {
                    theUri= uri
                }
            }
            binding.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.newimage).setOnClickListener{
                getContent.launch("image/*")
            }
            binding.findViewById<Button>(R.id.btnselectimg).setOnClickListener {
                getContent.launch("image/*")
            }




            binding.findViewById<Button>(R.id.btncheck).setOnClickListener {
                val d = dialog as AlertDialog?
                if (d != null) {
                    val positiveButton: Button = d.getButton(Dialog.BUTTON_POSITIVE) as Button
                    val newemail= binding.findViewById<EditText>(R.id.newemail).text.toString()
                    val newpas = binding.findViewById<EditText>(R.id.newpassword).text.toString()
                    val newpas2 = binding.findViewById<EditText>(R.id.newpassword2).text.toString()
                    val newnick= binding.findViewById<EditText>(R.id.newnick).text.toString()
                    val checkpass2 = binding.findViewById<ImageView>(R.id.checkpass2)
                    val checkemail = binding.findViewById<ImageView>(R.id.checkemail)
                    val alertemail = binding.findViewById<TextView>(R.id.alertemail)
                    val alertpass2 = binding.findViewById<TextView>(R.id.alerpass)
                    var exist = false

                    //comprobacion
                    if(newemail != ""){
                        val refnick = db.collection("users").get()

                        refnick.addOnSuccessListener { documentos ->

                                for(user in documentos){
                                    if(newemail.lowercase() == user.id){
                                        exist=true
                                        break
                                    }
                                }

                            if(exist){
                                alertemail.text = "El correo ya existe"
                                alertemail.visibility = View.VISIBLE
                            }else{
                                alertemail.visibility = View.GONE
                                checkemail.visibility = View.VISIBLE
                            }

                        }.addOnFailureListener{ exeption ->
                            println(exeption)
                        }
                    }else if(newemail == ""){
                        alertemail.text = "El Correo No puede estar en blanco"
                        alertemail.visibility = View.VISIBLE
                    }

                    if(newpas == newpas2 && newpas != "" && newpas2 != ""){
                        checkpass2.visibility = View.VISIBLE
                        alertpass2.visibility = View.GONE
                    }else{
                        alertpass2.visibility = View.VISIBLE
                        if(newpas == ""){
                            alertpass2.text = "La contrasenia No puede estar Vacia"
                        }else if(newpas2 == ""){
                            alertpass2.text = "Porfavor Repite La contrasenia"
                        }
                    }

                    if(newnick != "" && newemail != "" && checkpass2.isVisible ){
                        alertemail.visibility = View.GONE
                        alertpass2.visibility = View.GONE
                        positiveButton.visibility=View.VISIBLE
                    }else{
                        positiveButton.visibility=View.INVISIBLE
                    }
                }
            }



            builder.setView(binding)
                .setPositiveButton("Registrar",
                    DialogInterface.OnClickListener { dialog, id ->
                        val newnick= binding.findViewById<EditText>(R.id.newnick).text.toString()
                        val newemail= binding.findViewById<EditText>(R.id.newemail).text.toString()
                        val newpas = binding.findViewById<EditText>(R.id.newpassword).text.toString()

                        if(theUri != null){
                            listener.applyReg(newnick,newemail,newpas, theUri!!)
                        }else{
                            listener.applyReg(newnick,newemail,newpas, uri)
                        }


                    })
                .setNegativeButton("Cancelar",
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = ActionBar.LayoutParams.MATCH_PARENT
        params.height = ActionBar.LayoutParams.MATCH_PARENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }

    override fun onStart() {
        super.onStart()
        val d = dialog as AlertDialog?
        if (d != null) {
            val positiveButton: Button = d.getButton(Dialog.BUTTON_POSITIVE) as Button
            positiveButton.visibility= View.INVISIBLE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as dialgoRegisterListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implementAnadirDialogListener"))
        }
    }

}