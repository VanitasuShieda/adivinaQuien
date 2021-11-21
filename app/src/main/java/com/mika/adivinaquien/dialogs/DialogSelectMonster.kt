package com.mika.adivinaquien.dialogs

import android.app.ActionBar
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import androidx.core.view.get
import androidx.core.view.size
import com.mika.adivinaquien.R
import com.mika.adivinaquien.adapters.AdaptadorMonsters
import com.mika.adivinaquien.models.Player

//muestra el deck y se pide seleccionar un monstruo
class DialogSelectMonster(context: Context, private val player: Player): DialogFragment() {
    //interface de listener para la info que se recupera del dialog
    private lateinit var listener: DialogSelectMonsterListener
    interface DialogSelectMonsterListener {
        fun applySelectMonster(player: Player)
    }

    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            //vínculo con el layout selectmonsters_dialog.xml
            val binding = inflater.inflate(R.layout.selectmonsters_dialog, null)
            //Se crea el adaptador y se define el evento de click (para seleccionar al monstruo)
            val adaptador = AdaptadorMonsters(player,1){
                //Se asigna el evento click que selecciona una imagen
                onItemSelect(it, binding)
            }
            val recView_monstersSelect = binding.findViewById<RecyclerView>(R.id.recView_monstersSelect)
            recView_monstersSelect.layoutManager= GridLayoutManager(context, 10)
            recView_monstersSelect.adapter = adaptador
            //Botones del dialog
            builder.setView(binding)
                .setTitle("Selecciona tu monstruo")
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, id ->
                        listener.applySelectMonster(player)
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }
    //Al iniciar el dialog se deshabilita el botón 'ok'
    override fun onStart() {
        super.onStart()
        val d = dialog as AlertDialog?
        if (d != null) {
            val positiveButton: Button = d.getButton(Dialog.BUTTON_POSITIVE) as Button
            positiveButton.visibility=View.INVISIBLE
        }
    }
    //Para hacer más grande el dialog
    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = ActionBar.LayoutParams.MATCH_PARENT
        params.height = ActionBar.LayoutParams.MATCH_PARENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }
    //para funcionamiento del listener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as DialogSelectMonsterListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implementDialogSelectMonsterListener"))
        }
    }

    fun onItemSelect(it: Int, binding: View){
        val items: RecyclerView=binding.findViewById(R.id.recView_monstersSelect)
        if( items[it].background==null){
            //deselecciona el deck entero
            for (i in 0 until items.size) {
                items[i].background = null
                player.setCardChoiced(-1)
            }
            //selecciona al monstruo al que se le hizo click
            items[it].setBackgroundResource(R.drawable.seleccionado)
            player.setCardChoiced(player.getMyDeck()[it].id)
        }//quita la selección si se vuelve a hacer click
        else{
            items[it].background = null
            player.setCardChoiced(-1)
        }
        //Sí hay una elección se habilita el botón 'ok'
        val d = dialog as AlertDialog?
        if (d != null) {
            val positiveButton: Button = d.getButton(Dialog.BUTTON_POSITIVE) as Button
            if(player.getCardChoiced()==-1){
                positiveButton.visibility=View.INVISIBLE
            }else{positiveButton.visibility=View.VISIBLE}
        }
    }
}