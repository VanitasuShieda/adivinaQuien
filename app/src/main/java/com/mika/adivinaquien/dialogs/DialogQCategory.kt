package com.mika.adivinaquien.dialogs

import android.app.ActionBar
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mika.adivinaquien.R
import com.mika.adivinaquien.adapters.AdaptadorAttributes
import com.mika.adivinaquien.models.Attribute

//Dialog que muestra la lista de categorías o de atributos, esto dependiendo del itemType
class DialogQCategory (context: Context, private val itemType:Int): DialogFragment() {
    //interface de listener para la info que se recupera del dialog
    private lateinit var listener: DialogQCategoryListener
    interface DialogQCategoryListener {
        fun applyQCategory(itemType:Int, inDialog: String)
    }
    //variables para el efecto de sonido
    private val audioAttributes = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
    private  val sp: SoundPool =
        SoundPool.Builder().setMaxStreams(6).setAudioAttributes(audioAttributes).build()
    private val pop2: Int = sp.load(context,R.raw.pop2,1)

    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            var inDialog="" //Para identificar en que dialog nos encontramos y cual lista se mostrará
            var titulo:String=""//Texto del título de dialog el cual cambia dependiendo del itemType
            var boton: String="Volver"//Texto del botón el cual cambia dependiendo del itemType
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            //vínculo con el layout qcategory_dialog.xml
            val binding = inflater.inflate(R.layout.qcategory_dialog, null)
            when(itemType){//Determinar cual el el dialog en el que nos encontramos
                -1 -> inDialog="categories"
                0 -> inDialog="colors"
                1 -> inDialog="eyes"
                2 -> inDialog="head"
                3 -> inDialog="limbs"
                4 -> inDialog="expression"
            }
            //Se crea el adaptador y se define el evento de click
            val adaptador = AdaptadorAttributes(Attribute(),itemType){
                //Se asigna el evento click que selecciona una imagen
                onItemSelect(it, binding, inDialog)
            }
            val recView_question = binding.findViewById<RecyclerView>(R.id.recView_question)
            if(itemType==-1) {//(-1) -> para las categorías
                recView_question.layoutManager= GridLayoutManager(context, 5)
                titulo="Selecciona una categoría"
                boton="Cerrar"
            }else if(itemType==0) {//(0) -> para las colores
                recView_question.layoutManager= GridLayoutManager(context, 3)
                titulo="Colores"
            }else if(itemType==1) {//(1) -> para número de ojos
                recView_question.layoutManager= GridLayoutManager(context, 3)
                titulo="Número de ojos"
            }else if(itemType==2) {//(2) -> para la cabeza
                recView_question.layoutManager= GridLayoutManager(context, 4)
                titulo="Cabeza / Cara"
            }else if(itemType==3) {//(3) -> para las extremidades
                recView_question.layoutManager= GridLayoutManager(context, 3)
                titulo="Extremidades"
            }else if(itemType==4) {//(4) -> para las expresiones
                recView_question.layoutManager= GridLayoutManager(context, 5)
                titulo="Expresiones faciales"
            }
            recView_question.adapter = adaptador
            //Botones del dialog
            builder.setView(binding)
                .setTitle(titulo)
                .setNegativeButton(boton,
                    DialogInterface.OnClickListener { dialog, id ->
                        getDialog()?.dismiss()
                        if(boton=="Volver"){
                            listener.applyQCategory(-1,"volver")
                        }
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

    }
    //Para hacer más grande el dialog
    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = ActionBar.LayoutParams.MATCH_PARENT
        params.height = ActionBar.LayoutParams.WRAP_CONTENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }
    //Para funcionamiento del listener
    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as DialogQCategoryListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implementDialogSelectMonsterListener"))
        }
    }
    //Cuando se selecciona un ítem el dialog se cierra y se llama a applyQCategory
    fun onItemSelect(it: Int, binding: View, inDialog: String){
        //Se reproduce el sonido
        sp.play(pop2, 1f, 1f, 1, 0, 1f)
        val items: RecyclerView =binding.findViewById(R.id.recView_question)
        val d = dialog as AlertDialog?
        d?.dismiss()
        listener.applyQCategory(it,inDialog)
    }
}