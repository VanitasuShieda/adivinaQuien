package com.mika.adivinaquien.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.SoundPool
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.mika.adivinaquien.R

//dialog que muestra la respuesta de la CPU después de hacerle una pregunta
class DialogAnswerCPU (context: Context, private val question: String, private val answer:Boolean): DialogFragment() {
    //variables para el efecto de sonido
    private val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
    private  val sp: SoundPool =
        SoundPool.Builder().setMaxStreams(6).setAudioAttributes(audioAttributes).build()
    private val yes: Int = sp.load(context,R.raw.yes,1)
    private val no: Int = sp.load(context,R.raw.no,1)
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
//                val mp =MediaPlayer.create(context,R.raw.yes)
//                mp.setVolume(1.0f, 1.0f);
//                mp.start()
//                sp.play(yes, 1f, 1f, 1, 0, 1f)
                msgAnswer.text="Respuesta: Sí"
            }else{
                //Se reproduce el sonido
//                val mp =MediaPlayer.create(context,R.raw.no)
//                mp.setVolume(1.0f, 1.0f);
//                mp.start()
//                sp.play(no, 1f, 1f, 1, 0, 1f)
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