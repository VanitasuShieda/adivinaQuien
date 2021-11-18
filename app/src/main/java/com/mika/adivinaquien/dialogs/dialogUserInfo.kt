package com.mika.adivinaquien.dialogs

import android.app.ActionBar
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import com.mika.adivinaquien.R
import com.mika.adivinaquien.databinding.DialogInfoBinding
import com.mika.adivinaquien.models.User

class dialogUserInfo(private val userinf: User?): DialogFragment()  {
    private lateinit var listener:dialogUserInfoListener

    interface dialogUserInfoListener {
        //fun changeUserInf(nick: String, email: String, pass: String, imgFoto: Uri?)
    }

    override fun onCreateDialog( savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val binding = inflater.inflate(R.layout.dialog_info, null)

            var nick = binding.findViewById<TextView>(R.id.nickinf)
            nick.text = userinf?.nick


            builder.setView(binding)
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

}