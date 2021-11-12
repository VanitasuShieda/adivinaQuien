package com.mika.adivinaquien.dialogs

import androidx.fragment.app.DialogFragment
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import com.mika.adivinaquien.R

class dialogRegister: DialogFragment() {
    private lateinit var listener:dialgoRegisterListener
    interface dialgoRegisterListener {
        fun applyReg(nick: String, email: String, pass: String, imgFoto: Uri?)
    }

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

            binding.findViewById<Button>(R.id.registercheck).setOnClickListener {
                val newpas=binding.findViewById<EditText>(R.id.newpassword).text.toString()
                val newpa2=binding.findViewById<EditText>(R.id.newpassword2).text.toString()
                var check = binding.findViewById<ImageView>(R.id.checkpassword)

                if(newpas==newpa2){
                    //check.visibility = "visible"
                }
            }

            builder.setView(binding)
                .setTitle("Nueva Cuenta")
                .setPositiveButton("Registrar",
                    DialogInterface.OnClickListener { dialog, id ->
                        val newemail= binding.findViewById<EditText>(R.id.newemail).text.toString()
                        val newpas=binding.findViewById<EditText>(R.id.newpassword).text.toString()
                        val nick= binding.findViewById<EditText>(R.id.newnick).text.toString()

                        listener.applyReg(nick,newemail,newpas,theUri)
                    })
                .setNegativeButton("Cancelar",
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.cancel()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
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