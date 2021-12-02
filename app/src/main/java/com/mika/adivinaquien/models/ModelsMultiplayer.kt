package com.mika.adivinaquien.models

data class ModelsMultiplayer (
        var card: Int = 0
)

data class Turn(
        var turn: Int = 0,
        var usr: String = ""
)

data class Status(
        var result: String = ""
)