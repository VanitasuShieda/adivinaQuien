package com.mika.adivinaquien.dialogs

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.mika.adivinaquien.R
import com.mika.adivinaquien.models.Player

//muestra animación de traslación y da los resultados del piedra papel o tijera
class DialogDuelTurn (context: Context, private val player1: Player, private val player2: Player): DialogFragment() {
    //interface de listener para la info que se recupera del dialog
    private lateinit var listener: DialogDuelTurnListener
    interface DialogDuelTurnListener{
        fun applyDuelTurn(player1: Player, player2: Player, isDraw: Boolean)
    }

    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            //vínculo con el layout duelturn_dialog.xml
            val binding = inflater.inflate(R.layout.duelturn_dialog, null)
            //para dar los resultados del piedra, papel o tijera
            resultados(binding)
            //botones del dialog (No hay botones)
            builder.setView(binding)
                .setTitle("Resultados")
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }
    //para funcionamiento del listener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as DialogDuelTurnListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implementDialogSelectMonsterListener"))
        }
    }

    fun resultados(binding: View){
        val choice1 =binding.findViewById<ImageView>(R.id.choice1_image)
        val choice2 =binding.findViewById<ImageView>(R.id.choice2_image)
        val result =binding.findViewById<TextView>(R.id.result_textView)
        val result2 =binding.findViewById<TextView>(R.id.result2_textView)

        //Para realizar la animación de traslación de las imagenes
        animacion(choice1, player1,100f)
        animacion(choice2, player2,-100f)
        //Después de 15050 milisegundos se dan los resultados
        Handler(Looper.getMainLooper()).postDelayed({
            if(player1.getPpt() == player2.getPpt()){
                result.text="¡Es un empate!"
                closeDialog(true)

            }
            if((player1.getPpt()==0 && player2.getPpt()==1) || (player1.getPpt()==1 && player2.getPpt()==2) || (player1.getPpt()==2 && player2.getPpt()==0) ){
                result.text="¡Perdiste!"
                result2.text="El jugador 2 comienza primero"
                player1.setIsFirst(false)
                player2.setIsFirst(true)
                closeDialog(false)
            }
            if((player1.getPpt() == 0 && player2.getPpt() == 2) || (player1.getPpt() == 1 && player2.getPpt() == 0) || (player1.getPpt() == 2 && player2.getPpt() == 1)){
                result.text="¡Ganaste!"
                result2.text="El jugador 1 comienza primero"
                player1.setIsFirst(true)
                player2.setIsFirst(false)
                closeDialog(false)
            }
        }, 3050)
    }

    private fun closeDialog(isDraw: Boolean) {
        Handler(Looper.getMainLooper()).postDelayed({
            listener.applyDuelTurn(player1,player2,isDraw)
            val d = dialog as AlertDialog?
            d?.dismiss()
        }, 1500)
    }

    fun animacion(choice: ImageView, player: Player, translation: Float) {
        when(player.getPpt()){
            0 -> choice.setImageResource(R.drawable.rock)
            1 -> choice.setImageResource(R.drawable.paper)
            2 -> choice.setImageResource(R.drawable.scissors)
        }
        ObjectAnimator.ofFloat(choice, "translationX", translation).apply {
            duration = 1000
            start()
        }
    }
}