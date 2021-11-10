package com.mika.adivinaquien.models

data class chat(
    var id: String = "",
    var name: String = "",
    var users: List<String> = emptyList()
)
