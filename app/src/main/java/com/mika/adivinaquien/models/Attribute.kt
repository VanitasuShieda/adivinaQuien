package com.mika.adivinaquien.models

import com.mika.adivinaquien.R

class Attribute (){
    private var categories:MutableList<Categories> = mutableListOf(
        Categories(R.drawable.color,"Color"),
        Categories(R.drawable.eyes,"Ojos"),
        Categories(R.drawable.head,"Cabeza"),
        Categories(R.drawable.limbs,"Extremidades"),
        Categories(R.drawable.expression,"Expresión")
    )
    private var color:MutableList<Categories> = mutableListOf(
        Categories(R.drawable.color_green,"Verde"),
        Categories(R.drawable.color_red,"Rojo"),
        Categories(R.drawable.color_purple,"Morado"),
        Categories(R.drawable.color_blue,"Azul"),
        Categories(R.drawable.color_brown,"Café"),
        Categories(R.drawable.color_gray,"Gris")
    )
    private var eyes:MutableList<Categories> = mutableListOf(
        Categories(R.drawable.one_eye,"Un ojo"),
        Categories(R.drawable.two_eyes,"Dos ojos"),
        Categories(R.drawable.three_eyes,"Tres ojos")
    )
    private var head:MutableList<Categories> = mutableListOf(
        Categories(R.drawable.furry,"Pelo"),
        Categories(R.drawable.nose,"Nariz"),
        Categories(R.drawable.teeth,"Dientes"),
        Categories(R.drawable.tongue,"Lengua"),
        Categories(R.drawable.antennae,"Antenas"),
        Categories(R.drawable.horns,"Cuernos"),
        Categories(R.drawable.ears,"Orejas"),
    )
    private var limbs:MutableList<Categories> = mutableListOf(
        Categories(R.drawable.arms,"Con brazos"),
        Categories(R.drawable.legs,"Con piernas"),
        Categories(R.drawable.tentacle,"Con tentáculos")
    )
    private var expression:MutableList<Categories> = mutableListOf(
        Categories(R.drawable.happy,"Feliz"),
        Categories(R.drawable.anger,"Enojado"),
        Categories(R.drawable.sadness,"Triste"),
        Categories(R.drawable.surprise,"Sorprendido"),
        Categories(R.drawable.no_expresion,"Sin expresión")
    )

    fun getCategories():MutableList<Categories>{return categories}
    fun getColor():MutableList<Categories>{return color}
    fun getEyes():MutableList<Categories>{return eyes}
    fun getHead():MutableList<Categories>{return head}
    fun getLimbs():MutableList<Categories>{return limbs}
    fun getExpression():MutableList<Categories>{return expression}
}
