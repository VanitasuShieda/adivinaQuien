package com.mika.adivinaquien.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.mika.adivinaquien.R
import com.mika.adivinaquien.models.Player

//dialog que muestra la confirmación para resolver o la resolución de la CPU dependiendo del itemType
class DialogResolve (context: Context, private val player: Player, private val itemType: Int): DialogFragment() {
    //interface de listener para la info que se recupera del dialog
    private lateinit var listener: DialogResolveListener
    interface DialogResolveListener{
        fun applyDialogResolve(res: String,player: Player)
    }

    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            //vínculo con el layout resolve_dialog.xml
            val binding = inflater.inflate(R.layout.resolve_dialog, null)
            val msgResolve = binding.findViewById<TextView>(R.id.msgResolve_textView)
            val monster = binding.findViewById<ImageView>(R.id.monster_image)
            val whoResolve = binding.findViewById<TextView>(R.id.textView)
            val warning = binding.findViewById<TextView>(R.id.textView2)
            //Si es una confirmación de resolución del jugador
            if(itemType==0){
                msgResolve.text="Tu monstruo es ${player.getMonsterList()[player.getCardChoicedAnswer()].name}"
                monster.setImageResource(player.getMonsterList()[player.getCardChoicedAnswer()].imagen)
            }else{//Sí es la resolución de la CPU
                whoResolve.text="${player.getNickname()} resuelve"
                warning.visibility= View.INVISIBLE
                msgResolve.text="Tu monstruo es ${player.getMonsterList()[player.getCardChoicedAnswer()].name}"
                monster.setImageResource(player.getMonsterList()[player.getCardChoicedAnswer()].imagen)
            }

            //Botones del dialog
            if(itemType==0){//Si es una confirmación de resolución del jugador
                builder.setView(binding)
                    .setPositiveButton("Sí",
                        DialogInterface.OnClickListener { dialog, id ->
                            listener.applyDialogResolve("Si",player)
                            dialog.dismiss()
                        })
                    .setNegativeButton("No",
                        DialogInterface.OnClickListener { dialog, id ->
                            player.setCardChoicedAnswer(-1)
                            listener.applyDialogResolve("No",player)
                            dialog.dismiss()
                        })
            }else{//Sí es la resolución de la CPU
                builder.setView(binding)
                    .setPositiveButton("Ok",
                        DialogInterface.OnClickListener { dialog, id ->
                            listener.applyDialogResolve("Ok",player)
                            dialog.dismiss()
                        })
            }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }
    //para funcionamiento del listener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as DialogResolveListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implementDialogSelectMonsterListener"))
        }
    }
}