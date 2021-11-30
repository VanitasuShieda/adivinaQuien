package com.mika.adivinaquien.activities

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mika.adivinaquien.R
import com.mika.adivinaquien.adapters.MessageAdapter
import com.mika.adivinaquien.databinding.GameMultiplayBinding
import com.mika.adivinaquien.dialogs.dialogUserInfo
import com.mika.adivinaquien.models.Game
import com.mika.adivinaquien.models.Gameplay
import com.mika.adivinaquien.models.Message

class GameMultiplayer : AppCompatActivity(){
    private var gameId = ""
    private var user = ""
    private  lateinit var binding: GameMultiplayBinding
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = GameMultiplayBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)


        intent.getStringExtra("gameId")?.let { gameId = it }
        intent.getStringExtra("User")?.let { user = it }


        binding.chatbtn.setOnClickListener {
            if(binding.chatgame.visibility  == View.VISIBLE){
                binding.chatgame.visibility = View.GONE
            }else{
                binding.chatgame.visibility = View.VISIBLE
            }
        }

        if(gameId.isNotEmpty() && user.isNotEmpty()) {
            initViews()
        }

        setContentView(binding.root)
    }

    private fun initViews(){
        binding.messagesRecylerView.layoutManager = LinearLayoutManager(this)
        binding.messagesRecylerView.adapter = MessageAdapter(user)

        binding.sendMessageButton.setOnClickListener { sendMessage() }

        val gameRef = db.collection("games").document(gameId)

        gameRef.collection("messages").orderBy("dob", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { messages ->
                val listMessages = messages.toObjects(Message::class.java)
                (binding.messagesRecylerView.adapter as MessageAdapter).setData(listMessages)
            }

        gameRef.collection("messages").orderBy("dob", Query.Direction.ASCENDING)
            .addSnapshotListener { messages, error ->
                if(error == null){
                    messages?.let {
                        val listMessages = it.toObjects(Message::class.java)
                        (binding.messagesRecylerView.adapter as MessageAdapter).setData(listMessages)
                    }
                }
            }
    }

    private fun sendMessage(){
        val message = Message(
            message = binding.messageTextField.text.toString(),
            from = user
        )

        db.collection("games").document(gameId).collection("messages").document().set(message)

        binding.messageTextField.setText("")


    }

    private  fun defWiner(otherplayer: String , result: String){
        val partida = Gameplay(
                vs= otherplayer,
                status = result,
            )

        db.collection("users").document(user).collection("Multiplayergames").document().set(partida)
    }
}

//en el juego multijugador
// if(has ganado){
//  defWiner(OtroJugador, "Victoria")
// else
// defWiner(otrojugador, "Derrota")

