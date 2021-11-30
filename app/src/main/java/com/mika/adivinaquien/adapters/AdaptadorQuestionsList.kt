package com.mika.adivinaquien.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mika.adivinaquien.R
import com.mika.adivinaquien.models.Questions

//Para mostrar el RecyclerView de la lista de preguntas realizadas
class AdaptadorQuestionsList(private val questionsList:MutableList<Questions>):
    RecyclerView.Adapter<AdaptadorQuestionsList.QuestionsListViewHolder>() {
    class QuestionsListViewHolder(item: View): RecyclerView.ViewHolder(item){
        //Se crea el RecyclerView almacenando los items de la lista en el layout
        var turn_txt  = item.findViewById(R.id.turn_textView) as TextView
        var whoasks_txt  = item.findViewById(R.id.whoasks_textView) as TextView
        var questions_txt = item.findViewById(R.id.questions_textView) as TextView
        var whoanswers_txt = item.findViewById(R.id.whoanswers_textView) as TextView
        var answer_txt = item.findViewById(R.id.answer_textView) as TextView
        var layout = item.findViewById<ConstraintLayout>(R.id.layout)
        fun bindMonster(question: Questions){
            turn_txt.text="Turno: ${question.turn}"
            whoasks_txt.text=question.whoAsks.getNickname()
            questions_txt.text="P: ${question.question}"
            whoanswers_txt.text=question.whoAnswers.getNickname()
            answer_txt.text="R: ${question.answer}"
            //Intercalar en el color de fondo
            if(question.turn%2==0){
                layout.setBackgroundColor(Color.parseColor("#B3B5D3EC"))
            }else{
                layout.setBackgroundColor(Color.parseColor("#B3A9B0D6"))
            }

        }
    }
    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int): QuestionsListViewHolder {
        val item: View  = LayoutInflater.from(parent.context)
                .inflate(R.layout.listitem_questions, parent, false) as ConstraintLayout
        return QuestionsListViewHolder(item)
    }
    //Para determinar el evento click de cada item
    override fun onBindViewHolder (holder: QuestionsListViewHolder, position: Int) {
        val question=questionsList[position]
        holder.bindMonster(question)
    }
    override fun getItemCount() = questionsList.size
}