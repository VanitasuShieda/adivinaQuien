package com.mika.adivinaquien.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.core.view.iterator
import androidx.core.view.size
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mika.adivinaquien.R
import com.mika.adivinaquien.adapters.UsersAdapter
import com.mika.adivinaquien.databinding.ActivityUsersListBinding
import com.mika.adivinaquien.models.Game
import com.mika.adivinaquien.models.User
import kotlinx.coroutines.flow.flowOf
import java.util.*

class UsersList:  AppCompatActivity() {
    private lateinit var binding: ActivityUsersListBinding
    private var db = Firebase.firestore
    private var user = ""
    private var listGames = mutableListOf(Game("","", emptyList()))
    private var itemant = -1

    private val adaptador = UsersAdapter(listGames){
        val items: RecyclerView = findViewById(R.id.listGamesRecyclerView)
        if( items[it].background==null){
            //Solo permite una seleccion a su vez
                // nueva seleccion
            items[it].setBackgroundResource(R.drawable.seleccionado)

            if(itemant != -1)
                items[itemant].setBackground(null)

            itemant = it
        }else if(items[it].background != null){
            //doble click inicia el juego
            itemant= -1
            //quita la seleccion
            items[it].setBackground(null)

            gameSelected(listGames[it])
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent.getStringExtra("User")?.let { user = it }
        listGames.removeAt(0)

        val userRef = db.collection("users").document(user).collection("games").get()
            .addOnSuccessListener { documentos ->
                if(documentos != null){
                    for(user in documentos){
                        user.data
                        val game = Game(
                             id  = user.data?.get("id").toString(),
                            name = user.data?.get("name").toString(),
                            users = listOf(user.data?.get("users").toString())
                        )
                        listGames.add(game)
                    }
                }else{
                    Log.d("User", "No hay, en teoria")
                }
            }


        if (user.isNotEmpty()){
            initViews()
        }
    }

    private fun initViews(){

        binding.newGameButton.setOnClickListener { newGame() }

        binding.listGamesRecyclerView.setHasFixedSize(false)
        binding.listGamesRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)
        binding.listGamesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.listGamesRecyclerView.adapter = adaptador


        val userRef = db.collection("users").document(user)

        userRef.collection("games")
            .get()
            .addOnSuccessListener { games ->
                val listChats = games.toObjects(Game::class.java)
                (binding.listGamesRecyclerView.adapter as UsersAdapter).setData(listChats)
            }

        userRef.collection("games")
            .addSnapshotListener { games, error ->
                if(error == null){
                    games?.let {
                        val listChats = it.toObjects(Game::class.java)

                        (binding.listGamesRecyclerView.adapter as UsersAdapter).setData(listChats)
                    }
                }
            }
    }

    private fun gameSelected(game: Game){
        val intent = Intent(this, GameMultiplayer::class.java)
        intent.putExtra("gameId", game.id)
        intent.putExtra("User", user)
        startActivity(intent)
    }

    private fun newGame(){
        val gameId = UUID.randomUUID().toString()
        val otherUser = binding.newGameText.text.toString().lowercase()
        val users = listOf(user, otherUser)
        val users2 = listOf(otherUser, user)

        val game = Game(
            id = gameId,
            name = "Juego con $otherUser",
            users = users
        )

        val gameother = Game(
            id = gameId,
            name = "Juego contra $user",
            users = users2
        )

        db.collection("games").document(gameId).set(game)
        db.collection("users").document(user).collection("games").document(gameId).set(game)
        db.collection("users").document(otherUser).collection("games").document(gameId).set(gameother)

        val intent = Intent(this, GameMultiplayer::class.java)
        intent.putExtra("gameId", gameId)
        intent.putExtra("User", user)
        startActivity(intent)
    }
}