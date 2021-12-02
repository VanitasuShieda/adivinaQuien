package com.mika.adivinaquien.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.mika.adivinaquien.R
import com.mika.adivinaquien.models.Player

//dialog que muestra la opción de terminar y volver al menú o reiniciar partida
class DialogFinish (context: Context): DialogFragment() {
    //interface de listener para la info que se recupera del dialog
    private lateinit var listener: DialogFinishListener
    interface DialogFinishListener{
        fun applyDialogFinish(res: String)
    }

    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater
            //vínculo con el layout dialog_finish.xml
            val binding = inflater.inflate(R.layout.dialog_finish, null)
            val terminar = binding.findViewById<Button>(R.id.terminar_button)
            val reiniciar = binding.findViewById<Button>(R.id.reiniciar_button)
            terminar.setOnClickListener {
                listener.applyDialogFinish("Terminar")
            }
            reiniciar.setOnClickListener {
                listener.applyDialogFinish("Reiniciar")
            }
            //Botones del dialog
            builder.setView(binding)
                .setPositiveButton("Cancelar",
                    DialogInterface.OnClickListener { dialog, id ->
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
            listener = context as DialogFinishListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implementDialogSelectMonsterListener"))
        }
    }
}