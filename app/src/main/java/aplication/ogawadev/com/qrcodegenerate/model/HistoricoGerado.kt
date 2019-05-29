package aplication.ogawadev.com.qrcodegenerate.model

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.widget.Button
import android.widget.Toast
import com.baoyz.swipemenulistview.SwipeMenuCreator
import com.baoyz.swipemenulistview.SwipeMenuItem
import com.baoyz.swipemenulistview.SwipeMenuListView
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeCreator
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeHistoricoLeitura
import aplication.ogawadev.com.qrcodegenerate.fragment.GeradorFragment
import aplication.ogawadev.com.qrcodegenerate.fragment.LeitorFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_historico_gerado.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class HistoricoGerado : AppCompatActivity() {

    var arrayHistoricoGerado = ArrayList<QRCodeHistoricoLeitura>()
    var listGerado: SwipeMenuListView? = null
    private var qrCodeCreator = QRCodeCreator()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_historico_gerado, null)
        setContentView(view)

        listGerado = findViewById(R.id.listHistoricoLeitura)
        carregaHistoricoECriaLista()

        btnVoltarHistoricoGerado.setOnClickListener {
            onBackPressed()
        }
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })

        var btnDeleteHistorico = findViewById<Button>(R.id.btnExcluirHistoricoLeituraGerado)
        btnDeleteHistorico.setOnClickListener {
            btnDeleteHistorico.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
            Timer().schedule(500){
                btnDeleteHistorico.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
            }
            AlertDialog.Builder(this).setTitle("Alerta!!")
                .setMessage("Deseja realmente limpar o histórico?")
                .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, which ->
                    limparHistoricoLeitura()
                }).setNegativeButton(android.R.string.no){ dialog, which ->
                    dialog.dismiss()
                }
                .create().show()
        }

        listGerado?.setOnItemClickListener { parent, view, position, id ->
            var bundle = qrCodeCreator.createQRCode(arrayHistoricoGerado.get(position).conteudoQrCode)
            if(bundle == null){
                Toast.makeText(this,"Não foi possível recuperar o QR Code, tente novamente", Toast.LENGTH_SHORT).show()
            }else{
                irParaResultado(bundle)
            }
        }

        val creator = SwipeMenuCreator { menu ->
            // create "delete" item
            val deleteItem = SwipeMenuItem(
                this
            )

            // set item background
            deleteItem.background = ColorDrawable(
                Color.rgb(
                    0xF9,
                    0x3F, 0x25
                )
            )

            // set item width
            deleteItem.width = 200


            // set a icon
            deleteItem.setIcon(R.drawable.ic_delete_white_24dp)

            // add to menu

            menu.addMenuItem(deleteItem)
        }
        listGerado?.setMenuCreator(creator)

        listGerado!!.setOnMenuItemClickListener { position, menu, index ->
            when (index) {
                0 -> AlertDialog.Builder(this).setTitle("Alerta!!")
                    .setMessage("Deseja remover este item?")
                    .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, which ->
                        deletarItemHistorico(position)
                    }).setNegativeButton(android.R.string.no){ dialog, which ->
                        dialog.dismiss()
                    }
                    .create().show()

            }
            // false : close the menu; true : not close the menu
            false
        }


    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }


    private fun irParaResultado(bundle : Bundle){
        var intent  = Intent(this, ResultadoQRCode::class.java)
        intent.putExtra("qrCodeImg", bundle)
        startActivity(intent)
    }

    fun deletarItemHistorico(position: Int){
        var array = carregarHistoricoGerado(this)
        var item = array.get(position)
        array.remove(item)
        val sharedPreferences = this.getSharedPreferences("historicoGerado", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(array)
        editor.putString("listaHistoricoGerado", json)
        editor.apply()
        carregaHistoricoECriaLista()
    }

    fun carregaHistoricoECriaLista (){
        arrayHistoricoGerado = HistoricoGerado.carregarHistoricoGerado(this)
        //if(!(arrayHistoricoLeitura.isNullOrEmpty())){
        listGerado!!.adapter = aplication.ogawadev.com.qrcodegenerate.adapter.HistoricoLeituraAdapter(
            this,
            arrayHistoricoGerado.toList()
        )
        //}
    }

    fun limparHistoricoLeitura(){
        var arrayHistorico: ArrayList<QRCodeHistoricoLeitura>? = null
        val sharedPreferences = this.getSharedPreferences("historicoGerado", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        carregaHistoricoECriaLista()
        Toast.makeText(this, "O histórico foi limpo!", Toast.LENGTH_SHORT).show()
    }


    companion object{

        fun gravarItemNoHistoricoGerado(context: Context, valorQrcode: String, imagemModelo: ImagensCustomizadas){
            var dataFormato = SimpleDateFormat("dd/MM/yyyy-HH:mm")
            var cal = Calendar.getInstance()
            var data = cal.time
            var dataString = dataFormato.format(data)
            var arrayHistorico = carregarHistoricoGerado(context)
            val sharedPreferences = context.getSharedPreferences("historicoGerado", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val qrCodeHistorico = QRCodeHistoricoLeitura(valorQrcode, imagemModelo, dataString)
            arrayHistorico.add(qrCodeHistorico)
            val json = gson.toJson(arrayHistorico)
            editor.putString("listaHistoricoGerado", json)
            editor.apply()

        }
        fun carregarHistoricoGerado (context: Context): ArrayList<QRCodeHistoricoLeitura> {
            var sharedPreferences = context.getSharedPreferences("historicoGerado", Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString("listaHistoricoGerado", null)
            val type = object: TypeToken<ArrayList<QRCodeHistoricoLeitura>>(){}.type
            var array = ArrayList<QRCodeHistoricoLeitura>()
            if(json != null){
                array = gson.fromJson(json, type)
            }

            return array
        }
    }
}
