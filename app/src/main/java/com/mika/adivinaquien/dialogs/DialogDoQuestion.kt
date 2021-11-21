package com.mika.adivinaquien.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.mika.adivinaquien.R

class DialogDoQuestion (context: Context, private val question: String): DialogFragment() {
//    //interface de listener para la info que se recupera del dialog
//    private lateinit var listener: DialogDecideTurnListener
//    interface DialogDecideTurnListener{
//        fun applyDecideTurn(player: Player)
//    }

    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            //vínculo con el layout decideturn_dialog.xml
            val binding = inflater.inflate(R.layout.doquestion_dialog, null)
            val msgQuestion =binding.findViewById<TextView>(R.id.msgQuestion_textView)
            msgQuestion.text=question

            //Botones del dialog
            builder.setView(binding)
                .setPositiveButton("Sí",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
                .setNegativeButton("No",
                    DialogInterface.OnClickListener { dialog, id ->
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }
    //para funcionamiento del listener
//    override fun onAttach(context: Context) {
//        super.onAttach(context)
//        try {
//            // Instantiate the NoticeDialogListener so we can send events to the host
//            listener = context as DialogDecideTurnListener
//        } catch (e: ClassCastException) {
//            // The activity doesn't implement the interface, throw exception
//            throw ClassCastException((context.toString() +
//                    " must implementDialogSelectMonsterListener"))
//        }
//    }
}