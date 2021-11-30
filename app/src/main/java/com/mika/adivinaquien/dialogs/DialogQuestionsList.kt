package com.mika.adivinaquien.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mika.adivinaquien.R
import com.mika.adivinaquien.adapters.AdaptadorQuestionsList
import com.mika.adivinaquien.models.Questions

//dialog que muestra el recView con la lista de preguntas realizadas
class DialogQuestionsList(context: Context, private val questionsList:MutableList<Questions>): DialogFragment() {

    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            //vínculo con el layout questionslist_dialog.xml
            val binding = inflater.inflate(R.layout.questionslist_dialog, null)
            val noList=binding.findViewById<TextView>(R.id.noList_textView)
            //Sí la lista de preguntas esta vacía
            if(questionsList.size==0){
                noList.visibility=View.VISIBLE
                noList.text="Aún no hay preguntas"
            }else{
                //Se crea el adaptador
                val adaptador = AdaptadorQuestionsList(questionsList)
                val recView_questionList = binding.findViewById<RecyclerView>(R.id.recView_questionList)
                recView_questionList.layoutManager= LinearLayoutManager(context,LinearLayoutManager.VERTICAL, false)
                recView_questionList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
                recView_questionList.adapter = adaptador
            }
            //Botones del dialog
            builder.setView(binding)
                .setNegativeButton("Cerrar",
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.dismiss()
                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}