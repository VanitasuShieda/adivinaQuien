package com.mika.adivinaquien.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.system.Os.remove
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.mika.adivinaquien.R
import com.mika.adivinaquien.adapters.GamesListAdapter
import com.mika.adivinaquien.adapters.UsersAdapter
import com.mika.adivinaquien.databinding.ActivityUsersListBinding
import com.mika.adivinaquien.models.Game
import com.mika.adivinaquien.models.User
import java.io.File
import java.lang.Boolean.TRUE
import java.util.*
import java.util.function.Predicate

class UsersList:  AppCompatActivity() {
    private lateinit var binding: ActivityUsersListBinding
    private var db = Firebase.firestore
    private var user = ""
    private var nick = ""
    private var listGames = mutableListOf(Game("","", "", emptyList()))
    private var listUsers = mutableListOf(User(TRUE,"","","", 0,0,0,0 ))
    private var itemant = -1
    private var itemuser = -1

    private val adaptador = GamesListAdapter(listGames){
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

   private val adaptadorUsers = UsersAdapter(listUsers){
       val items: RecyclerView = findViewById(R.id.listaUsuarios)
       if( items[it].background==null){
           //Solo permite una seleccion a su vez
           // nueva seleccion
           items[it].setBackgroundResource(R.drawable.seleccionado)

           if(itemuser != -1)
               items[itemuser].setBackground(null)

           itemuser = it
       }else if(items[it].background != null){
           //doble click inicia el juego
           itemuser= -1
           //quita la seleccion
           items[it].setBackground(null)

           userSelected(listUsers[it])
       }
   }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        intent.getStringExtra("User")?.let { user = it }
        intent.getStringExtra("Nick")?.let { nick = it }
        listGames.removeAt(0)
        listUsers.removeAt(0)

        val gamesRef = db.collection("users").document(user).collection("games").get()
            .addOnSuccessListener { documentos ->
                if(documentos != null){
                    for(gameuser in documentos){
                        gameuser.data
                        val game = Game(
                             id  = gameuser.data?.get("id").toString(),
                            name = gameuser.data?.get("name").toString(),
                            status = gameuser.data?.get("status").toString(),
                            users = listOf(gameuser.data?.get("users").toString())
                        )
                        listGames.add(game)
                    }
                }else{
                    Log.d("User", "No hay, en teoria")
                }
            }

        val UsersRef = db.collection("users").get()
            .addOnSuccessListener { documentos ->
                if(documentos != null){
                    for(users in documentos){
                        users.data
                        val usernew = User(
                            online = users.data?.get("online") as Boolean,
                            id = users.data?.get("id").toString(),
                            nick = users.data?.get("nick").toString(),
                            email = users.data?.get("email").toString(),
                            solowins = users.getLong("solowins")?.toInt()!!,
                            sololoses = users.getLong("sololoses")?.toInt()!!,
                            multiwins = users.getLong("multiwins")?.toInt()!!,
                            multiloses = users.getLong("multiloses")?.toInt()!!,
                        )
                        if(usernew.email.lowercase() != user){
                            listUsers.add(usernew)
                        }

                    }
                }else{
                    Log.d("User", "No hay, en teoria")
                }
            }

        //imagen del usuario
        var mStorage = FirebaseStorage.getInstance()
        var mReference = mStorage.reference
        val refnick = db.collection("users").document(user).get()

        refnick.addOnSuccessListener { document ->
            if(document != null){
                binding.usernick.text =  document.data?.get("nick").toString()
                val imgRef = mReference.child("images/$user")
                val localfile = File.createTempFile("tempImg","jpg")

                imgRef.getFile(localfile).addOnSuccessListener {
                    var bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                    val options = RequestOptions()
                    options.centerCrop().fitCenter()
                    Glide.with(this@UsersList).load(bitmap).apply(options).into(binding.myimagemenu)

                }
            }else{
                println("Este es print de error")
            }
        }.addOnFailureListener{ exeption ->
            println(exeption)
        }


        if (user.isNotEmpty()){
            initViews()
        }
    }

    private fun initViews(){

       // binding.newGameButton.setOnClickListener { newGame() }

        binding.listGamesRecyclerView.setHasFixedSize(false)
        binding.listGamesRecyclerView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)
        binding.listGamesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.listGamesRecyclerView.adapter = adaptador

        binding.listaUsuarios.setHasFixedSize(false)
        binding.listaUsuarios.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL, false)
        binding.listaUsuarios.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        binding.listaUsuarios.adapter = adaptadorUsers


        val gameRef = db.collection("users").document(user)

        gameRef.collection("games")
            .get()
            .addOnSuccessListener { games ->
                val listChats = games.toObjects(Game::class.java)
                (binding.listGamesRecyclerView.adapter as GamesListAdapter).setData(listChats)
            }

        gameRef.collection("games")
            .addSnapshotListener { games, error ->
                if(error == null){
                    games?.let {
                        val listChats = it.toObjects(Game::class.java)

                        (binding.listGamesRecyclerView.adapter as GamesListAdapter).setData(listChats)
                    }
                }
            }



        val userRef = db.collection("registros")

        userRef.get()
            .addOnSuccessListener { userdoc ->
                val listnewUsers = userdoc.toObjects(User::class.java)
                listnewUsers.removeIf { it.email.lowercase() == user }

                (binding.listaUsuarios.adapter as UsersAdapter).setData(listnewUsers)
            }

        userRef.addSnapshotListener { userdoc, error ->
            if(error == null){
                userdoc?.let {
                    val listnewUsers = it.toObjects(User::class.java)
                    listnewUsers.removeIf { it.email.lowercase() == user }
                    (binding.listaUsuarios.adapter as UsersAdapter).setData(listnewUsers)
                }
            }
        }

    }

    private fun gameSelected(game: Game){
        val intent = Intent(this, GameMultiplayer::class.java)

        if(game.status != "En Proceso"){
            newGame(game)
        }else{
            intent.putExtra("gameId", game.id)
            intent.putExtra("User", user)
            intent.putExtra("Nick", nick)
            startActivity(intent)
        }

    }

    private fun userSelected(userpick: User){

        val gameId = UUID.randomUUID().toString()
        val otherUser = userpick.email.lowercase()
        val users = listOf(user, otherUser)
        val users2 = listOf(otherUser, user)


        var game = Game()
        var gameother = Game()

        val ref = db.collection("users").document(otherUser).get()
        ref.addOnSuccessListener { document ->
            if(document != null){
                game = Game(
                    id = gameId,
                    name = "Partida Contra ${document.data?.get("nick").toString()}",
                    status = "En Proceso",
                    users = users
                )

                db.collection("games").document(gameId).set(game)
                db.collection("users").document(user).collection("games").document(gameId).set(game)
            }else{
                println("Error newGameUserNick")
            }
        }.addOnFailureListener{ exeption ->
            println(exeption)
        }

        val refother = db.collection("users").document(user).get()
        refother.addOnSuccessListener { document ->
            if(document != null){
                gameother = Game(
                    id = gameId,
                    name = "Partida Contra ${document.data?.get("nick").toString()}",
                    status = "En Proceso",
                    users = users2
                )
                db.collection("users").document(otherUser).collection("games").document(gameId).set(gameother)
            }else{
                println("Error newGameUserNick")
            }
        }.addOnFailureListener{ exeption ->
            println(exeption)
        }

        val intent = Intent(this, GameMultiplayer::class.java)
        intent.putExtra("gameId", gameId)
        intent.putExtra("User", user)
        intent.putExtra("Nick", nick)
        startActivity(intent)


    }

    private fun newGame(game: Game){
        val gameId = UUID.randomUUID().toString()
        val parts = game.users.last().split(',',']')
        val otherUser = parts[1].replace(" ","").lowercase()
        val users = listOf(user, otherUser)
        val users2 = listOf(otherUser, user)

        //sacar nicks
        var game = Game()
        var gameother = Game()

        val ref = db.collection("users").document(otherUser).get()
        ref.addOnSuccessListener { document ->
            if(document != null){
                game = Game(
                    id = gameId,
                    name = "Partida Contra ${document.data?.get("nick").toString()}",
                    status = "En Proceso",
                    users = users
                )

                db.collection("games").document(gameId).set(game)
                db.collection("users").document(user).collection("games").document(gameId).set(game)
            }else{
                println("Error newGameUserNick")
            }
        }.addOnFailureListener{ exeption ->
            println(exeption)
        }

        val refother = db.collection("users").document(user).get()
        refother.addOnSuccessListener { document ->
            if(document != null){
                gameother = Game(
                    id = gameId,
                    name = "Partida Contra ${document.data?.get("nick").toString()}",
                    status = "En Proceso",
                    users = users2
                )
                db.collection("users").document(otherUser).collection("games").document(gameId).set(gameother)
            }else{
                println("Error newGameUserNick")
            }
        }.addOnFailureListener{ exeption ->
            println(exeption)
        }

        //iniciacion del juego



        val intent = Intent(this, GameMultiplayer::class.java)
        intent.putExtra("gameId", gameId)
        intent.putExtra("User", user)
        intent.putExtra("Nick", nick)
        startActivity(intent)
    }
}