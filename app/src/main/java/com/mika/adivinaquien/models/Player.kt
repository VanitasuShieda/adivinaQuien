package com.mika.adivinaquien.models

import com.mika.adivinaquien.R


class Player() {
    //tdas las cartas
    private val monsterList:MutableList<Monster> = mutableListOf(
        Monster(id = 0,name = "UNI-DIENTE",isReverse = false,color = "VERDE",eyes = 1,nose = false, legs = false, arms = false, tentacles = true, horns = false, ears = false, antennae = false, furry = false, expression = "SORPRENDIDO", teeth = true, tongue = false, R.drawable.mob0),
        Monster(id = 1,name = "SIMPLÓN",isReverse = false,color = "ROJO",eyes = 2,nose = false, legs = true, arms = true, tentacles = false, horns = false, ears = false, antennae = false, furry = false, expression = "SORPRENDIDO", teeth = false,tongue = false, R.drawable.mob1),
        Monster(id = 2,name = "DRAGÓN",isReverse = false,color = "ROJO",eyes = 2,nose = true, legs = true, arms = true, tentacles = false, horns = true, ears = true, antennae = false, furry = false, expression = "FELIZ",teeth = true, tongue = false, R.drawable.mob2),
        Monster(id = 3,name = "TRES-OJOS",isReverse = false,color = "VERDE",eyes = 3,nose = true, legs = true, arms = true, tentacles = false, horns = false, ears = false, antennae = false, furry = false, expression = "FELIZ",teeth = true, tongue = false, R.drawable.mob3),
        Monster(id = 4,name = "CUERNOS",isReverse = false,color = "ROJO",eyes = 3,nose = false, legs = true, arms = true, tentacles = false, horns = true, ears = false, antennae = false, furry = false, expression = "SORPRENDIDO",teeth = false, tongue = true, R.drawable.mob4),
        Monster(id = 5,name = "FELICIDAD",isReverse = false,color = "MORADO",eyes = 2,nose = false, legs = true, arms = true, tentacles = false, horns = false, ears = true, antennae = false, furry = false, expression = "FELIZ",teeth = true, tongue = false, R.drawable.mob5),
        Monster(id = 6,name = "NARIZ-ROJA",isReverse = false,color = "CAFÉ",eyes = 2,nose = true, legs = true, arms = false, tentacles = false, horns = false, ears = true, antennae = false, furry = true, expression = "SIN EXPRESIÓN",teeth = false, tongue = false, R.drawable.mob6),
        Monster(id = 7,name = "LENGUADO",isReverse = false,color = "VERDE",eyes = 1,nose = false, legs = true, arms = true, tentacles = false, horns = false, ears = false, antennae = false, furry = false, expression = "SORPRENDIDO",teeth = false, tongue = true, R.drawable.mob7),
        Monster(id = 8,name = "DUENDE",isReverse = false,color = "VERDE",eyes = 2,nose = false, legs = true, arms = true, tentacles = false, horns = true, ears = true, antennae = false, furry = false, expression = "ENOJADO",teeth = true, tongue = false, R.drawable.mob8),
        Monster(id = 9,name = "UNI-ROJO",isReverse = false,color = "ROJO",eyes = 1,nose = false, legs = true, arms = true, tentacles = false, horns = false, ears = false, antennae = false, furry = false, expression = "ENOJADO",teeth = false, tongue = false, R.drawable.mob9),
        Monster(id = 10,name = "OREJÓN",isReverse = false,color = "VERDE",eyes = 2,nose = true, legs = true, arms = true, tentacles = false, horns = true, ears = true, antennae = false, furry = false, expression = "TRISTE",teeth = false, tongue = false, R.drawable.mob10),
        Monster(id = 11,name = "GIGANTE",isReverse = false,color = "CAFÉ",eyes = 2,nose = false, legs = true, arms = false, tentacles = false, horns = false, ears = true, antennae = false, furry = true, expression = "FELIZ",teeth = true, tongue = false, R.drawable.mob11),
        Monster(id = 12,name = "ANTENAS",isReverse = false,color = "VERDE",eyes = 1,nose = false, legs = false, arms = false, tentacles = true, horns = false, ears = false, antennae = true, furry = false, expression = "FELIZ",teeth = true, tongue = false, R.drawable.mob12),
        Monster(id = 13,name = "BABOSA",isReverse = false,color = "MORADO",eyes = 2,nose = false, legs = false, arms = true, tentacles = false, horns = false, ears = false, antennae = false, furry = false, expression = "TRISTE",teeth = false, tongue = false, R.drawable.mob13),
        Monster(id = 14,name = "PIEDRA",isReverse = false,color = "AZUL",eyes = 2,nose = true, legs = true, arms = false, tentacles = false, horns = true, ears = false, antennae = false, furry = false, expression = "ENOJADO",teeth = true, tongue = false, R.drawable.mob14),
        Monster(id = 15,name = "BESUCONA",isReverse = false,color = "MORADO",eyes = 2,nose = false, legs = false, arms = false, tentacles = true, horns = false, ears = false, antennae = false, furry = false, expression = "ENOJADO",teeth = false, tongue = false, R.drawable.mob15),
        Monster(id = 16,name = "ILUISIÓN",isReverse = false,color = "MORADO",eyes = 1,nose = false, legs = true, arms = true, tentacles = false, horns = true, ears = false, antennae = false, furry = false, expression = "TRISTE",teeth = true, tongue = false, R.drawable.mob16),
        Monster(id = 17,name = "TOPOS",isReverse = false,color = "AZUL",eyes = 2,nose = false, legs = true, arms = true, tentacles = false, horns = false, ears = false, antennae = false, furry = false, expression = "TRISTE",teeth = true, tongue = false, R.drawable.mob17),
        Monster(id = 18,name = "GUISANTE",isReverse = false,color = "VERDE",eyes = 1,nose = false, legs = true, arms = true, tentacles = false, horns = false, ears = false, antennae = true, furry = false, expression = "SIN EXPRESIÓN",teeth = false, tongue = false, R.drawable.mob18),
        Monster(id = 19,name = "ELEFANTE",isReverse = false,color = "GRIS",eyes = 2,nose = true, legs = true, arms = false, tentacles = false, horns = false, ears = true, antennae = false, furry = true, expression = "TRISTE",teeth = false, tongue = false, R.drawable.mob19),
        Monster(id = 20,name = "GUERRERO",isReverse = false,color = "MORADO",eyes = 1,nose = false, legs = true, arms = false, tentacles = false, horns = false, ears = false, antennae = false, furry = false, expression = "SIN EXPRESIÓN",teeth = false, tongue = false, R.drawable.mob20),
        Monster(id = 21,name = "MÁSCARA",isReverse = false,color = "ROJ0",eyes = 2,nose = true, legs = true, arms = false, tentacles = false, horns = false, ears = false, antennae = false, furry = true, expression = "ENOJADO",teeth = false, tongue = false, R.drawable.mob21),
        Monster(id = 22,name = "ALEGRÍA",isReverse = false,color = "ROJ0",eyes = 1,nose = true, legs = true, arms = false, tentacles = false, horns = false, ears = false, antennae = true, furry = false, expression = "FELIZ",teeth = false, tongue = true, R.drawable.mob22),
        Monster(id = 23,name = "BERENJENA",isReverse = false,color = "MORADO",eyes = 1,nose = false, legs = true, arms = true, tentacles = false, horns = true, ears = false, antennae = false, furry = true, expression = "ENOJADO",teeth = true, tongue = false, R.drawable.mob23),
        Monster(id = 24,name = "DIENTES",isReverse = false,color = "AZUL",eyes = 2,nose = false, legs = true, arms = true, tentacles = false, horns = false, ears = false, antennae = false, furry = false, expression = "SORPRENDIDO",teeth = true, tongue = false, R.drawable.mob24),
        Monster(id = 25,name = "PELUDO",isReverse = false,color = "CAFÉ",eyes = 2,nose = false, legs = true, arms = true, tentacles = false, horns = false, ears = false, antennae = false, furry = true, expression = "ENOJADO",teeth = true, tongue = false, R.drawable.mob25),
        Monster(id = 26,name = "MORDISCOS",isReverse = false,color = "CAFÉ",eyes = 2,nose = false, legs = true, arms = false, tentacles = false, horns = true, ears = false, antennae = false, furry = true, expression = "ENOJADO",teeth = true, tongue = false, R.drawable.mob26),
        Monster(id = 27,name = "MONO",isReverse = false,color = "CAFÉ",eyes = 3,nose = false, legs = true, arms = false, tentacles = false, horns = false, ears = true, antennae = false, furry = true, expression = "SORPRENDIDO",teeth = true, tongue = false, R.drawable.mob27),
    )
    private  var nickname:String=""
    private var myDeck:MutableList<Monster> = arrayListOf()
    private var cardChoiced = -1 //la carta (id) que se escogió en el DialogSelectMonster
    private var ppt = -1 //piedra(0), papel(1) o tijera(2)
    private var isFirst = false
    //al instanciar un objeto se crea un deck con un orden aleatorio
    init {
        myDeck=randomizar()
    }
    //funciones getters y setters
    fun getNickname():String{return nickname}
    fun setNickname(nickname: String){this.nickname=nickname}
    fun getMyDeck():MutableList<Monster>{return myDeck}
    fun setMyDeck(myDeck: MutableList<Monster>){this.myDeck=myDeck}
    fun getCardChoiced():Int{return cardChoiced}
    fun setCardChoiced(cardChoiced:Int){this.cardChoiced=cardChoiced}
    fun getPpt():Int{return ppt}
    fun setPpt(ppt:Int){this.ppt=ppt}
    fun getIsFirst():Boolean{return isFirst}
    fun setIsFirst(isFirst:Boolean){this.isFirst=isFirst}

    fun randomizar(): MutableList<Monster> {
        while (myDeck.size < monsterList.size) {
            val random: Int =  (0 until monsterList.size).random()
            if (!myDeck.contains(monsterList[random])) {
                myDeck.add(monsterList[random])
            }
        }
        return myDeck
    }
    fun copyDeck(player: Player){
        this.setMyDeck(player.myDeck)
    }
    fun countReverse():String{
        var count=0
        for (i in myDeck.indices){
            if(myDeck[i].isReverse==false){
                count++
            }
        }
        return "$count / ${myDeck.size}"
    }
}
