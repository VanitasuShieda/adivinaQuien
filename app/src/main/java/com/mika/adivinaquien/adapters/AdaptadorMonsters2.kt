package com.mika.adivinaquien.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.mika.adivinaquien.R
import com.mika.adivinaquien.models.Monster
import com.mika.adivinaquien.models.Player

//Para mostrar el RecyclerView con el deck de los monstruos
class AdaptadorMonsters2(private val player: Player, private val itemType:Int,
                        private val clickListener: (Int) -> Unit):
    RecyclerView.Adapter<AdaptadorMonsters2.MonstersViewHolder>() {
    class MonstersViewHolder(item: View): RecyclerView.ViewHolder(item){
        //Se crea el RecyclerView almacenando los items de la lista en el layout
        val imagen = item.findViewById(R.id.monsterFront_image) as ImageView
        fun bindMonster(monster: Monster, itemType:Int){
            if(itemType == 2){
                imagen.setImageResource(R.drawable.reverso)
            }else{
                imagen.setImageResource(monster.imagen)
            }
        }
    }
    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int): MonstersViewHolder {
        var item: View? = null
        //Dependiendo del itemType se determina el tipo de deck que se crea
        //(0) -> para el tablero del jugador
        if(itemType == 0){
            item = LayoutInflater.from(parent.context)
                .inflate(R.layout.listitem_monsters, parent, false) as LinearLayout
        }
        //(1) -> para el tablero del dialogo monster select
        else if(itemType == 1){
            item = LayoutInflater.from(parent.context)
                .inflate(R.layout.listitem_monsterselect, parent, false) as LinearLayout
        }
        //(2) -> para el tablero del jugador 2
        else if(itemType == 2){
            item = LayoutInflater.from(parent.context)
                .inflate(R.layout.listitem_monstersplayer2, parent, false) as LinearLayout
        }
        return MonstersViewHolder(item!!)
    }
    //Para determinar el evento click de cada item
    override fun onBindViewHolder (holder: MonstersViewHolder, position: Int) {
        val monster=player.getMyDeck()[position]
        holder.bindMonster(monster,itemType)
        holder.itemView.setOnClickListener{clickListener(position)}
    }

    override fun getItemCount() = player.getMyDeck().size

}