package aplication.ogawadev.com.qrcodegenerate.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import aplication.ogawadev.com.qrcodegenerate.R


class GeradorAdapter(private val context: Context, private val imagensCustomizadas: List<aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas> ) : BaseAdapter(){
    var listImagensCustomizadas = imagensCustomizadas
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var imagemCustomizada = listImagensCustomizadas.get(position)
        var view  = convertView
        if(view == null){
            view = View.inflate(context,
                R.layout.layout_listview_gerador,null)
        }


        val imageViewGerador = view!!.findViewById<ImageView>(R.id.imageViewGerador)
        val textViewGerador = view!!.findViewById<TextView>(R.id.textViewGerador)

        imageViewGerador.setImageResource(imagemCustomizada.imgId)
        if(imagemCustomizada.nome.contains("Crip&")){
            textViewGerador.setText("Criptografado")
        }
        else{
            textViewGerador.setText(imagemCustomizada.nome)
        }


        return view

    }

    override fun getItem(position: Int): Any {
        return listImagensCustomizadas.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return listImagensCustomizadas.size
    }
}