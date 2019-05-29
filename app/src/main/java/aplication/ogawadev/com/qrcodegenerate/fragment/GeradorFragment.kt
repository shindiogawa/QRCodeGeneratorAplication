package aplication.ogawadev.com.qrcodegenerate.fragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.manager.MainActivity
import aplication.ogawadev.com.qrcodegenerate.model.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_gerador.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class GeradorFragment : Fragment() {

    var list: ListView? = null
    var positionInArray = 0
    var arrayModelosPersonalizados = ArrayList<String>()
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gerador, container, false)

        MainActivity.navMenu!!.selectedItemId = R.id.itemGerar

        list = view.findViewById(R.id.listLeitor)
        var arrayAdapter =
            aplication.ogawadev.com.qrcodegenerate.adapter.GeradorAdapter(context!!, initListImages())
        //registerForContextMenu(list)
        list?.adapter = arrayAdapter

        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeLeft() {
                MainActivity.setFragment(1, context!!)
            }
        })

        var btnHistorico = view.findViewById<Button>(R.id.btnHistoricoGerados)
        btnHistorico.setOnClickListener {
            btnHistoricoGerados.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
            Timer().schedule(500){
                btnHistoricoGerados.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.black))
            }
            var intent = Intent(context, HistoricoGerado::class.java)
            startActivity(intent)
        }

        list?.setOnItemClickListener { parent, view, position, id ->
            if( position == 0){
                val intent = Intent(context, GerarQRCodePersonalizado::class.java)
                startActivityForResult(intent, 1)
                //startActivity(intent)
            }

            if(position == 1){
                val intent = Intent(context, GerarQRCodeEmail::class.java)
                startActivity(intent)
            }
            if(position == 2){
                val intent = Intent(context, GerarQRCodeMensagem::class.java)
                startActivity(intent)
            }
            if (position == 3){
                val intent = Intent(context, GerarQRCodeContato::class.java)
                startActivity(intent)
            }

            if(position == 4){
                val intent = Intent(context, GerarQRCodeTelefone::class.java)
                startActivity(intent)
            }

            if(position == 5){
                val intent = Intent(context, GerarQRCodeTexto::class.java)
                startActivity(intent)
            }

            if(position == 6){
                val intent = Intent(context, GerarQRCodeGeoLocalizacao::class.java)
                startActivity(intent)
            }

            if(position == 7){
                val intent = Intent(context, GerarQRCodeEvento::class.java)
                startActivity(intent)
            }

            if (position == 8){
                val intent = Intent(context, GerarQRCodeUrl::class.java)
                startActivity(intent)
            }

            if (position == 9){
                val intent = Intent(context, GerarQRCodeWifi::class.java)
                startActivity(intent)
            }

            if(!(arrayModelosPersonalizados.isNullOrEmpty())){
                if(position > 9 && position <= (9 + arrayModelosPersonalizados.size)){
                    val intent = Intent(context, GerarQRCodePersonalizadoSalvo::class.java)
                    positionInArray = position - 10
                    intent.putExtra("modelo", arrayModelosPersonalizados.get(positionInArray))
                    startActivityForResult(intent,2)
                }
            }
        }

        return view
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == 1){
            if(resultCode == Activity.RESULT_OK){
                val resposta = data!!.getBooleanExtra("resposta", false)
                if(resposta){
                    var arrayAdapter = aplication.ogawadev.com.qrcodegenerate.adapter.GeradorAdapter(
                        context!!,
                        initListImages()
                    )
                    list?.adapter = arrayAdapter
                }
            }
        }
        if(requestCode == 2){
            if(resultCode == Activity.RESULT_OK){
                val retorno = data!!.getBooleanExtra("retorno", false)
                if(retorno){
                    arrayModelosPersonalizados.removeAt(positionInArray)
                    val sharedPreferences = this.activity!!.getSharedPreferences("modelos", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    val gson = Gson()
                    val json = gson.toJson(arrayModelosPersonalizados)
                    editor.putString("listaModelos", json)
                    editor.apply()
                    var arrayAdapter = aplication.ogawadev.com.qrcodegenerate.adapter.GeradorAdapter(
                        context!!,
                        initListImages()
                    )
                    list?.adapter = arrayAdapter
                }
            }
        }
    }

    private  fun initListImages() : ArrayList<ImagensCustomizadas> {
        var imagens = ArrayList<ImagensCustomizadas>()

        imagens.add(
            ImagensCustomizadas(
                context!!.getString(R.string.img_name_personalizado),
                R.drawable.personalizado
            )
        )

        imagens.add(
            ImagensCustomizadas(
                context!!.getString(R.string.img_name_email),
                R.drawable.email
            )
        )
        imagens.add(
            ImagensCustomizadas(
                context!!.getString(R.string.img_name_mensagem),
                R.drawable.mensagem
            )
        )
        imagens.add(
            ImagensCustomizadas(
                context!!.getString(R.string.img_name_contato),
                R.drawable.contato
            )
        )
        imagens.add(
            ImagensCustomizadas(
                context!!.getString(R.string.img_name_telefone),
                R.drawable.telefone
            )
        )
        imagens.add(
            ImagensCustomizadas(
                context!!.getString(R.string.img_name_texto),
                R.drawable.texto
            )
        )
        imagens.add(
            ImagensCustomizadas(
                context!!.getString(R.string.img_name_localizacao),
                R.drawable.localizacao
            )
        )
        imagens.add(
            ImagensCustomizadas(
                context!!.getString(R.string.img_name_evento),
                R.drawable.evento
            )
        )
        imagens.add(
            ImagensCustomizadas(
                context!!.getString(R.string.img_name_url),
                R.drawable.url
            )
        )
        imagens.add(
            ImagensCustomizadas(
                "Wifi",
                R.drawable.wifi
            )
        )

        carregarModelos()
        if(!(arrayModelosPersonalizados.isNullOrEmpty())){
            for (i in 0..arrayModelosPersonalizados.size-1){
                var quebraNome = arrayModelosPersonalizados.get(i).split("&")
                imagens.add(ImagensCustomizadas(quebraNome.get(0), R.drawable.personalizado))
            }
        }

        return imagens
    }

    fun carregarModelos (){
        var sharedPreferences = this.activity!!.getSharedPreferences("modelos", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("listaModelos", null)
        val type = object: TypeToken<ArrayList<String>>(){}.type
        if(json != null){
            arrayModelosPersonalizados = gson.fromJson(json, type)
        }
    }
}


