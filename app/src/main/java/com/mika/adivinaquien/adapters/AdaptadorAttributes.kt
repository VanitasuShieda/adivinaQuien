package com.mika.adivinaquien.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mika.adivinaquien.R
import com.mika.adivinaquien.models.Attribute
import com.mika.adivinaquien.models.Categories

class AdaptadorAttributes(private val attributes: Attribute, private val itemType:Int,
                          private val clickListener: (Int) -> Unit):
    RecyclerView.Adapter<AdaptadorAttributes.AttributesViewHolder>() {
    class AttributesViewHolder(item: View): RecyclerView.ViewHolder(item){
        //Se crea el RecyclerView almacenando los items de la lista en el layout
        val imagen = item.findViewById(R.id.attribute_image) as ImageView
        val titulo = item.findViewById(R.id.titulo_TextView) as TextView
        fun bindAttribute(category: Categories){
            imagen.setImageResource(category.imagen)
            titulo.text=category.titulo
        }
    }
    override fun onCreateViewHolder (parent: ViewGroup, viewType: Int): AttributesViewHolder {
        val item: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.listitem_attributes, parent, false) as LinearLayout

        return AttributesViewHolder(item)
    }
    //Para determinar el evento click de cada item
    override fun onBindViewHolder (holder: AttributesViewHolder, position: Int) {
        var attribute: Categories? = null
        if(itemType==-1){//(-1) -> para las categorías
            attribute=attributes.getCategories()[position]
        }else if(itemType==0){//(0) -> para los colores
            attribute=attributes.getColor()[position]
        }else if(itemType==1){//(1) -> para número de ojos
            attribute=attributes.getEyes()[position]
        }else if(itemType==2){//(2) -> para la cabeza
            attribute=attributes.getHead()[position]
        }else if(itemType==3){//(3) -> para las extremidades
            attribute=attributes.getLimbs()[position]
        }else if(itemType==4){//(4) -> para las expresiones
            attribute=attributes.getExpression()[position]
        }
        holder.bindAttribute(attribute!!)
        holder.itemView.setOnClickListener{clickListener(position)}
    }

    override fun getItemCount():Int {
        var size:Int? = null
        if(itemType==-1){//(-1) -> para las categorías
            size = attributes.getCategories().size
        }else if(itemType==0){//(0) -> para los colores
            size = attributes.getColor().size
        }else if(itemType==1){//(1) -> para número de ojos
            size = attributes.getEyes().size
        }else if(itemType==2){//(2) -> para la cabeza
            size = attributes.getHead().size
        }else if(itemType==3){//(3) -> para las extremidades
            size = attributes.getLimbs().size
        }else if(itemType==4){//(4) -> para las expresiones
            size = attributes.getExpression().size
        }
        return size!!
    }
}