package com.mika.adivinaquien.models

data class user(
    var id: String = "",
    var nick: String = "",
    var email: String = "",
    var wins: Int = 0,
    var loses: Int = 0,
    var multiplayergames: List<String> = emptyList()
    //Contra quien jugo y el resultado de la partida
    //Ejemplo: listof(User2, Victoria)
)
