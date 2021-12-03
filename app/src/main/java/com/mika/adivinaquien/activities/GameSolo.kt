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
import android.os.*
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mika.adivinaquien.R
import com.mika.adivinaquien.adapters.AdaptadorMonsters
import com.mika.adivinaquien.databinding.ActivityGameSoloBinding
import com.mika.adivinaquien.dialogs.*
import com.mika.adivinaquien.models.Player
import com.mika.adivinaquien.models.Questions
import java.io.File

class GameSolo : AppCompatActivity(), DialogSelectMonster.DialogSelectMonsterListener,
    DialogDecideTurn.DialogDecideTurnListener, DialogDuelTurn.DialogDuelTurnListener,
    DialogQCategory.DialogQCategoryListener, DialogDoQuestion.DialogDoQuestionListener,
    DialogResolve.DialogResolveListener, DialogCpuQuestion.DialogCpuQuestionListener,
    DialogResults.DialogResultsListener, DialogFinish.DialogFinishListener {
    //variables para el efecto de sonido
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


    private var usermail = ""
    private var db = FirebaseFirestore.getInstance()
    private var bitmap: Bitmap? = null
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mReference: StorageReference
    private var chronoRunning = false //Determina si el cronómetro esta corriendo
    private var player1 = Player()
    private var player2 = Player()
    private var numTurns = 0 //Número de turnos (contador)
    private var questionsList: MutableList<Questions> = arrayListOf() //Lista de preguntas
    private lateinit var superBinding: ActivityGameSoloBinding //Binding global

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGameSoloBinding.inflate(layoutInflater)
        superBinding = binding
        setContentView(binding.root)

        //PREPARATIVOS==========================================================================================================
        intent.getStringExtra("User")?.let { usermail = it }
        mStorage = FirebaseStorage.getInstance()
        mReference = mStorage.reference
        val imgRef = mReference.child("images/$usermail")
        val localfile = File.createTempFile("tempImg", "jpg")
        imgRef.getFile(localfile).addOnSuccessListener {
            bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
        }
        val ref = db.collection("users").document(usermail).get()
        //Se establece el nombre de los jugadores y su imagen de perfil
        ref.addOnSuccessListener { document ->
            if (document != null) {
                player1.setNickname(document.data?.get("nick").toString())
                player1.setUsermail(usermail)
                player1.setSolowins(document.getLong("solowins")?.toInt()!!)
                player1.setSololoses(document.getLong("sololoses")?.toInt()!!)
                val imgRef = mReference.child("images/$usermail")
                val localfile = File.createTempFile("tempImg", "jpg")
                imgRef.getFile(localfile).addOnSuccessListener {
                    bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
                    val options = RequestOptions()
                    options.centerCrop().fitCenter()
                    Glide.with(this@GameSolo).load(bitmap).apply(options)
                        .into(superBinding.user1Image)
                }
            } else {
                println("Este es print de error")
            }
        }.addOnFailureListener { exeption ->
            println(exeption)
        }
        player2.setNickname("CPU")
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
        mp.setVolume(0.3f, 0.3f)
        mp.start()
        mp.isLooping = true
        val selectMonsterDialog = DialogSelectMonster(this, player1, 0)
        selectMonsterDialog.isCancelable = false
        selectMonsterDialog.show(supportFragmentManager, "Selecciona tu monstruo")
    }
    override fun onPause() {
        super.onPause()
        mp.stop()
    }

    override fun applySelectMonster(player: Player, itemType: Int) {
        this.player1 = player
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

    override fun applyDecideTurn(player: Player) {
        sp.play(boxing, 0.3f, 0.3f, 1, 0, 1f)
        this.player1 = player
        player2.setPpt((0..2).random())
        val duelTurnDialog = DialogDuelTurn(this, player1, player2)
        duelTurnDialog.isCancelable = false
        duelTurnDialog.show(supportFragmentManager, "Decidir turno")

    }

    override fun applyDuelTurn(player1: Player, player2: Player, isDraw: Boolean) {
        this.player1 = player1
        this.player2 = player2
        //Si hay empate en el piedra, papael o tijeras se repite el juego
        if (isDraw == true) {
            val decideTurnDialog = DialogDecideTurn(this, player1)
            decideTurnDialog.isCancelable = false
            decideTurnDialog.show(supportFragmentManager, "Decidir turno")
        } else {
            iniciarJuego()
        }
    }

    fun iniciarJuego() {
        //Se establece el cronómetro
        superBinding.crono.base = SystemClock.elapsedRealtime()
        superBinding.crono.start()
        chronoRunning = true
        //para que el player2 tenga el mismo deck (orden) que el player2
        player2.copyDeck(player1)
        //se selecciona al azar la tarjeta de la CPU
        player2.setCardChoiced((0 until player2.getMonsterList().size).random())
        //Mostrar el tablero
        superBinding.tablero.visibility = View.VISIBLE
        //Tablero 2
        var adaptador = AdaptadorMonsters(player2, 2) {}
        superBinding.recView2Monsters.layoutManager = GridLayoutManager(this, 7)
        superBinding.recView2Monsters.adapter = adaptador

        //Tablero 1
        adaptador = AdaptadorMonsters(player1, 0) {
            //Se reproduce el sonido
            sp.play(swoosh, 1f, 1f, 1, 0, 1f)
            val items: RecyclerView = superBinding.recViewMonsters
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
            superBinding.numCartas1TextView.text = player1.countReverse()
        }
        superBinding.recViewMonsters.layoutManager = GridLayoutManager(this, 7)
        superBinding.recViewMonsters.adapter = adaptador

        //Muestra la imagen del monstruo seleccionado
        superBinding.myMonsterImage.setImageResource(player1.getMonsterList()[player1.getCardChoiced()].imagen)

        //JUEGO=================================================================================================================
        numTurns++
        superBinding.numTurnosTextView.text = "Turno $numTurns"
        superBinding.turnButton.isEnabled = false
        //Se ejecuta un código dependiendo de quien va primero
        if (player2.getIsFirst()) {
            turnoCPU()
        } else {
            val turnMsgDialog = DialogTurnMsg(this, player1)
            turnMsgDialog.show(supportFragmentManager, "mensaje de turno")
        }
        //setOnClickListener de los botones del tablero
        superBinding.terminarButton.setOnClickListener { ((terminar())) }
        superBinding.preguntaButton.setOnClickListener { (hacerPregunta(player1)) }
        superBinding.resolverButton.setOnClickListener { (tuMonstruoEs(player1)) }
        superBinding.questionsButton.setOnClickListener { (listaPreguntas()) }
        superBinding.turnButton.setOnClickListener {
            numTurns++
            superBinding.numTurnosTextView.text = "Turno $numTurns"
            turnoCPU()
        }
    }

    fun terminar() {
        val dialogFinish = DialogFinish(this)
        dialogFinish.show(supportFragmentManager, "Terminar partida")
    }

    fun hacerPregunta(playersTurn: Player) {
        val qCategoryDialog = DialogQCategory(this, -1)
        qCategoryDialog.isCancelable = false
        qCategoryDialog.show(supportFragmentManager, "Atributo")
    }

    fun tuMonstruoEs(playersTurn: Player) {
        val selectMonsterDialog = DialogSelectMonster(this, playersTurn, 1)
        selectMonsterDialog.show(supportFragmentManager, "Selecciona tu monstruo")
    }

    fun listaPreguntas() {
        val questionsListDialog = DialogQuestionsList(this, questionsList)
        questionsListDialog.show(supportFragmentManager, "Selecciona tu monstruo")
    }

    fun turnoCPU() {
        val turnMsgDialog = DialogTurnMsg(this, player2)
        turnMsgDialog.show(supportFragmentManager, "mensaje de turno")
        Handler(Looper.getMainLooper()).postDelayed({
            //Se selecciona al azar una pregunta
            var questionsIndex: Int
            //lógica para preguntas oportunas cuando queden pocas cartas
            if (player2.countReverseInt() <= 10) {
                questionsIndex = preguntaPertinente()
            } else {
                while (true) {
                    questionsIndex = (0 until player2.getQuestionsCPU().size).random()
                    if (!player2.getQuestionsChoseCPU().contains(questionsIndex)) {
                        player2.getQuestionsChoseCPU().add(questionsIndex)
                        break
                    }
                }
            }
            val TheQuestion = player2.getQuestionsCPU()[player2.getQuestionsChoseCPU().last()]
            val cpuQuestionDialog = DialogCpuQuestion(
                this,
                player1,
                player2,
                TheQuestion,
                player2.getQuestionsChoseCPU().last()
            )
            cpuQuestionDialog.isCancelable = false
            cpuQuestionDialog.show(supportFragmentManager, "Pregunta CPU")
        }, 1900)
    }

    fun preguntaPertinente(): Int {
        var questionsIndex = 0
        //Se determina la primera carta que aún siga levantada
        for (i in player2.getMyDeck().indices) {
            if (player2.getMyDeck()[i].isReverse == false) {
                while (true) {
                    questionsIndex = (0 until player2.getQuestionsCPU().size).random()
                    if (!player2.getQuestionsChoseCPU().contains(questionsIndex)) {
                        when (questionsIndex) {
                            0 -> {
                                if (player2.getMyDeck()[i].color == "VERDE") player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            1 -> {
                                if (player2.getMyDeck()[i].color == "ROJO") player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            2 -> {
                                if (player2.getMyDeck()[i].color == "MORADO") player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            3 -> {
                                if (player2.getMyDeck()[i].color == "AZUL") player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            4 -> {
                                if (player2.getMyDeck()[i].color == "CAFÉ") player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            5 -> {
                                if (player2.getMyDeck()[i].color == "GRIS") player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            6 -> {
                                if (player2.getMyDeck()[i].eyes == 1) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            7 -> {
                                if (player2.getMyDeck()[i].eyes == 2) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            8 -> {
                                if (player2.getMyDeck()[i].eyes == 3) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            9 -> {
                                if (player2.getMyDeck()[i].furry) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            10 -> {
                                if (player2.getMyDeck()[i].nose) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            11 -> {
                                if (player2.getMyDeck()[i].teeth) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            12 -> {
                                if (player2.getMyDeck()[i].tongue) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            13 -> {
                                if (player2.getMyDeck()[i].antennae) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            14 -> {
                                if (player2.getMyDeck()[i].horns) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            15 -> {
                                if (player2.getMyDeck()[i].ears) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            16 -> {
                                if (player2.getMyDeck()[i].arms) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            17 -> {
                                if (player2.getMyDeck()[i].legs) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            18 -> {
                                if (player2.getMyDeck()[i].tentacles) player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            19 -> {
                                if (player2.getMyDeck()[i].expression == "FELIZ") player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            20 -> {
                                if (player2.getMyDeck()[i].expression == "ENOJADO") player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            21 -> {
                                if (player2.getMyDeck()[i].expression == "TRISTE") player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            22 -> {
                                if (player2.getMyDeck()[i].expression == "SORPRENDIDO") player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                            23 -> {
                                if (player2.getMyDeck()[i].expression == "SIN EXPRESIÓN") player2.getQuestionsChoseCPU()
                                    .add(questionsIndex) else continue
                            }
                        }
                        break
                    }
                }
                break
            }
        }
        return questionsIndex
    }

    //botón de terminar partida
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
            intent.putExtra("User", usermail)
            startActivity(intent)
            finish()
        }
    }

    //Función que agrega una pregunta con su respuesta a la lista de preguntas realizadas
    fun addQuestionsListDialog(
        whoAsks: Player,
        whoAnswers: Player,
        question: String,
        answer: Boolean
    ) {
        val answerString: String = if (answer) "Sí" else "No"
        questionsList.add(Questions(numTurns, question, answerString, whoAsks, whoAnswers))
    }

    override fun applyQCategory(itemType: Int, inDialog: String) {
        when (inDialog) {
            "categories" -> dCategories(itemType)
            "colors" -> dColors(itemType)
            "eyes" -> dEyes(itemType)
            "head" -> dHead(itemType)
            "limbs" -> dLimbs(itemType)
            "expression" -> dExpression(itemType)
        }
        if (inDialog == "volver") {
            val qCategoryDialog = DialogQCategory(this, -1)
            qCategoryDialog.isCancelable = false
            qCategoryDialog.show(supportFragmentManager, "Atributo")
        }

    }

    //Dependiendo del applyQCategory se ejecutan algunas de estas funciones
    fun dCategories(selected: Int) {
        val qCategoryDialog = DialogQCategory(this, selected)
        qCategoryDialog.isCancelable = false
        qCategoryDialog.show(supportFragmentManager, "Atributo")
    }

    fun dColors(selected: Int) {
        var pregunta = "¿Su cuerpo es color"
        when (selected) {
            0 -> pregunta = "$pregunta verde?"
            1 -> pregunta = "$pregunta rojo?"
            2 -> pregunta = "$pregunta morado?"
            3 -> pregunta = "$pregunta azul?"
            4 -> pregunta = "$pregunta café?"
            5 -> pregunta = "$pregunta gris?"
        }
        val doQuestionDialog = DialogDoQuestion(this, pregunta, 0, selected)
        doQuestionDialog.isCancelable = false
        doQuestionDialog.show(supportFragmentManager, "Hacer pregunta")
    }

    fun dEyes(selected: Int) {
        var pregunta = "¿Tiene"
        when (selected) {
            0 -> pregunta = "$pregunta un ojo?"
            1 -> pregunta = "$pregunta dos ojos?"
            2 -> pregunta = "$pregunta tres ojos?"
        }
        val doQuestionDialog = DialogDoQuestion(this, pregunta, 1, selected)
        doQuestionDialog.isCancelable = false
        doQuestionDialog.show(supportFragmentManager, "Hacer pregunta")
    }

    fun dHead(selected: Int) {
        var pregunta = "¿Tiene"
        when (selected) {
            0 -> pregunta = "$pregunta pelo?"
            1 -> pregunta = "$pregunta nariz?"
            2 -> pregunta = "$pregunta dientes?"
            3 -> pregunta = "$pregunta lengua?"
            4 -> pregunta = "$pregunta antenas?"
            5 -> pregunta = "$pregunta cuernos?"
            6 -> pregunta = "$pregunta orejas?"
        }
        val doQuestionDialog = DialogDoQuestion(this, pregunta, 2, selected)
        doQuestionDialog.isCancelable = false
        doQuestionDialog.show(supportFragmentManager, "Hacer pregunta")
    }

    fun dLimbs(selected: Int) {
        var pregunta = "¿Tiene"
        when (selected) {
            0 -> pregunta = "$pregunta brazos?"
            1 -> pregunta = "$pregunta piernas?"
            2 -> pregunta = "$pregunta tentáculos?"
        }
        val doQuestionDialog = DialogDoQuestion(this, pregunta, 3, selected)
        doQuestionDialog.isCancelable = false
        doQuestionDialog.show(supportFragmentManager, "Hacer pregunta")
    }

    fun dExpression(selected: Int) {
        var pregunta = "¿Está"
        when (selected) {
            0 -> pregunta = "$pregunta feliz?"
            1 -> pregunta = "$pregunta enojado?"
            2 -> pregunta = "$pregunta triste?"
            3 -> pregunta = "$pregunta sorprendido?"
            4 -> pregunta = "¿No tiene expresión?"
        }
        val doQuestionDialog = DialogDoQuestion(this, pregunta, 4, selected)
        doQuestionDialog.isCancelable = false
        doQuestionDialog.show(supportFragmentManager, "Hacer pregunta")
    }

    override fun applyDialogDoQuestion(
        res: String,
        question: String,
        itemType: Int,
        selected: Int
    ) {
        if (res == "No") {
            val qCategoryDialog = DialogQCategory(this, itemType)
            qCategoryDialog.isCancelable = false
            qCategoryDialog.show(supportFragmentManager, "Atributo")
        } else if (res == "Si") {
            respuestaCPU(question, itemType, selected)
        }
    }

    //Se determina la respuesta a la pregunta realizada por el jugador
    fun respuestaCPU(question: String, itemType: Int, selected: Int) {
        var answer = false
        when (itemType) {
            0 -> {
                when (selected) {
                    0 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].color == "VERDE") answer =
                            true
                    }
                    1 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].color == "ROJO") answer =
                            true
                    }
                    2 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].color == "MORADO") answer =
                            true
                    }
                    3 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].color == "AZUL") answer =
                            true
                    }
                    4 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].color == "CAFÉ") answer =
                            true
                    }
                    5 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].color == "GRIS") answer =
                            true
                    }
                }
            }
            1 -> {
                when (selected) {
                    0 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].eyes == 1) answer =
                            true
                    }
                    1 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].eyes == 2) answer =
                            true
                    }
                    2 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].eyes == 3) answer =
                            true
                    }
                }
            }
            2 -> {
                when (selected) {
                    0 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].furry) answer = true
                    }
                    1 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].nose) answer = true
                    }
                    2 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].teeth) answer = true
                    }
                    3 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].tongue) answer = true
                    }
                    4 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].antennae) answer =
                            true
                    }
                    5 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].horns) answer = true
                    }
                    6 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].ears) answer = true
                    }
                }
            }
            3 -> {
                when (selected) {
                    0 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].arms) answer = true
                    }
                    1 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].legs) answer = true
                    }
                    2 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].tentacles) answer =
                            true
                    }
                }
            }
            4 -> {
                when (selected) {
                    0 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].expression == "FELIZ") answer =
                            true
                    }
                    1 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].expression == "ENOJADO") answer =
                            true
                    }
                    2 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].expression == "TRISTE") answer =
                            true
                    }
                    3 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].expression == "SORPRENDIDO") answer =
                            true
                    }
                    4 -> {
                        if (player2.getMonsterList()[player2.getCardChoiced()].expression == "SIN EXPRESIÓN") answer =
                            true
                    }
                }
            }
        }
        //Se reproduce el audio
        if (answer) {
            sp.play(yes, 1f, 1f, 1, 0, 1f)
        } else {
            sp.play(no, 1f, 1f, 1, 0, 1f)
        }
        //Se agrega la pregunta a la lista
        addQuestionsListDialog(player1, player2, question, answer)
        superBinding.preguntaButton.isEnabled = false
        superBinding.turnButton.isEnabled = true
        val answerCPUDialog = DialogAnswerCPU(this, question, answer)
        answerCPUDialog.show(supportFragmentManager, "Respuesta CPU")
    }

    override fun applyDialogResolve(res: String, player: Player) {
        if (res == "Si" || res == "Ok") {
            superBinding.crono.setBackgroundColor(Color.parseColor("#E91E63"))
            superBinding.crono.stop()
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

    override fun applyDialogCpuQuestion(res: String, indexQ: Int) {
        if (res == "Sí") {
            when (indexQ) {
                0 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].color != "VERDE") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                1 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].color != "ROJO") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                2 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].color != "MORADO") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                3 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].color != "AZUL") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                4 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].color != "CAFÉ") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                5 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].color != "GRIS") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                6 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].eyes != 1) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                7 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].eyes != 2) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                8 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].eyes != 3) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                9 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].furry) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                10 -> {
                    for (i in player2.getMyDeck().indices) if (!player2.getMyDeck()[i].nose) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                11 -> {
                    for (i in player2.getMyDeck().indices) if (!player2.getMyDeck()[i].teeth) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                12 -> {
                    for (i in player2.getMyDeck().indices) if (!player2.getMyDeck()[i].tongue) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                13 -> {
                    for (i in player2.getMyDeck().indices) if (!player2.getMyDeck()[i].antennae) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                14 -> {
                    for (i in player2.getMyDeck().indices) if (!player2.getMyDeck()[i].horns) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                15 -> {
                    for (i in player2.getMyDeck().indices) if (!player2.getMyDeck()[i].ears) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                16 -> {
                    for (i in player2.getMyDeck().indices) if (!player2.getMyDeck()[i].arms) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                17 -> {
                    for (i in player2.getMyDeck().indices) if (!player2.getMyDeck()[i].legs) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                18 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].tentacles) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                19 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].expression != "FELIZ") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                20 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].expression != "ENOJADO") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                21 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].expression != "TRISTE") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                22 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].expression != "SORPRENDIDO") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                23 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].expression != "SIN EXPRESIÓN") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
            }
        } else {
            when (indexQ) {
                0 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].color == "VERDE") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                1 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].color == "ROJO") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                2 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].color == "MORADO") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                3 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].color == "AZUL") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                4 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].color == "CAFÉ") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                5 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].color == "GRIS") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                6 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].eyes == 1) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                7 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].eyes == 2) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                8 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].eyes == 3) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                9 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].furry) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                10 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].nose) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                11 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].teeth) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                12 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].tongue) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                13 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].antennae) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                14 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].horns) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                15 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].ears) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                16 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].arms) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                17 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].legs) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                18 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].tentacles) player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                19 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].expression == "FELIZ") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                20 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].expression == "ENOJADO") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                21 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].expression == "TRISTE") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                22 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].expression == "SORPRENDIDO") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
                23 -> {
                    for (i in player2.getMyDeck().indices) if (player2.getMyDeck()[i].expression == "SIN EXPRESIÓN") player2.getMyDeck()[i].isReverse =
                        true.also { animarCPU(i) }
                }
            }
        }
        sp.play(swoosh2, 1f, 1f, 1, 0, 1f)
        superBinding.numCartas2TextView.text = player2.countReverse()
        val TheQuestion = player2.getQuestionsCPU()[indexQ]
        //Se agrega la pregunta a la lista
        val answerString: Boolean = if (res == "Sí") true else false
        addQuestionsListDialog(player2, player1, TheQuestion, answerString)
        //Si la CPU solo le queda una carta, resolverá tu monstruo es
        if (player2.countReverseInt() == 1) {
            //Se determina cual es la caarta que quedó y se setea a setCardChoicedAnswer
            for (i in player2.getMyDeck().indices) {
                if (player2.getMyDeck()[i].isReverse == false) {
                    player2.setCardChoicedAnswer(player2.getMyDeck()[i].id)
                    break
                }
            }
            val resolveDialog = DialogResolve(this, player2, 1)
            resolveDialog.isCancelable = false
            resolveDialog.show(supportFragmentManager, "Tu monstruo es")
        } else if (player2.countReverseInt() == 0) {
            player2.setCardChoicedAnswer(player1.getCardChoiced())
            val resolveDialog = DialogResolve(this, player2, 1)
            resolveDialog.isCancelable = false
            resolveDialog.show(supportFragmentManager, "Tu monstruo es")
        } else {
            //Se le da el turno al jugador
            Handler(Looper.getMainLooper()).postDelayed({
                val turnMsgDialog = DialogTurnMsg(this, player1)
                turnMsgDialog.show(supportFragmentManager, "mensaje de turno")
                numTurns++
                superBinding.numTurnosTextView.text = "Turno $numTurns"
                superBinding.preguntaButton.isEnabled = true
                superBinding.turnButton.isEnabled = false
            }, 800)
        }
    }

    fun animarCPU(i: Int) {
        val items: RecyclerView = superBinding.recView2Monsters
        //se definen las animaciones de flip
        val front_anim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.animation_vertical_flip_front_in
        ) as AnimatorSet
        val back_anim = AnimatorInflater.loadAnimator(
            applicationContext,
            R.animator.animation_vertical_front_out
        ) as AnimatorSet
        front_anim.setTarget(items[i].findViewById<ImageView>(R.id.monsterBack_image))
        back_anim.setTarget(items[i].findViewById<ImageView>(R.id.monsterFront_image))
        back_anim.start()
        front_anim.start()
    }

    override fun applyDialogResults(res: String, player1wins: Boolean) {
        if (player1wins) {
            db.collection("users").document(usermail).update("solowins", player1.getSolowins() + 1)
        } else {
            db.collection("users").document(usermail)
                .update("sololoses", player1.getSololoses() + 1)
        }
        if (res == "Terminar") {
            val intent = Intent(this, GameMenu::class.java)
            intent.putExtra("User", player1.getUsermail())
            startActivity(intent)
            finish()
        } else if (res == "Nueva partida") {
            val intent = Intent(this, GameSolo::class.java)
            intent.putExtra("User", usermail)
            startActivity(intent)
            finish()
        }
    }
}