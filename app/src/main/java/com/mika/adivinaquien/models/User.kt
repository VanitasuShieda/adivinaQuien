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
)
