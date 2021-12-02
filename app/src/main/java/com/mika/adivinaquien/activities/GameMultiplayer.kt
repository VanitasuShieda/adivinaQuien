package com.mika.adivinaquien.activities

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mika.adivinaquien.R
import com.mika.adivinaquien.adapters.AdaptadorMonsters
import com.mika.adivinaquien.adapters.MessageAdapter
import com.mika.adivinaquien.databinding.GameMultiplayBinding
import com.mika.adivinaquien.dialogs.*
import com.mika.adivinaquien.models.*
import java.io.File
import java.util.*


class GameMultiplayer : AppCompatActivity(), DialogSelectMonster.DialogSelectMonsterListener,
    DialogDecideTurn.DialogDecideTurnListener, DialogDuelTurn.DialogDuelTurnListener,
    DialogResolve.DialogResolveListener, DialogResults.DialogResultsListener,
    DialogFinish.DialogFinishListener {
    private lateinit var audioAttributes: AudioAttributes
    private lateinit var sp: SoundPool
    private var swoosh: Int = 0
    private var swoosh2: Int = 0
    private var yes: Int = 0
    private var no: Int = 0
    private var victory: Int = 0
    private var defeat: Int = 0
    private var boxing: Int = 0
    private lateinit var mp: MediaPlayer


    private lateinit var mStorage: FirebaseStorage
    private lateinit var mReference: StorageReference
    private var bitmap: Bitmap? = null

    private var chronoRunning = false //Determina si el cronómetro esta corriendo
    private var nick = ""
    private var rivalnick = ""
    private var rivalemail = ""
    private var player1 = Player()
    private var player2 = Player()
    private var numTurns = 0 //Número de turnos (contador)
    private lateinit var superBinding: GameMultiplayBinding //Binding global
    private var gameId = ""
    private var user = ""
    private var db = Firebase.firestore
    private var db0 = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = GameMultiplayBinding.inflate(layoutInflater)
        superBinding = binding
        setContentView(binding.root)

        intent.getStringExtra("gameId")?.let { gameId = it }
        intent.getStringExtra("User")?.let { user = it }
        intent.getStringExtra("Nick")?.let { nick = it }

        mStorage = FirebaseStorage.getInstance()
        mReference = mStorage.reference
        val imgRef = mReference.child("images/$user")
        val localfile = File.createTempFile("tempImg", "jpg")
        imgRef.getFile(localfile).addOnSuccessListener {
            bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
        }
        val ref = db0.collection("users").document(user).get()
        //Se establece el nombre de los jugadores y su imagen de perfil
        ref.addOnSuccessListener { document ->
            if (document != null) {
                player1.setNickname(document.data?.get("nick").toString())
                player1.setUsermail(user)
                player1.setSolowins(document.getLong("solowins")?.toInt()!!)
                player1.setSololoses(document.getLong("sololoses")?.toInt()!!)
                val imgRef = mReference.child("images/$user")
                val localfile = File.createTempFile("tempImg", "jpg")
                imgRef.getFile(localfile).addOnSuccessListener {
                    bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                    val options = RequestOptions()
                    options.centerCrop().fitCenter()
                    Glide.with(this@GameMultiplayer).load(bitmap).apply(options)
                        .into(superBinding.user1Image2)
                }
            } else {
                println("Este es print de error")
            }
        }.addOnFailureListener { exeption ->
            println(exeption)
        }
        audioAttributes =
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        sp = SoundPool.Builder().setMaxStreams(6).setAudioAttributes(audioAttributes).build()
        swoosh = sp.load(this, R.raw.swoosh, 1)
        swoosh2 = sp.load(this, R.raw.swoosh2, 1)
        yes = sp.load(this, R.raw.yes, 1)
        no = sp.load(this, R.raw.no, 1)
        victory = sp.load(this, R.raw.victory, 1)
        defeat = sp.load(this, R.raw.defeat, 1)
        boxing = sp.load(this, R.raw.boxing, 1)
        //Reproducción de música
        mp = MediaPlayer.create(this, R.raw.games)
        mp.setVolume(0.5f, 0.5f)
        mp.start()
        mp.isLooping = true


        //obtiene datos extras necesarios para mostrar en pantalla
        val ref2 = db.collection("users").document(user).collection("games").document(gameId).get()
        ref2.addOnSuccessListener { document ->
            if (document != null) {
                val parts = document.data?.get("users").toString().split(',', ']')
                val otherUser = parts[1].replace(" ", "").lowercase()
                val otherref = db.collection("users").document(otherUser).get()
                    .addOnSuccessListener { userdoc ->
                        if (userdoc != null) {
                            rivalemail = userdoc.data?.get("email").toString().lowercase()
                            rivalnick = userdoc.data?.get("nick").toString()
                        }

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

    private fun defWiner(otherplayer: String, result: String) {
        val partida = Gameplay(
            vs = otherplayer,
            status = result,
        )
        db.collection("users").document(user).collection("Multiplayergames").document(gameId)
            .set(partida)
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

        db.collection("games").document(gameId).collection("monster").get()
            .addOnSuccessListener { documents ->
                if (documents != null) {
                    println("mosnter:$documents size:${documents.size()}")
                    if (documents.size() == 2) {
                        println("entro en 2 bien")

                        if (documents.documents[0].id == user)
                            player2.setCardChoiced(
                                documents.documents[1].getLong("card")?.toInt()!!
                            )
                        else
                            player2.setCardChoiced(
                                documents.documents[0].getLong("card")?.toInt()!!
                            )

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
                    } else if (documents.size() == 1) {
                        println("entro allistener ")
                        db.collection("games").document(gameId).collection("monster")
                            .addSnapshotListener { doc, error ->
                                if (error == null) {
                                    doc?.let {
                                        println("collection listener sice:${doc.size()} conte$doc")
                                        if (doc.size() == 2) {
                                            dAlert.dismiss()
                                            println("Bien espero turno")
                                            if (itemType == 0) {//Selecciona tu carta
                                                val decideTurnDialog =
                                                    DialogDecideTurn(this, player)
                                                decideTurnDialog.isCancelable = false
                                                decideTurnDialog.show(
                                                    supportFragmentManager,
                                                    "Decidir turno"
                                                )
                                            } else if (itemType == 1) {//Tu monstruo ess... (resolver)
                                                val resolveDialog = DialogResolve(this, player, 0)
                                                resolveDialog.isCancelable = false
                                                resolveDialog.show(
                                                    supportFragmentManager,
                                                    "Tu monstruo es"
                                                )
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

        db.collection("games").document(gameId).collection("rockpaperscissors").get()
            .addOnSuccessListener { documents ->
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

                    } else if (documents.size() == 1) {
                        println("entro allistener ")
                        db.collection("games").document(gameId).collection("rockpaperscissors")
                            .addSnapshotListener { doc, error ->
                                if (error == null) {
                                    doc?.let {
                                        println("rockpaperscissors listener size:${doc.size()} conte$doc")
                                        if (doc.size() == 2) {
                                            dAlert.dismiss()
                                            println("Bien espero turno")

                                            if (doc.documents[0].id == user)
                                                player2.setPpt(
                                                    doc.documents[1].getLong("turn")?.toInt()!!
                                                )
                                            else
                                                player2.setPpt(
                                                    doc.documents[0].getLong("turn")?.toInt()!!
                                                )

                                            //piedra(0), papel(1) o tijera(2)
                                            println(
                                                "rock p1 ${doc.documents[0].id}  :${
                                                    doc.documents[0].getLong(
                                                        "turn"
                                                    )?.toInt()!!
                                                }"
                                            )
                                            println(
                                                "rock p1 ${doc.documents[1].id}  :${
                                                    doc.documents[1].getLong(
                                                        "turn"
                                                    )?.toInt()!!
                                                }"
                                            )

                                            println("player1: ${player1.getPpt()} player2:${player2.getPpt()}")

                                            val duelTurnDialog =
                                                DialogDuelTurn(this, player1, player2)
                                            duelTurnDialog.isCancelable = false
                                            duelTurnDialog.show(
                                                supportFragmentManager,
                                                "Decidir turno"
                                            )
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

        superBinding.crono2.base = SystemClock.elapsedRealtime()
        superBinding.crono2.start()
        chronoRunning = true
        //JUEGO=================================================================================================================
        numTurns++
        superBinding.numTurnosTextView2.text = "Turno $numTurns"
        //superBinding.turnButtonmultiplayer.isEnabled = false
        //Se ejecuta un código dependiendo de quien va primero
        if (player2.getIsFirst()) {
            db.collection("games").document(gameId).collection("turn").document(gameId)
                .set(Turn(numTurns, rivalnick))
        } else {
            db.collection("games").document(gameId).collection("turn").document(gameId)
                .set(Turn(numTurns, nick))
            val turnMsgDialog = DialogTurnMsg(this, player1)
            turnMsgDialog.show(supportFragmentManager, "mensaje de turno")
        }

        //Mostrar el tablero
        superBinding.tablero2.visibility = View.VISIBLE
        //Tablero 2
        var adaptador = AdaptadorMonsters(player2, 2) {}
        superBinding.recView2Monsters2.layoutManager = GridLayoutManager(this, 7)
        superBinding.recView2Monsters2.adapter = adaptador

        //Tablero 1
        adaptador = AdaptadorMonsters(player1, 0) {
            val items: RecyclerView = superBinding.recViewMonsters2
            //se definen las animaciones de flip
            val front_anim = AnimatorInflater.loadAnimator(
                applicationContext,
                R.animator.animation_vertical_flip_front_in
            ) as AnimatorSet
            val back_anim = AnimatorInflater.loadAnimator(
                applicationContext,
                R.animator.animation_vertical_front_out
            ) as AnimatorSet
            //Sí la tarjeta no esta en su lado reverso (lado monstruo)
            if (player1.getMyDeck()[it].isReverse == false) {
                front_anim.setTarget(items[it].findViewById<ImageView>(R.id.monsterBack_image))
                back_anim.setTarget(items[it].findViewById<ImageView>(R.id.monsterFront_image))
                back_anim.start()
                front_anim.start()
                player1.getMyDeck()[it].isReverse = true
            } else { //Sí la tarjeta esta en su lado reverso
                front_anim.setTarget(items[it].findViewById<ImageView>(R.id.monsterFront_image))
                back_anim.setTarget(items[it].findViewById<ImageView>(R.id.monsterBack_image))
                front_anim.start()
                back_anim.start()
                player1.getMyDeck()[it].isReverse = false
            }
            //mostrar el número de tarjetas volteadas
            superBinding.numCartas1TextView2.text = player1.countReverse()
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

//        superBinding.terminarButton2.setOnClickListener {
//            defWiner(rivalnick, "Derrota")
//            finish()
//        }

        superBinding.terminarButton2.setOnClickListener { ((terminar())) }

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
        db.collection("games").document(gameId).collection("turn").document(gameId)
            .addSnapshotListener { turno, error ->
                if (error == null) {
                    turno?.let {
                        numTurns = turno.getLong("turn")?.toInt()!!
                        superBinding.numTurnosTextView2.text = "Turno $numTurns"
                        val usrtr = turno.get("usr").toString()
                        if (usrtr != nick) {
                            superBinding.turnButtonmultiplayer.visibility = View.GONE
                        } else {
                            superBinding.turnButtonmultiplayer.visibility = View.VISIBLE
                        }
                    }
                }
            }
        //listener el otro se rinde
        //listener Rival Pierde o Gana
        db.collection("games").document(gameId).collection("GameStatus")
            .addSnapshotListener { value, error ->
                if (error == null) {
                    value?.let {
                        if(value.documents.size > 0){
                            if (value.documents[0].id == rivalemail) {

                                if (value.documents[0].get("result") == "Derrota") {
                                    player1.setCardChoicedAnswer(player2.getCardChoiced())
                                }

                                val resolveDialog = DialogResolve(this, player1, 2)
                                resolveDialog.isCancelable = false
                                resolveDialog.show(supportFragmentManager, "Tu monstruo es")

                            }
                        }
                    }

                }
            }
        //falta modificar campo de game


    }

    fun tuMonstruoEs(playersTurn: Player) {
        val selectMonsterDialog = DialogSelectMonster(this, playersTurn, 1)
        selectMonsterDialog.show(supportFragmentManager, "Selecciona tu monstruo")
    }

    fun turnoCPU() {

        val increment = FieldValue.increment(1)

        val uid = FirebaseAuth.getInstance().currentUser?.email
        val postRef =
            FirebaseFirestore.getInstance().collection("games").document(gameId).collection("turn")
                .document(gameId)
        val postsLikesRef = FirebaseFirestore.getInstance().collection("turns").document(gameId)

        val database = FirebaseFirestore.getInstance()

        database.runTransaction { transaction ->
            val snapshot = transaction.get(postRef)
            val likeCount = snapshot.getLong("turn")
            if (likeCount != null) {
                if (likeCount >= 0) {
                    transaction.update(postRef, "turn", increment)
                    transaction.update(postRef, "usr", rivalnick)
                }
            }
        }.addOnFailureListener {
            throw Exception(it.message)
        }


        val turnMsgDialog = DialogTurnMsg(this, player2)
        turnMsgDialog.show(supportFragmentManager, "mensaje de turno")
    }

    fun terminar() {
        val dialogFinish = DialogFinish(this)
        dialogFinish.show(supportFragmentManager, "Terminar partida")
    }

    override fun applyDialogFinish(res: String) {
        if (res == "Terminar") {
            mp.stop()
            val intent = Intent(this, GameMenu::class.java)
            intent.putExtra("User", player1.getUsermail())
            startActivity(intent)
            finish()
        } else if (res == "Reiniciar") {
            mp.stop()
            val intent = Intent(this, GameSolo::class.java)
            intent.putExtra("User", user)
            startActivity(intent)
            finish()
        }
    }

    override fun applyDialogResolve(res: String, player: Player) {
        if (res == "Si" || res == "Ok") {
            superBinding.crono2.setBackgroundColor(Color.parseColor("#E91E63"))
            superBinding.crono2.stop()
            chronoRunning = false
            mp.stop()
            if (res == "Si") {
                //Se reproduce el audio
                if (player1.getCardChoicedAnswer() == player2.getCardChoiced()) {
                    sp.play(victory, 1f, 1f, 1, 0, 1f)
                } else {
                    sp.play(defeat, 1f, 1f, 1, 0, 1f)
                }
                this.player1 = player
                val resultsDialog = DialogResults(this, player1, player2, 0)
                resultsDialog.isCancelable = false
                resultsDialog.show(supportFragmentManager, "Resultados")
            } else if (res == "Ok") {
                //Se reproduce el audio
                if (player2.getCardChoicedAnswer() == player1.getCardChoiced()) {
                    sp.play(defeat, 1f, 1f, 1, 0, 1f)
                } else {
                    sp.play(victory, 1f, 1f, 1, 0, 1f)
                }
                chronoRunning = false
                this.player2 = player
                val resultsDialog = DialogResults(this, player1, player2, 1)
                resultsDialog.isCancelable = false
                resultsDialog.show(supportFragmentManager, "Resultados")
            }
        }
    }

    override fun applyDialogResolve(res: String, player1wins: Boolean) {
        if (player1wins) {
            db0.collection("users").document(user).update("multiwins", player1.getSolowins() + 1)
            defWiner(rivalnick, "Victoria")
            //rival, resultado
            if (player1.getautowin()) {
                db0.collection("users").document(user).collection("games").document(gameId)
                    .update("status", "Victoria")
                db0.collection("users").document(rivalemail).collection("games").document(gameId)
                    .update("status", "Derrota")
                db0.collection("games").document(gameId).collection("GameStatus").document(user)
                    .set(Status("Victoria"))
            }
        } else {
            db0.collection("users").document(user).update("multiloses", player1.getSololoses() + 1)
            defWiner(rivalnick, "Derrota")
            if(player1.getautowin()){
                db0.collection("users").document(user).collection("games").document(gameId)
                    .update("status", "Derrota")
                db0.collection("users").document(rivalemail).collection("games").document(gameId)
                    .update("status", "Voctoria")
                db0.collection("games").document(gameId).collection("GameStatus").document(user)
                    .set(Status("Derrota"))
            }
        }

        if (res == "Terminar") {
            val intent = Intent(this, GameMenu::class.java)
            intent.putExtra("User", player1.getUsermail())
            startActivity(intent)
            finish()
        } else if (res == "Nueva partida") {

            val newgameId = UUID.randomUUID().toString()
            val users = listOf(user, rivalemail)
            val users2 = listOf(rivalemail, user)
            var game = Game()
            var gameother = Game()
            game = Game(
                id = newgameId,
                name = "Partida Contra $rivalnick",
                status = "En Proceso",
                users = users
            )

            db.collection("games").document(newgameId).set(game)
            db.collection("users").document(user).collection("games").document(newgameId).set(game)

            gameother = Game(
                id = newgameId,
                name = "Partida Contra $nick",
                status = "En Proceso",
                users = users2
            )
            db.collection("users").document(rivalemail).collection("games").document(newgameId)
                .set(gameother)


            val intent = Intent(this, GameMultiplayer::class.java)
            intent.putExtra("gameId", newgameId)
            intent.putExtra("User", user)
            intent.putExtra("Nick", nick)
            startActivity(intent)
            finish()
        }
    }

}
