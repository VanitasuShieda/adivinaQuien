package com.mika.adivinaquien.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.mika.adivinaquien.R

class dialogLogin : DialogFragment() {
    private lateinit var listener: dialogLoginListener

    interface dialogLoginListener {
        fun applyLogin(email: String, pass: String)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            val binding = inflater.inflate(R.layout.dialog_login, null)


            builder.setView(binding)
                .setTitle("Inicio de Sesion")
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, id ->
                        val email = binding.findViewById<EditText>(R.id.emailText).text.toString()
                        val pass = binding.findViewById<EditText>(R.id.passwordText).text.toString()

                        listener.applyLogin(email, pass)
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
            listener = context as dialogLoginListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException(
                (context.toString() +
                        " must implementAnadirDialogListener")
            )
        }
    }
}