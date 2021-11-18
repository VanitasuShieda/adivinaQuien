package com.mika.adivinaquien.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mika.adivinaquien.R
import com.mika.adivinaquien.adapters.UsersAdapter
import com.mika.adivinaquien.databinding.UsersListBinding
import com.mika.adivinaquien.models.Chat
import java.util.*

class UsersList:  AppCompatActivity() {
    private lateinit var binding: UsersListBinding
    private var db = Firebase.firestore
    private var user = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.users_list)
        binding = UsersListBinding.inflate(layoutInflater)

        intent.getStringExtra("user")?.let { user = it }

        if (user.isNotEmpty()){
            initViews()
        }
    }

    private fun initViews(){
        binding.newGameButton.setOnClickListener { newChat() }

        binding.listGamesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.listGamesRecyclerView.adapter =
            UsersAdapter { chat ->
                chatSelected(chat)
            }

        val userRef = db.collection("users").document(user)

        userRef.collection("Chats")
            .get()
            .addOnSuccessListener { chats ->
                val listChats = chats.toObjects(Chat::class.java)

                (binding.listGamesRecyclerView.adapter as UsersAdapter).setData(listChats)
            }

        userRef.collection("Chats")
            .addSnapshotListener { chats, error ->
                if(error == null){
                    chats?.let {
                        val listChats = it.toObjects(Chat::class.java)

                        (binding.listGamesRecyclerView.adapter as UsersAdapter).setData(listChats)
                    }
                }
            }
    }

    private fun chatSelected(chat: Chat){
        val intent = Intent(this, GameMultiplayer::class.java)
        intent.putExtra("chatId", chat.id)
        intent.putExtra("User", user)
        startActivity(intent)
    }

    private fun newChat(){
        val chatId = UUID.randomUUID().toString()
        val otherUser = binding.newGameText.text.toString()
        val users = listOf(user, otherUser)

        val chat = Chat(
            id = chatId,
            name = "Chat con $otherUser",
            users = users
        )

        db.collection("Chats").document(chatId).set(chat)
        db.collection("users").document(user).collection("Chats").document(chatId).set(chat)
        db.collection("users").document(otherUser).collection("Chats").document(chatId).set(chat)

        val intent = Intent(this, GameMultiplayer::class.java)
        intent.putExtra("chatId", chatId)
        intent.putExtra("User", user)
        startActivity(intent)
    }
}