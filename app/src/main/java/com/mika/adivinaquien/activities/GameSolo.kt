package com.mika.adivinaquien.activities

import android.animation.AnimatorInflater
import android.animation.AnimatorSet
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mika.adivinaquien.R
import com.mika.adivinaquien.adapters.AdaptadorMonsters
import com.mika.adivinaquien.databinding.ActivityGameSoloBinding
import com.mika.adivinaquien.dialogs.*
import com.mika.adivinaquien.models.Player

class GameSolo : AppCompatActivity(), DialogSelectMonster.DialogSelectMonsterListener, DialogDecideTurn.DialogDecideTurnListener, DialogDuelTurn.DialogDuelTurnListener, DialogQCategory.DialogQCategoryListener{
    private var chronoRunning = false //Determina si el cronómetro esta corriendo
    private var player1 = Player()
    private var player2 = Player()
    private lateinit var superBinding: ActivityGameSoloBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityGameSoloBinding.inflate(layoutInflater)
        superBinding=binding
        setContentView(binding.root)
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        window.decorView.apply {
            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        player1.setNickname("Player1")
        player2.setNickname("Player2")
        val selectMonsterDialog = DialogSelectMonster(this, player1)
        selectMonsterDialog.isCancelable=false
        selectMonsterDialog.show(supportFragmentManager, "Selecciona tu monstruo")

    }

    override fun applySelectMonster(player: Player) {
        this.player1=player
        val decideTurnDialog = DialogDecideTurn(this, player)
        decideTurnDialog.isCancelable=false
        decideTurnDialog.show(supportFragmentManager, "Decidir turno")

    }

    override fun applyDecideTurn(player: Player) {
        this.player1=player
        player2.setPpt((0..2).random())
        val duelTurnDialog = DialogDuelTurn(this, player1, player2)
        duelTurnDialog.isCancelable=false
        duelTurnDialog.show(supportFragmentManager, "Decidir turno")

    }

    override fun applyDuelTurn(player1: Player, player2: Player, isDraw: Boolean) {
        this.player1=player1
        this.player2=player2
        if(isDraw==true){
            val decideTurnDialog = DialogDecideTurn(this, player1)
            decideTurnDialog.isCancelable=false
            decideTurnDialog.show(supportFragmentManager, "Decidir turno")
        }
        else{
            iniciarJuego()
        }
    }

    fun iniciarJuego(){
        var isWinner = false
        var numTurns = 0
        var playersTurn=Player()
        superBinding.crono.base= SystemClock.elapsedRealtime()
        superBinding.crono.start()
        chronoRunning=true

        //para que el player2 tenga el mismo deck (orden) que el player2
        player2.copyDeck(player1)
        //Mostrar el tablero
        superBinding.tablero.visibility= View.VISIBLE
        //Tablero 2
        var adaptador = AdaptadorMonsters(player2, 2){}
        superBinding.recView2Monsters.layoutManager = GridLayoutManager(this, 7)
        superBinding.recView2Monsters.adapter = adaptador
        //Tablero 1
        adaptador = AdaptadorMonsters(player1, 0){
            val items: RecyclerView =superBinding.recViewMonsters
            //se definen las animaciones de flip
            val front_anim = AnimatorInflater.loadAnimator(applicationContext,R.animator.animation_vertical_flip_front_in) as AnimatorSet
            val back_anim = AnimatorInflater.loadAnimator(applicationContext,R.animator.animation_vertical_front_out) as AnimatorSet
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
            superBinding.numCartas1TextView.text=player1.countReverse()
        }
        superBinding.recViewMonsters.layoutManager = GridLayoutManager(this, 7)
        superBinding.recViewMonsters.adapter = adaptador
        //Muestra la imagen del monstruo seleccionado
        val resourceId: Int =
            this.resources.getIdentifier("mob${player1.getCardChoiced()}", "drawable", this.packageName)
        superBinding.myMonsterImage.setImageResource(resourceId)

        while(!isWinner){
            if(player1.getIsFirst()){
                playersTurn=player1
                player1.setIsFirst(false)
            }
            else if(player2.getIsFirst()){
                playersTurn=player2
                player2.setIsFirst(false)
            }
            numTurns++
            superBinding.numTurnosTextView.text="Turno $numTurns"
            val turnMsgDialog = DialogTurnMsg(this, playersTurn)
            turnMsgDialog.show(supportFragmentManager, "mensaje de turno")
            isWinner=true
        }
        superBinding.preguntaButton.setOnClickListener{(hacerPregunta(playersTurn))}

    }
    fun hacerPregunta(playersTurn:Player){
        val qCategoryDialog = DialogQCategory(this,-1)
        qCategoryDialog.isCancelable=false
        qCategoryDialog.show(supportFragmentManager, "Atributo")
    }

    override fun applyQCategory(itemType: Int, inDialog: String) {
        when(inDialog){
            "categories" -> dCategories(itemType)
            "colors" -> dColors(itemType)
            "eyes" -> dEyes(itemType)
            "head" ->dHead(itemType)
            "limbs" ->dLimbs(itemType)
            "expression" ->dExpression(itemType)
        }

    }

    fun dCategories(selected: Int){
        val qCategoryDialog = DialogQCategory(this,selected)
        qCategoryDialog.isCancelable=false
        qCategoryDialog.show(supportFragmentManager, "Atributo")
    }

    fun dColors(selected: Int){
        var pregunta="¿Su cuerpo es color"
        when(selected){
            0 -> pregunta="$pregunta verde?"
            1 -> pregunta="$pregunta rojo?"
            2 -> pregunta="$pregunta morado?"
            3 -> pregunta="$pregunta azul?"
            4 -> pregunta="$pregunta café?"
            5 -> pregunta="$pregunta gris?"
        }
        val doQuestionDialog = DialogDoQuestion(this,pregunta)
        doQuestionDialog.isCancelable=false
        doQuestionDialog.show(supportFragmentManager, "Hacer pregunta")
    }

    fun dEyes(selected: Int){
        var pregunta="¿Tiene "
        when(selected){
            0 -> pregunta="$pregunta un ojo?"
            1 -> pregunta="$pregunta dos ojos?"
            2 -> pregunta="$pregunta tres ojos?"
        }
        val doQuestionDialog = DialogDoQuestion(this,pregunta)
        doQuestionDialog.isCancelable=false
        doQuestionDialog.show(supportFragmentManager, "Hacer pregunta")
    }
    fun dHead(selected: Int){
        var pregunta="¿Tiene"
        when(selected){
            0 -> pregunta="$pregunta pelo?"
            1 -> pregunta="$pregunta nariz?"
            2 -> pregunta="$pregunta dientes?"
            3 -> pregunta="$pregunta lengua?"
            4 -> pregunta="$pregunta antenas?"
            5 -> pregunta="$pregunta cuernos?"
            6 -> pregunta="$pregunta orejas?"
        }
        val doQuestionDialog = DialogDoQuestion(this,pregunta)
        doQuestionDialog.isCancelable=false
        doQuestionDialog.show(supportFragmentManager, "Hacer pregunta")
    }
    fun dLimbs(selected: Int){
        var pregunta="¿Tiene"
        when(selected){
            0 -> pregunta="$pregunta brazos?"
            1 -> pregunta="$pregunta piernas?"
            2 -> pregunta="$pregunta tentáculos?"
            3 -> pregunta="¿No tiene brazos?"
            4 -> pregunta="¿No tiene piernas?"
            5 -> pregunta="¿No tiene tentáculos?"
        }
        val doQuestionDialog = DialogDoQuestion(this,pregunta)
        doQuestionDialog.isCancelable=false
        doQuestionDialog.show(supportFragmentManager, "Hacer pregunta")
    }
    fun dExpression(selected: Int){
        var pregunta="¿Está"
        when(selected){
            0 -> pregunta="$pregunta feliz?"
            1 -> pregunta="$pregunta enojado?"
            2 -> pregunta="$pregunta triste?"
            3 -> pregunta="$pregunta sorprendido?"
            4 -> pregunta="¿No tiene expresión?"
        }
        val doQuestionDialog = DialogDoQuestion(this,pregunta)
        doQuestionDialog.isCancelable=false
        doQuestionDialog.show(supportFragmentManager, "Hacer pregunta")
    }

}