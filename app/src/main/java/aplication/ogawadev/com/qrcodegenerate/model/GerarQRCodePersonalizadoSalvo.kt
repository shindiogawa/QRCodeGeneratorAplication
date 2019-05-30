package aplication.ogawadev.com.qrcodegenerate.model

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeCreator
import kotlinx.android.synthetic.main.activity_gerar_qrcode_personalizado_salvo.*
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule

class GerarQRCodePersonalizadoSalvo : AppCompatActivity() {
    lateinit var quebraModelo : List<String>
    private var count = 0
    var idsEditTexts = ArrayList<Int>()
    var idsTextViews = ArrayList<Int>()
    private var qrCodeCreator = QRCodeCreator()
    private var isLeitura = false
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_gerar_qrcode_personalizado_salvo, null)
        setContentView(view)
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })

        var resultadoQRCode = intent.getStringExtra("qrcode")

        if(!(resultadoQRCode.isNullOrEmpty())) {
            isLeitura = true
            btnExcluirModeloPersonalizado.visibility = View.INVISIBLE
            linearPersonalizado2.visibility = View.INVISIBLE
            btnGerarPersonalizadoSalvo.visibility = View.INVISIBLE
            var qrCodeQuebrado = resultadoQRCode.split("&")

            var tituloPersonalizadoEnviado = qrCodeQuebrado[0]

            var novaStringUm = StringBuilder()
            for(cont in 1.. qrCodeQuebrado.size-1){
                novaStringUm.append(qrCodeQuebrado[cont])
            }
            var qrCodeQuebradoDois = novaStringUm.split(";")

            var novaStringDois = StringBuilder()
            for (conteudo in qrCodeQuebradoDois) {
                novaStringDois.append(",${conteudo} ")
            }
            var novaStringTres = novaStringDois.toString().replace(":",",")
            var qrCodeQuebradoTres = novaStringTres.split(",")

            var conteudos = ArrayList<String>()
            var titulos = ArrayList<String>()
            for(cont in 0.. qrCodeQuebradoTres.size -1 ){
                if(!qrCodeQuebradoTres[cont].equals("")) {
                    if (cont % 2 == 0) {
                        conteudos.add(qrCodeQuebradoTres[cont])
                    } else {
                        titulos.add(qrCodeQuebradoTres[cont])
                    }
                }
            }

            for(contador in 0.. conteudos.size-1){
                createEditText(titulos[contador], conteudos[contador])
            }
            txtTituloPersonalizado.setText(tituloPersonalizadoEnviado)
        }

        if(!isLeitura) {
            var modelo = intent.getStringExtra("modelo")
            quebraModelo = modelo.split("&")
            txtTituloPersonalizado.text = quebraModelo.get(0)
            criaCampos(quebraModelo.get(1))
        }
        btnVoltarPersonalizadoSalvo.setOnClickListener {
            onBackPressed()
        }
        btnExcluirModeloPersonalizado.setOnClickListener {
            btnExcluirModeloPersonalizado.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
            Handler().postDelayed({
                btnExcluirModeloPersonalizado.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.black))
            },500)

            AlertDialog.Builder(this).setTitle("Alerta").setMessage("Deseja realmente excluir o modelo "+quebraModelo.get(0)+"?")
                .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                    val intent = Intent()
                    intent.putExtra("retorno", true)
                    setResult(Activity.RESULT_OK, intent)
                    Toast.makeText(this, "Modelo excluído!", Toast.LENGTH_SHORT).show()
                    finish()
                }).setNegativeButton(android.R.string.cancel){
                        dialog, which -> dialog.dismiss()
                }.create().show()

        }

        btnGerarPersonalizadoSalvo.setOnClickListener {
            var edtTxt: EditText
            var str = quebraModelo.get(0)+"&"
            var listaTitulos = ArrayList<String>()
            var listaTextos = ArrayList<String>()
            for (i in 0..idsEditTexts.size-1){
                edtTxt = findViewById(idsEditTexts.get(i)) as EditText
                listaTitulos.add(edtTxt.hint.toString())
                listaTextos.add(edtTxt.text.toString())
                //str = str + edtTxt.hint + ":"+ edtTxt.text.toString() + ";"

            }
            var mensagemFormatada = formatarConteudoCustomizado(txtTituloPersonalizado.text.toString(),listaTitulos, listaTextos)

            if(mensagemFormatada.isNullOrBlank()){
                Toast.makeText(this, "É necessário preencher os campos vazios", Toast.LENGTH_SHORT).show()
            }else{
                if(checkSenhaPersonalizadoSalvo.isChecked){
                    createAlertDialog(mensagemFormatada)
                }
                else{
                    var bundle = qrCodeCreator.createQRCode(mensagemFormatada)
                    if(bundle == null){
                        Toast.makeText(this,"Não foi possível criar o QR Code, tente novamente", Toast.LENGTH_SHORT).show()
                    }else{
                        var modelo = mensagemFormatada.split("&")
                        var imagemTitulo = ImagensCustomizadas(modelo.get(0), R.drawable.personalizado)
                        HistoricoGerado.gravarItemNoHistoricoGerado(this,mensagemFormatada, imagemTitulo)
                        irParaResultado(bundle)
                    }
                }

            }

        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun createAlertDialog(mensagemFormatada: String){
        var view = layoutInflater.inflate(R.layout.alert_dialog_senha_layout, null)
        var senha = view.findViewById<EditText>(R.id.edtSenha)
        AlertDialog.Builder(this).setView(view).setTitle("Senha").setMessage("Informe a senha: ")
            .setPositiveButton("Ok"){dialog, which ->
                if(!(senha.text.toString().isNullOrEmpty())){
                    var mensagemCriptografada = "Crip&"+ aplication.ogawadev.com.qrcodegenerate.business.Criptografia.criptografar(mensagemFormatada, senha.text.toString())
                    val nomeConteudoQrCode = mensagemFormatada.split("&")
                    val imagemTitulo = ImagensCustomizadas(nomeConteudoQrCode.get(0), R.drawable.personalizado)
                    HistoricoGerado.gravarItemNoHistoricoGerado(this,mensagemCriptografada, imagemTitulo)
                    var bundle = qrCodeCreator.createQRCode(mensagemCriptografada)
                    if(bundle == null){
                        Toast.makeText(this,"Não foi possível criar o QR Code, tente novamente", Toast.LENGTH_SHORT).show()
                    }else{
                        irParaResultado(bundle)
                    }
                }
                else{
                    Toast.makeText(this, "A senha deve ser informada.", Toast.LENGTH_SHORT).show()
                    createAlertDialog(mensagemFormatada)
                }


            }.setNegativeButton("Cancel"){dialog, which ->
                dialog.dismiss()
            }.create().show()
    }

    private fun irParaResultado(bundle : Bundle){
        var intent  = Intent(this, ResultadoQRCode::class.java)
        intent.putExtra("qrCodeImg", bundle)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun formatarConteudoCustomizado(tituloPersonalizado : String , titulos : ArrayList<String>, textos : ArrayList<String>) : String{
        var formatedMessage = ""
        var contador = 0
        var isDadosPreenchidos =  false
        while(contador <= titulos.size -1) {
            if (!(titulos[contador].isNullOrEmpty() || titulos[contador].isNullOrBlank())
                && !(textos[contador].isNullOrEmpty() || textos[contador].isNullOrBlank())
            ){
                isDadosPreenchidos = true
            }else{
                isDadosPreenchidos = false
            }
            contador++
        }
        if(isDadosPreenchidos){
            formatedMessage = qrCodeCreator.createCustomContent(tituloPersonalizado,titulos, textos)
            return formatedMessage
        }
        return formatedMessage
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun criaCampos(stringCampos: String){
        if(!isLeitura) {
            var quebraCampos = stringCampos.split(";")

            for (i in 0..quebraCampos.size - 2) {
                var editText = EditText(this)
                editText.id = View.generateViewId()
                editText.hint = quebraCampos.get(i)
                editText.background = getDrawable(R.drawable.main_bk_element)
                var params = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                )
                if (!(idsEditTexts.isNullOrEmpty()))
                    params.addRule(RelativeLayout.BELOW, idsEditTexts.get(i - 1))
                editText.layoutParams = params
                idsEditTexts.add(editText.id)
                relativeLayoutPersonalizadoSalvo.addView(editText)
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun createEditText (descricao:String, conteudo: String){
        var textView = TextView(this)
        textView.id = View.generateViewId()//R.id.textView
        textView.textSize = 20f
        textView.text = descricao
        if(count > 0){
            var paramsTxt = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
            paramsTxt.addRule(RelativeLayout.BELOW, idsEditTexts.get(count-1))
            textView.layoutParams = paramsTxt
        }

        relativeLayoutPersonalizadoSalvo.addView(textView)
        var editText = EditText(this)
        editText.id = View.generateViewId()
        editText.background = getDrawable(R.drawable.main_bk_element)
        editText.text = Editable.Factory.getInstance().newEditable(conteudo)
        var params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.BELOW, textView.id)
        editText.layoutParams = params
        relativeLayoutPersonalizadoSalvo.addView(editText)

        idsTextViews.add(textView.id)
        idsEditTexts.add(editText.id)
        count ++

    }
}
