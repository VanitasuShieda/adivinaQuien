package com.mika.adivinaquien.activities

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.guesswhosingleplayer.DialogDecideTurn
import com.example.guesswhosingleplayer.DialogResults
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mika.adivinaquien.R
import com.mika.adivinaquien.adapters.AdaptadorMonsters
import com.mika.adivinaquien.adapters.MessageAdapter
import com.mika.adivinaquien.databinding.GameMultiplayBinding
import com.mika.adivinaquien.dialogs.*
import com.mika.adivinaquien.models.*


class GameMultiplayer : AppCompatActivity(), DialogSelectMonster.DialogSelectMonsterListener,
    DialogDecideTurn.DialogDecideTurnListener, DialogDuelTurn.DialogDuelTurnListener,
    DialogResolve.DialogResolveListener, DialogResults.DialogResultsListener {
    private var chronoRunning = false //Determina si el cronómetro esta corriendo
    private var nick = ""
    private var rivalnick = ""
    private var rivalemail = ""
    private var player1 = Player()
    private var player2 = Player()
    private var numTurns = 0 //Número de turnos (contador)
    private var questionsList: MutableList<Questions> = arrayListOf() //Lista de preguntas
    private lateinit var superBinding: GameMultiplayBinding //Binding global
    private var gameId = ""
    private var user = ""
    private var db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = GameMultiplayBinding.inflate(layoutInflater)
        superBinding = binding
        setContentView(binding.root)

        intent.getStringExtra("gameId")?.let { gameId = it }
        intent.getStringExtra("User")?.let { user = it }
        intent.getStringExtra("Nick")?.let { nick = it }


        //obtiene datos extras necesarios para mostrar en pantalla
        val ref = db.collection("users").document(user).collection("games").document(gameId).get()
        ref.addOnSuccessListener { document ->
            if (document != null) {
                val parts = document.data?.get("users").toString().split(',', ']')
                val otherUser = parts[1].replace(" ", "").lowercase()
                val otherref = db.collection("users").document(otherUser).get()
                    .addOnSuccessListener { userdoc ->
                        if (userdoc != null) {
                            rivalemail = userdoc.data?.get("email").toString().lowercase()
                            rivalnick = userdoc.data?.get("nick").toString()
                        }

                        player1.setNickname(nick)
                        player2.setNickname(rivalnick)

                        val selectMonsterDialog = DialogSelectMonster(this, player1, 0)
                        selectMonsterDialog.isCancelable = false
                        selectMonsterDialog.show(
                            supportFragmentManager,
                            "Selecciona tu monstruo"
                        )
                    }

            } else {
                println("Error newGameUserNick")
            }
        }.addOnFailureListener { exeption ->
            println(exeption)
        }


    //aqui se termina
    }//on create

    private fun sendMessage() {
        val message = Message(
            message = superBinding.messageTextField.text.toString(),
            from = user
        )

        db.collection("games").document(gameId).collection("messages").document().set(message)

        superBinding.messageTextField.setText("")
    }


//en el juego multijugador
// if(has ganado){
//  defWiner(OtroJugador, "Victoria")
// else
// defWiner(otrojugador, "Derrota")



    private fun defWiner(otherplayer: String, result: String) {
        val partida = Gameplay(
            vs = otherplayer,
            status = result,
        )

        db.collection("users").document(user).collection("Multiplayergames").document(gameId).set(partida)
    }


    override fun applySelectMonster(player: Player, itemType: Int) {

        this.player1 = player
        var cardC = ModelsMultiplayer(
            card = player1.getCardChoiced()
        )

        db.collection("games").document(gameId).collection("monster").document(user).set(cardC)

        val dAlert = DialogMsg("Esperando Seleccion de $rivalnick")
        dAlert.isCancelable = false
        dAlert.show(supportFragmentManager, "Waiting")

        db.collection("games").document(gameId).collection("monster").get().addOnSuccessListener{ documents ->
                if (documents != null) {
                 println("mosnter:$documents size:${documents.size()}")
                        if (documents.size() == 2) {
                            println("entro en 2 bien")
                            dAlert.dismiss()
                            if (itemType == 0) {//Selecciona tu carta
                                val decideTurnDialog = DialogDecideTurn(this, player)
                                decideTurnDialog.isCancelable = false
                                decideTurnDialog.show(supportFragmentManager, "Decidir turno")
                            } else if (itemType == 1) {//Tu monstruo ess... (resolver)
                                val resolveDialog = DialogResolve(this, player, 0)
                                resolveDialog.isCancelable = false
                                resolveDialog.show(supportFragmentManager, "Tu monstruo es")
                            }
                        }else if(documents.size() == 1 ){
                            println("entro allistener ")
                            db.collection("games").document(gameId).collection("monster").addSnapshotListener{ doc, error ->
                                if(error ==null){
                                    doc?.let {
                                        println("collection listener sice:${doc.size()} conte$doc")
                                        if(doc.size() == 2 ){
                                            dAlert.dismiss()
                                            println("Bien espero turno")
                                            if (itemType == 0) {//Selecciona tu carta
                                                val decideTurnDialog = DialogDecideTurn(this, player)
                                                decideTurnDialog.isCancelable = false
                                                decideTurnDialog.show(supportFragmentManager, "Decidir turno")
                                            } else if (itemType == 1) {//Tu monstruo ess... (resolver)
                                                val resolveDialog = DialogResolve(this, player, 0)
                                                resolveDialog.isCancelable = false
                                                resolveDialog.show(supportFragmentManager, "Tu monstruo es")
                                            }
                                        }

                                    }
                                }

                            }

                        }

                }
            }

    }

    override fun applyDecideTurn(player: Player) {
        this.player1 = player

        var pptchoise = Turn(turn = player.getPpt())
        db.collection("games").document(gameId).collection("rockpaperscissors").document(user)
            .set(pptchoise)

        //verifica que ambos jugadores ya elijieron turno para continuar
        val dAlert = DialogMsg("Esperando Seleccion de $rivalnick")
        dAlert.isCancelable = false
        dAlert.show(supportFragmentManager, "Waiting")

        db.collection("games").document(gameId).collection("rockpaperscissors").get().addOnSuccessListener{ documents ->
            if (documents != null) {
                println("rockpaperscissors:$documents size:${documents.size()}")
                if (documents.size() == 2) {
                    println("entro en 2 bien")
                    dAlert.dismiss()

                            if (documents.documents[0].id == user)
                                player2.setPpt(documents.documents[1].getLong("turn")?.toInt()!!)
                            else
                                player2.setPpt(documents.documents[0].getLong("turn")?.toInt()!!)


                        dAlert.dismiss()

                        val duelTurnDialog = DialogDuelTurn(this, player1, player2)
                        duelTurnDialog.isCancelable = false
                        duelTurnDialog.show(supportFragmentManager, "Decidir turno")

                }else if(documents.size() == 1 ){
                    println("entro allistener ")
                    db.collection("games").document(gameId).collection("rockpaperscissors").addSnapshotListener{ doc, error ->
                        if(error ==null){
                            doc?.let {
                                println("rockpaperscissors listener size:${doc.size()} conte$doc")
                                if(doc.size() == 2 ){
                                    dAlert.dismiss()
                                    println("Bien espero turno")

                                    if (doc.documents[0].id == user)
                                        player2.setPpt(doc.documents[1].getLong("turn")?.toInt()!!)
                                    else
                                        player2.setPpt(doc.documents[0].getLong("turn")?.toInt()!!)

                                    //piedra(0), papel(1) o tijera(2)
                                    println("rock p1 ${doc.documents[0].id}  :${doc.documents[0].getLong("turn")?.toInt()!!}")
                                    println("rock p1 ${doc.documents[1].id}  :${doc.documents[1].getLong("turn")?.toInt()!!}")

                                    println("player1: ${player1.getPpt()} player2:${player2.getPpt()}")

                                    val duelTurnDialog = DialogDuelTurn(this, player1, player2)
                                    duelTurnDialog.isCancelable = false
                                    duelTurnDialog.show(supportFragmentManager, "Decidir turno")
                                }

                            }
                        }

                    }

                }

            }
        }



    }

    override fun applyDuelTurn(player1: Player, player2: Player, isDraw: Boolean) {
        this.player1 = player1
        this.player2 = player2
        //Si hay empate en el piedra, papael o tijeras se repite el juego
        if (isDraw) {
            db.collection("games").document(gameId).collection("rockpaperscissors").document(user)
                .delete()
                .addOnSuccessListener {
                    Log.d("Delete user", "DocumentSnapshot successfully deleted!")
                    db.collection("games").document(gameId).collection("rockpaperscissors")
                        .document(rivalemail)
                        .delete()
                        .addOnSuccessListener {
                            Log.d("Delete rival", "DocumentSnapshot successfully deleted!")
                            val decideTurnDialog = DialogDecideTurn(this, player1)
                            decideTurnDialog.isCancelable = false
                            decideTurnDialog.show(supportFragmentManager, "Decidir turno")
                        }
                        .addOnFailureListener { e ->
                            Log.w("Delete", "Error deleting document", e)
                        }

                }
                .addOnFailureListener { e ->
                    Log.w("Delete error user", "Error deleting document", e)
                }
        } else {
            iniciarJuego()
        }
    }

    fun iniciarJuego() {
        //Se establece el cronómetro

        superBinding.crono2.base= SystemClock.elapsedRealtime()
        superBinding.crono2.start()
        chronoRunning=true
        //JUEGO=================================================================================================================
        numTurns++
        superBinding.numTurnosTextView2.text = "Turno $numTurns"
        //superBinding.turnButtonmultiplayer.isEnabled = false
        //Se ejecuta un código dependiendo de quien va primero
        if (player2.getIsFirst()) {
            db.collection("games").document(gameId).collection("turn").document(gameId).set(Turn(numTurns,rivalnick))
        } else {
            val turnMsgDialog = DialogTurnMsg(this, player1)
            turnMsgDialog.show(supportFragmentManager, "mensaje de turno")
        }

        //Mostrar el tablero
        superBinding.tablero2.visibility=View.VISIBLE
        //Tablero 2
        var adaptador = AdaptadorMonsters(player2, 2){}
        superBinding.recView2Monsters2.layoutManager = GridLayoutManager(this, 7)
        superBinding.recView2Monsters2.adapter = adaptador

        //Tablero 1
        adaptador = AdaptadorMonsters(player1, 0){
            val items: RecyclerView =superBinding.recViewMonsters2
            //se definen las animaciones de flip
            val front_anim = AnimatorInflater.loadAnimator(applicationContext, R.animator.animation_vertical_flip_front_in) as AnimatorSet
            val back_anim = AnimatorInflater.loadAnimator(applicationContext, R.animator.animation_vertical_front_out) as AnimatorSet
            //Sí la tarjeta no esta en su lado reverso (lado monstruo)
            if(player1.getMyDeck()[it].isReverse==false){
                front_anim.setTarget(items[it].findViewById<ImageView>(R.id.monsterBack_image))
                back_anim.setTarget(items[it].findViewById<ImageView>(R.id.monsterFront_image))
                back_anim.start()
                front_anim.start()
                player1.getMyDeck()[it].isReverse= true
            } else{ //Sí la tarjeta esta en su lado reverso
                front_anim.setTarget(items[it].findViewById<ImageView>(R.id.monsterFront_image))
                back_anim.setTarget(items[it].findViewById<ImageView>(R.id.monsterBack_image))
                front_anim.start()
                back_anim.start()
                player1.getMyDeck()[it].isReverse= false
            }
            //mostrar el número de tarjetas volteadas
            superBinding.numCartas1TextView2.text=player1.countReverse()
        }
        superBinding.recViewMonsters2.layoutManager = GridLayoutManager(this, 7)
        superBinding.recViewMonsters2.adapter = adaptador

        //Muestra la imagen del monstruo seleccionado
        superBinding.myMonsterImage2.setImageResource(player1.getMonsterList()[player1.getCardChoiced()].imagen)


        superBinding.chatbtn.setOnClickListener {
            if (superBinding.chatgame.visibility == View.VISIBLE) {
                superBinding.chatgame.visibility = View.GONE
                superBinding.tablero2.visibility = View.VISIBLE
            } else {
                superBinding.chatgame.visibility = View.VISIBLE
                superBinding.tablero2.visibility = View.INVISIBLE
            }
        }

        superBinding.closechatbtn.setOnClickListener {
            if (superBinding.chatgame.visibility == View.VISIBLE) {
                superBinding.chatgame.visibility = View.GONE
                superBinding.tablero2.visibility = View.VISIBLE
            } else {
                superBinding.chatgame.visibility = View.VISIBLE
                superBinding.tablero2.visibility = View.INVISIBLE
            }
        }

        superBinding.terminarButton2.setOnClickListener {
            defWiner(rivalemail, "Derrota")
            finish()
        }






        superBinding.resolverButton2.setOnClickListener { (tuMonstruoEs(player1)) }

        superBinding.turnButtonmultiplayer.setOnClickListener {
            turnoCPU()
        }

        superBinding.messagesRecylerView.layoutManager = LinearLayoutManager(this)
        superBinding.messagesRecylerView.adapter = MessageAdapter(user)

        superBinding.sendMessageButton.setOnClickListener { sendMessage() }

        val gameRef = db.collection("games").document(gameId)

        //creador de mensajes
        gameRef.collection("messages").orderBy("dob", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { messages ->
                val listMessages = messages.toObjects(Message::class.java)
                (superBinding.messagesRecylerView.adapter as MessageAdapter).setData(listMessages)
            }
        //listener de mensajes
        gameRef.collection("messages").orderBy("dob", Query.Direction.ASCENDING)
            .addSnapshotListener { messages, error ->
                if (error == null) {
                    messages?.let {
                        val listMessages = it.toObjects(Message::class.java)
                        (superBinding.messagesRecylerView.adapter as MessageAdapter).setData(
                            listMessages
                        )
                    }
                }
            }
        //listener turno
        db.collection("games").document(gameId).collection("turn").document(gameId).addSnapshotListener{
            turno, error ->
            if(error == null){
                turno?.let {
                    numTurns = turno.getLong("turn")?.toInt()!!
                    superBinding.numTurnosTextView2.text = "Turno $numTurns"
                    val usrtr = turno.get("usr").toString()
                    if(usrtr != nick){
                        superBinding.turnButtonmultiplayer.visibility = View.GONE
                    }else{
                        superBinding.turnButtonmultiplayer.visibility = View.VISIBLE
                    }
                }
            }
        }
        //listener el otro se rinde
        //falta modificar campo de game






        db.collection("users").document(rivalemail).collection("Multiplayergames").addSnapshotListener{
                game, error ->
            if(error != null){

                if (game != null) {
                    if(game.documents[gameId].get("status").toString() == "Derrota"){
                        val partida = Gameplay(
                            vs = rivalemail,
                            status = "Victoria",
                        )
                        db.collection("users").document(user).collection("Multiplayergames").document(gameId).set(partida)
                    }else{
                        val partida = Gameplay(
                            vs = rivalemail,
                            status = "Derrota",
                        )
                        db.collection("users").document(user).collection("Multiplayergames").document(gameId).set(partida)
                    }

                    finish()
                }


            }

        }

    }

    fun tuMonstruoEs(playersTurn: Player) {
        val selectMonsterDialog = DialogSelectMonster(this, playersTurn, 1)
        selectMonsterDialog.show(supportFragmentManager, "Selecciona tu monstruo")
    }

    fun turnoCPU() {

        val increment = FieldValue.increment(1)

        val uid = FirebaseAuth.getInstance().currentUser?.email
        val postRef = FirebaseFirestore.getInstance().collection("games").document(gameId).collection("turn").document(gameId)
        val postsLikesRef = FirebaseFirestore.getInstance().collection("turns").document(gameId)

        val database = FirebaseFirestore.getInstance()

        database.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likeCount = snapshot.getLong("turn")
            if (likeCount != null) {
                if (likeCount >= 0) {
                        transaction.update(postRef, "turn", increment)
                        transaction.update(postRef, "usr",rivalnick)
                }
            }
        }.addOnFailureListener {
            throw Exception(it.message)
        }


        val turnMsgDialog = DialogTurnMsg(this, player2)
        turnMsgDialog.show(supportFragmentManager, "mensaje de turno")
    }


    override fun applyDialogResolve(res: String, player: Player) {
        if (res == "Si") {
            this.player1 = player
            val resultsDialog = DialogResults(this, player1, player2, 0)
            resultsDialog.isCancelable = false
            resultsDialog.show(supportFragmentManager, "Resultados")
        } else if (res == "Ok") {
            this.player2 = player
            val resultsDialog = DialogResults(this, player1, player2, 1)
            resultsDialog.isCancelable = false
            resultsDialog.show(supportFragmentManager, "Resultados")
        }
    }

    override fun applyDialogResolve(res: String) {
        if (res == "Terminar") {

        } else if (res == "Nueva partida") {

        }
    }
}
