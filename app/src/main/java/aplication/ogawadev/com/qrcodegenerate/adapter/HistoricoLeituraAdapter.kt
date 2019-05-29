package aplication.ogawadev.com.qrcodegenerate.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeHistoricoLeitura

class HistoricoLeituraAdapter(private val context: Context, private val qrCodesLeitura: List<QRCodeHistoricoLeitura> ) : BaseAdapter(){
    var listQrCode = qrCodesLeitura
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        var qrCodeHistorico = listQrCode.get(position)
        var view  = convertView
        if(view == null){
            view = View.inflate(context,
                R.layout.layout_list_historico_leitura,null)
        }


        val imagem = view!!.findViewById<ImageView>(R.id.imagemHistorico)
        val nomeModelo = view!!.findViewById<TextView>(R.id.nomeModelo)
        val conteudo = view!!.findViewById<TextView>(R.id.conteudoQrCode)
        val data = view!!.findViewById<TextView>(R.id.dataQrCode)

        imagem.setImageResource(qrCodeHistorico.imagemTitulo.imgId)
        nomeModelo.setText(qrCodeHistorico.imagemTitulo.nome)

        if(qrCodeHistorico.conteudoQrCode.contains("Crip&")){
            conteudo.setText("Criptografado")
        }
        else{
            if(qrCodeHistorico.conteudoQrCode.length > 20){
                conteudo.setText(qrCodeHistorico.conteudoQrCode.substring(0,20))
            }
            else{
                conteudo.setText(qrCodeHistorico.conteudoQrCode)
            }
        }
        data.setText(qrCodeHistorico.dataLeitura)

        return view

    }

    override fun getItem(position: Int): Any {
        return listQrCode.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return listQrCode.size
    }
}