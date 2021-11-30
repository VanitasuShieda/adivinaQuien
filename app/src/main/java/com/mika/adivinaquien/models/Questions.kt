package com.mika.adivinaquien.models

//Clase para las preguntas que se muestran en la lista de preguntas realizadas
class Questions (
    val turn: Int,
    val question: String,
    val answer: String,
    val whoAsks: Player,
    val whoAnswers: Player
    ){}