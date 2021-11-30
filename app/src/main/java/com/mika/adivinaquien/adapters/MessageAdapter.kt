package com.mika.adivinaquien.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.mika.adivinaquien.R
import com.mika.adivinaquien.models.Message

class MessageAdapter(private val user: String): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var messages: List<Message> = emptyList()

    fun setData(list: List<Message>){
        messages = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_msg,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]

        if(user == message.from){
            holder.itemView.findViewById<ConstraintLayout>(R.id.myMessageLayout).visibility = View.VISIBLE
            holder.itemView.findViewById<ConstraintLayout>(R.id.otherMessageLayout).visibility = View.GONE

            holder.itemView.findViewById<TextView>(R.id.myMessageTextView).text = message.message
        } else {
            holder.itemView.findViewById<ConstraintLayout>(R.id.myMessageLayout).visibility = View.GONE
            holder.itemView.findViewById<ConstraintLayout>(R.id.otherMessageLayout).visibility = View.VISIBLE

            holder.itemView.findViewById<TextView>(R.id.othersMessageTextView).text = message.message
        }

    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class MessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
}