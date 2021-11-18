package com.mika.adivinaquien.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mika.adivinaquien.R
import com.mika.adivinaquien.models.Chat

class UsersAdapter(val chatClick: (Chat) -> Unit): RecyclerView.Adapter<UsersAdapter.ChatViewHolder>()  {

    var chats: List<Chat> = emptyList()

    fun setData(list: List<Chat>){
        chats = list
        notifyDataSetChanged()
    }


    class ChatViewHolder(item: View): RecyclerView.ViewHolder(item){
        val chatName = item.findViewById(R.id.chatNameText) as TextView
        val usersTexe = item.findViewById(R.id.usersTextView) as TextView
        fun bindChat(chat: Chat){
            chatName.text=chat.name
            usersTexe.text=chat.users.toString()
        }
    }

    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int): ChatViewHolder {
        val item = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_users, parent, false)
        return ChatViewHolder(item)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val chat = chats[position]
        holder.bindChat(chat)
        holder.itemView.setOnClickListener {
            chatClick(chats[position])
        }
    }

    override fun getItemCount(): Int {
        return chats.size
    }


}