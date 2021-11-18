package com.mika.adivinaquien.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mika.adivinaquien.R
import com.mika.adivinaquien.databinding.GameMultiplayBinding

class GameMultiplayer : AppCompatActivity(){
    private var chatId = ""
    private var user = ""
    private  lateinit var binding: GameMultiplayBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = GameMultiplayBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_multiplay)

        intent.getStringExtra("chatId")?.let { chatId = it }
        intent.getStringExtra("user")?.let { user = it }

        if(chatId.isNotEmpty() && user.isNotEmpty()) {
            //initViews()
        }
    }
}