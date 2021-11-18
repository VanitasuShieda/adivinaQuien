package com.mika.adivinaquien.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mika.adivinaquien.R
import com.mika.adivinaquien.models.Chat

class UsersAdapter(val chatClick: (Chat) -> Unit): RecyclerView.Adapter<UsersAdapter.UsersViewHolder>()  {

    var chats: List<Chat> = emptyList()

    fun setData(list: List<Chat>){
        chats = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return UsersViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_users,parent,false            )
        )
    }

    override fun onBindViewHolder(holder: UsersViewHolder, position: Int) {

        holder.itemView.findViewById<TextView>(R.id.chatNameText).text = chats[position].name
        holder.itemView.findViewById<TextView>(R.id.usersTextView).text = chats[position].users.toString()

        holder.itemView.setOnClickListener {
            chatClick(chats[position])
        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    class UsersViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
}