package aplication.ogawadev.com.qrcodegenerate.model

import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.widget.*
import com.baoyz.swipemenulistview.SwipeMenuCreator
import com.baoyz.swipemenulistview.SwipeMenuItem
import com.baoyz.swipemenulistview.SwipeMenuListView
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeHistoricoLeitura
import aplication.ogawadev.com.qrcodegenerate.fragment.LeitorFragment
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_historico_leitura.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class HistoricoLeitura : AppCompatActivity() {

    var arrayHistoricoLeitura = ArrayList<QRCodeHistoricoLeitura>()
    var listLeitura: SwipeMenuListView? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_historico_leitura, null)
        setContentView(view)
        listLeitura = findViewById(R.id.listHistoricoLeitura)
        carregaHistoricoECriaLista()

        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })

        var btnDeleteHistoricoLeitura = findViewById<Button>(R.id.btnExcluirHistoricoLeitura)
        btnDeleteHistoricoLeitura.setOnClickListener {
            btnDeleteHistoricoLeitura.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
            Timer().schedule(500){
                btnDeleteHistoricoLeitura.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.white))
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

        btnVoltarHistorico.setOnClickListener {
            onBackPressed()
        }

        listLeitura?.setOnItemClickListener { parent, view, position, id ->
            var item = arrayHistoricoLeitura.get(position)
            if(item.conteudoQrCode.contains("Crip&")){
                createAlertDialog(item.conteudoQrCode)
            }
            else{
                var intent = LeitorFragment.abreActivityModelo(item.conteudoQrCode, this, 1, false)
                startActivity(intent)
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

        listLeitura?.setMenuCreator(creator)

        listLeitura!!.setOnMenuItemClickListener { position, menu, index ->
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

    fun createAlertDialog (resultado: String){
        var view = layoutInflater.inflate(R.layout.alert_dialog_senha_layout, null)
        var senha = view.findViewById<EditText>(R.id.edtSenha)
        var resultadoDescriptografado = ""
        var alert = android.app.AlertDialog.Builder(this)
        alert.setView(view).setTitle("Atenção!").setMessage("Este QRCode está protegido por senha. Informe para ver seu conteúdo:")
            .setPositiveButton("Ok"){dialog, which ->
                if(!(senha.text.toString().isNullOrEmpty())) {
                    try{
                        var resultadoDescrip = resultado.replace("Crip&","")
                        resultadoDescriptografado = aplication.ogawadev.com.qrcodegenerate.business.Criptografia.descriptografar(resultadoDescrip, senha.text.toString())
                    } catch (e: Exception){
                        e.printStackTrace()
                    }
                    if(!(resultadoDescriptografado.isNullOrEmpty())){
                        var intent = LeitorFragment.abreActivityModelo(resultadoDescriptografado, this, 0, true)
                        startActivity(intent)
                    }
                    else{
                        Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show()
                        createAlertDialog(resultado)
                    }

                }
                else{
                    Toast.makeText(this, "A senha deve ser informada.",Toast.LENGTH_SHORT).show()
                    createAlertDialog(resultado)
                }

            }.setNegativeButton("Cancel"){dialog, which ->
                dialog.dismiss()
            }.create().show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun deletarItemHistorico(position: Int){
        var array = carregarHistoricoLeitura(this)
        var item = array.get(position)
        array.remove(item)
        val sharedPreferences = this.getSharedPreferences("historicoLeitura", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(array)
        editor.putString("listaHistoricoLeitura", json)
        editor.apply()
        carregaHistoricoECriaLista()
    }

    fun carregaHistoricoECriaLista (){
        arrayHistoricoLeitura = carregarHistoricoLeitura(this)
        //if(!(arrayHistoricoLeitura.isNullOrEmpty())){
            listLeitura!!.adapter =
                aplication.ogawadev.com.qrcodegenerate.adapter.HistoricoLeituraAdapter(
                    this,
                    arrayHistoricoLeitura.toList()
                )
        //}
    }

    fun limparHistoricoLeitura(){
        var arrayHistorico: ArrayList<QRCodeHistoricoLeitura>? = null
        val sharedPreferences = this.getSharedPreferences("historicoLeitura", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear()
        editor.apply()
        carregaHistoricoECriaLista()
        Toast.makeText(this, "O histórico foi limpo!", Toast.LENGTH_SHORT).show()
    }





    companion object{

        fun gravarItemNoHistoricoLeitura(context: Context, valorQrcode:String, imagemModelo: ImagensCustomizadas, dataLeitura: String){
            var arrayHistorico = carregarHistoricoLeitura(context)
            val sharedPreferences = context.getSharedPreferences("historicoLeitura", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            val qrCodeHistorico = QRCodeHistoricoLeitura(valorQrcode, imagemModelo, dataLeitura)
            arrayHistorico.add(qrCodeHistorico)
            val json = gson.toJson(arrayHistorico)
            editor.putString("listaHistoricoLeitura", json)
            editor.apply()

        }
        fun carregarHistoricoLeitura (context: Context): ArrayList<QRCodeHistoricoLeitura> {
            var sharedPreferences = context.getSharedPreferences("historicoLeitura", Context.MODE_PRIVATE)
            val gson = Gson()
            val json = sharedPreferences.getString("listaHistoricoLeitura", null)
            val type = object: TypeToken<ArrayList<QRCodeHistoricoLeitura>>(){}.type
            var array = ArrayList<QRCodeHistoricoLeitura>()
            if(json != null){
                array = gson.fromJson(json, type)
            }

            return array
        }
    }

}


