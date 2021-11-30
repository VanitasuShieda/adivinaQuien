package com.example.guesswhosingleplayer

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

//dialog que muestra los resultados, quién ganó, tiene un itemType porque también lo puede llamar la CPU
class DialogResults (context: Context, private val player1: Player, private val player2: Player, private val itemType: Int): DialogFragment() {
    //interface de listener para la info que se recupera del dialog
    private lateinit var listener: DialogResultsListener
    interface DialogResultsListener{
        fun applyDialogResolve(res: String, player1wins: Boolean)
    }

    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            //Para indicar si es una victoria o derrota para el player1
            var player1wins: Boolean = false
            //vínculo con el layout results_dialog.xml
            val binding = inflater.inflate(R.layout.results_dialog, null)
            val monster1 = binding.findViewById<ImageView>(R.id.monster1_image)
            val monster2 = binding.findViewById<ImageView>(R.id.monster2_image)
            val player1Txt = binding.findViewById<TextView>(R.id.player1_textView)
            val player2Txt = binding.findViewById<TextView>(R.id.player2_textView)
            val msgResults1 = binding.findViewById<TextView>(R.id.msgResults1_textView)
            val msgResults2 = binding.findViewById<TextView>(R.id.msgResults2_textView)
            player1Txt.text=player1.getNickname()
            player2Txt.text=player2.getNickname()
            monster1.setImageResource(player1.getMonsterList()[player1.getCardChoiced()].imagen)
            monster2.setImageResource(player2.getMonsterList()[player2.getCardChoiced()].imagen)
            if(itemType==0){//Si lo llamó el jugador
                if(player1.getCardChoicedAnswer()==player2.getCardChoiced()){
                    msgResults1.text="FELICIDADES\t 🤗"
                    msgResults2.text="HAS GANADO"
                    player1wins=true
                }else{
                    msgResults1.text="LASTIMA\t 😖"
                    msgResults2.text="HAS PERDIDO"
                    player1wins=false
                }
            }else if(itemType==1){//Si lo llamó la CPU
                if(player2.getCardChoicedAnswer()==player1.getCardChoiced()){
                    msgResults1.text="LASTIMA\t 😖"
                    msgResults2.text="HAS PERDIDO"
                    player1wins=false
                }else{
                    msgResults1.text="FELICIDADES\t 🤗"
                    msgResults2.text="HAS GANADO"
                    player1wins=true
                }
            }
            //Botones del dialog
            builder.setView(binding)
                .setPositiveButton("Terminar",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.applyDialogResolve("Terminar",player1wins)
                        dialog.dismiss()
                    })
                .setNegativeButton("Nueva partida",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.applyDialogResolve("Nueva partida",player1wins)
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
            listener = context as DialogResultsListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implementDialogSelectMonsterListener"))
        }
    }
}