package com.mika.adivinaquien.dialogs

import android.app.ActionBar
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mika.adivinaquien.adapters.gamesAdapter
import com.mika.adivinaquien.databinding.DialogInfoBinding
import com.mika.adivinaquien.models.Gameplay
import com.mika.adivinaquien.models.User

class dialogUserInfo(private val userinf: User?, private val bitmap: Bitmap?): DialogFragment()  {
       private lateinit var binding: DialogInfoBinding
       private var db = Firebase.firestore
        private  var user = ""

    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            binding =  DialogInfoBinding.inflate(layoutInflater)

            binding.usernick.text = userinf?.nick
            binding.useremail.text = userinf?.email
            binding.solowins.text  = userinf?.solowins.toString()
            binding.soloLose.text  = userinf?.sololoses.toString()
            binding.MultiLose.text  = userinf?.multiloses.toString()
            binding.MultiWins.text  = userinf?.multiwins.toString()

            user = binding.useremail.text.toString()

            val options = RequestOptions()
            options.centerCrop().fitCenter()
            Glide.with(this).load(bitmap).apply(options).into(binding.avatar)

            initViews()

            builder.setView(binding.root)
                .setPositiveButton("Ok",
                    DialogInterface.OnClickListener { dialog, id ->

                    })
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onResume() {
        super.onResume()
        val params: ViewGroup.LayoutParams = dialog!!.window!!.attributes
        params.width = ActionBar.LayoutParams.MATCH_PARENT
        params.height = ActionBar.LayoutParams.MATCH_PARENT
        dialog!!.window!!.attributes = params as WindowManager.LayoutParams
    }

    private fun initViews(){
        //db.collection("users").document(user).collection("Multiplayergames").document().set(partida)
        binding.recyclergames.layoutManager = LinearLayoutManager(context)
        binding.recyclergames.adapter = gamesAdapter()

        val gamesRef = db.collection("users").document(user)

        gamesRef.collection("Multiplayergames").orderBy("dob", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { game ->
                val listGames = game.toObjects(Gameplay::class.java)
                (binding.recyclergames.adapter as gamesAdapter).setData(listGames)
            }

        gamesRef.collection("Multiplayergames").orderBy("dob", Query.Direction.ASCENDING)
            .addSnapshotListener { game, error ->
                if(error == null){
                    game?.let {
                        val listGames = it.toObjects(Gameplay::class.java)
                        (binding.recyclergames.adapter as gamesAdapter).setData(listGames)
                    }
                }
            }
    }


}