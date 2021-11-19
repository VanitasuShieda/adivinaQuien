package com.mika.adivinaquien.models

data class Game(
    var id: String = "",
    var name: String = "",
    var users: List<String> = emptyList()
)