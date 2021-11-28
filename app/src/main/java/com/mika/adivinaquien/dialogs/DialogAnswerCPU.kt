package com.mika.adivinaquien.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.mika.adivinaquien.R

//dialog que muestra la respuesta de la CPU después de hacerle una pregunta
class DialogAnswerCPU (context: Context, private val question: String, private val answer:Boolean): DialogFragment() {
    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            //vínculo con el layout answercpu_dialog.xml
            val binding = inflater.inflate(R.layout.answercpu_dialog, null)
            val msgQuestion = binding.findViewById<TextView>(R.id.msgQuestion_textView)
            val msgAnswer = binding.findViewById<TextView>(R.id.msgAnswer_textView)
            msgQuestion.text="Pregunta:\n$question"
            if(answer){
                msgAnswer.text="Respuesta: Sí"
            }else{
                msgAnswer.text="Respuesta: No"
            }
            //Botones del dialog
            builder.setView(binding)
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}