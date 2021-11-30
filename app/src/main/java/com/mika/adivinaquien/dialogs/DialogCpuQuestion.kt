package com.mika.adivinaquien.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.mika.adivinaquien.R
import com.mika.adivinaquien.models.Player

//dialog que muestra la pregunta de la CPU, se pide responder entre sí o no
class DialogCpuQuestion (context: Context, private val player1: Player, private val player2: Player, private val question: String, private val indexQ:Int): DialogFragment() {
    //interface de listener para la info que se recupera del dialog
    private lateinit var listener: DialogCpuQuestionListener
    interface DialogCpuQuestionListener{
        fun applyDialogCpuQuestion(res: String, indexQ:Int)
    }

    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            //vínculo con el layout cpuquestion_dialog.xml
            val binding = inflater.inflate(R.layout.cpuquestion_dialog, null)
            val msgWho= binding.findViewById<TextView>(R.id.msgWho_textView)
            val msgQuestion= binding.findViewById<TextView>(R.id.msgQuestion_textView)
            val monster_image= binding.findViewById<ImageView>(R.id.monster_image)
            msgWho.text="${player2.getNickname()} pregunta:"
            msgQuestion.text=question
            monster_image.setImageResource(player1.getMonsterList()[player1.getCardChoiced()].imagen)
            //Botones del dialog
            builder.setView(binding)
                .setPositiveButton("Sí",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.applyDialogCpuQuestion("Sí",indexQ)
                        dialog.dismiss()
                    })
                .setNegativeButton("No",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.applyDialogCpuQuestion("No",indexQ)
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
            listener = context as DialogCpuQuestionListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implementDialogSelectMonsterListener"))
        }
    }
}