package com.mika.adivinaquien.adapters

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.mika.adivinaquien.R
import com.mika.adivinaquien.models.Gameplay
import java.io.File

class gamesAdapter (): RecyclerView.Adapter<gamesAdapter.GameViewHolder>() {
    private var db = FirebaseFirestore.getInstance()
    private var games: List<Gameplay> = emptyList()
    private lateinit var mStorage: FirebaseStorage
    private lateinit var mReference: StorageReference

    fun setData(list: List<Gameplay>){
        games = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        return GameViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_game,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        val game = games[position]

        val refnick = db.collection("users").get()
        var otheremail = ""
        var bitmap: Bitmap? = null

        refnick.addOnSuccessListener { documentos ->

            for(user in documentos){
                if(game.vs.lowercase() == user.get("nick").toString()){
                    otheremail=user.get("email").toString().lowercase()
                    break
                }
            }

            mStorage = FirebaseStorage.getInstance()
            mReference = mStorage.reference

            val imgRef = mReference.child("images/$otheremail")
            val localfile = File.createTempFile("tempImg","jpg")

            imgRef.getFile(localfile).addOnSuccessListener {
                bitmap = BitmapFactory.decodeFile(localfile.absolutePath)
            }

            val options = RequestOptions()
            options.centerCrop().fitCenter()
            Glide.with(holder.itemView.context).load(bitmap).apply(options).into(holder.itemView.findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.rivalimage))


        }.addOnFailureListener{ exeption ->
            println(exeption)
        }

        if("Victoria" == game.status){
            //victoria
            holder.itemView.findViewById<TextView>(R.id.usergame).text = game.vs
            holder.itemView.findViewById<TextView>(R.id.gamestatus).text = game.status
            holder.itemView.findViewById<TextView>(R.id.gamestatus).setBackgroundResource(R.color.holo_green_light)
        } else {
            //derrota
            holder.itemView.findViewById<TextView>(R.id.usergame).text = game.vs
            holder.itemView.findViewById<TextView>(R.id.gamestatus).text = game.status
            holder.itemView.findViewById<TextView>(R.id.gamestatus).setBackgroundResource(R.color.holo_purple)
        }

    }

    override fun getItemCount(): Int {
        return games.size
    }

    class GameViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
}