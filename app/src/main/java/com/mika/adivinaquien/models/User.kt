package com.mika.adivinaquien.models

data class User(
    var online: Boolean ,
    var id: String = "",
    var nick: String = "",
    var email: String = "",
    var solowins: Int = 0,
    var sololoses: Int = 0,
    var multiwins: Int = 0,
    var multiloses: Int = 0,
    var multiplayergames: List<String> = emptyList()
    //Contra quien jugo y el resultado de la partida
    //Ejemplo: listof(User2, Victoria)
)
