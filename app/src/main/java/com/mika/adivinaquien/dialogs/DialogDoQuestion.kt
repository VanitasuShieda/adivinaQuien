package com.mika.adivinaquien.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.mika.adivinaquien.R

////dialog en el que se muestra la pregunta que se le hará a la CPU y se pide una confirmación
class DialogDoQuestion (context: Context, private val question: String, private val itemType:Int, private val selected:Int): DialogFragment() {
  //interface de listener para la info que se recupera del dialog
    private lateinit var listener: DialogDoQuestionListener
    interface DialogDoQuestionListener{
        fun applyDialogDoQuestion(res: String,question: String, itemType:Int, selected:Int)
    }

    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            //vínculo con el layout doquestion_dialog.xml
            val binding = inflater.inflate(R.layout.doquestion_dialog, null)
            val msgQuestion =binding.findViewById<TextView>(R.id.msgQuestion_textView)
            msgQuestion.text=question
            //Botones del dialog
            builder.setView(binding)
                .setPositiveButton("Sí",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.applyDialogDoQuestion("Si",question,itemType,selected)
                        dialog.dismiss()
                    })
                .setNegativeButton("No",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.applyDialogDoQuestion("No",question,itemType,selected)
                        dialog.dismiss()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }

    //para funcionamiento del listener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as DialogDoQuestionListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implementDialogSelectMonsterListener"))
        }
    }
}