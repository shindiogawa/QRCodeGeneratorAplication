package aplication.ogawadev.com.qrcodegenerate.model

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.view.View
import android.widget.*
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeCreator
import aplication.ogawadev.com.qrcodegenerate.manager.MainActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_gerar_qrcode_personalizado.*
import kotlinx.android.synthetic.main.activity_gerar_qrcode_personalizado.linearPersonalizado2
import kotlinx.android.synthetic.main.activity_gerar_qrcode_personalizado_salvo.*
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule


class GerarQRCodePersonalizado : AppCompatActivity() {

    var count = 0
    var idsEditTexts = ArrayList<Int>()
    var idsTextViews = ArrayList<Int>()
    var arrayModelos = ArrayList<String>()
    var isLeitura = false
    var modeloSalvo = false
    private var qrCodeCreator = QRCodeCreator()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_gerar_qrcode_personalizado, null)
        setContentView(view)
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })

        var resultadoQRCode = intent.getStringExtra("qrcode")
        if(!(resultadoQRCode.isNullOrEmpty())) {
            isLeitura = true
            linear.visibility = View.INVISIBLE
            linearPersonalizado2.visibility = View.INVISIBLE
            btnGerarPersonalizado.visibility = View.INVISIBLE
            var qrCodeQuebrado = resultadoQRCode.split("custom&")
            var stringTempUm = StringBuilder()
            for(conteudo in qrCodeQuebrado){
                if(!(conteudo.isNullOrEmpty() || conteudo.isNullOrBlank())){
                    stringTempUm.append(conteudo)
                }
            }

            var qrCodeQuebradoDois = stringTempUm.split(";")

            var stringTempDois = StringBuilder()


            for(conteudo in qrCodeQuebrado){
                if(!(conteudo.isNullOrEmpty() || conteudo.isNullOrBlank())){
                    stringTempDois.append((conteudo))
                }
            }
            var qrCodeQuebradoTres = stringTempDois.split(":")
            var stringTempTres = StringBuilder()
            for(conteudo in qrCodeQuebradoTres){
                if(!(conteudo.isNullOrEmpty() || conteudo.isNullOrBlank())){
                    stringTempTres.append(",${conteudo}")
                }
            }
            var stringTempQuatro = stringTempTres.toString().replace(";",",")

            var qrCodeQuebradoQuatro = stringTempQuatro.split(",")

            var conteudos = ArrayList<String>()
            var titulos = ArrayList<String>()
            for(cont in 0.. qrCodeQuebradoQuatro.size -1 ){
                if(!qrCodeQuebradoQuatro[cont].equals("")) {
                    if (cont % 2 == 0) {
                        conteudos.add(qrCodeQuebradoQuatro[cont])
                    } else {
                        titulos.add(qrCodeQuebradoQuatro[cont])
                    }
                }
            }

            for(contador in 0.. conteudos.size-1){
                createEditText(titulos[contador], conteudos[contador])
            }

        }

        btnVoltarPersonalizado.setOnClickListener {
            onBackPressed()
        }

        btnSalvarModeloPersonalizado.setOnClickListener {
            btnSalvarModeloPersonalizado.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
            Handler().postDelayed({
                btnSalvarModeloPersonalizado.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.black))
            },500)

            if(idsTextViews.isNullOrEmpty()){
                Toast.makeText(this, "Deve haver pelo menos um campo para salvar um modelo.", Toast.LENGTH_SHORT).show()
            }
            else{
                var view = layoutInflater.inflate(R.layout.alert_dialog_nome_modelo_personalizado, null)
                var edtNomeModelo = view.findViewById<EditText>(R.id.edtNomeModelo)
                AlertDialog.Builder(this)
                    .setTitle("Salvar Modelo")
                    .setMessage("Insira um nome para o modelo")
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                        salvarModeloPersonalizado(edtNomeModelo.text.toString())
                    }).setNegativeButton(android.R.string.cancel){
                            dialog, which -> dialog.dismiss()
                    }.create().show()
            }
        }

        btnAddCampo.setOnClickListener {

            if(idsTextViews.size < 12){
                var view = layoutInflater.inflate(R.layout.alert_dialog_add_personalizado, null)
                var descricao = view.findViewById<EditText>(R.id.descricaoPersonalizado)
                var conteudo = view.findViewById<EditText>(R.id.conteudoPersonalizado)

                AlertDialog.Builder(this)
                    .setTitle("Novo campo")
                    .setMessage("Insira a descrição e o conteúdo do novo campo")
                    .setView(view)
                    .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                        createEditText(descricao.text.toString(), conteudo.text.toString())
                    }).setNegativeButton(android.R.string.cancel){
                            dialog, which -> dialog.dismiss()
                    }.create().show()
            }
            else{
                Toast.makeText(this, "Você atingiu o limite de campos permitidos (12 campos).", Toast.LENGTH_SHORT).show()
            }

        }

        btnGerarPersonalizado.setOnClickListener {
            var edtTxt: EditText
            var txtView: TextView
            var listaTitulos = ArrayList<String>()
            var listaTextos = ArrayList<String>()
            var str = "Personalizado&"
            if(idsTextViews.isNullOrEmpty()){
                Toast.makeText(this,"Deve haver pelo menos um campo para gerar um qrcode.", Toast.LENGTH_SHORT).show()
            }
            else{

                for (i in 0..count-1){
                    edtTxt = findViewById(idsEditTexts.get(i)) as EditText
                    txtView = findViewById(idsTextViews.get(i)) as TextView
                    listaTitulos.add(txtView.text.toString())
                    listaTextos.add(edtTxt.text.toString())
                    //str = str + txtView.text.toString() + ":"+ edtTxt.text.toString() + ";"
                }
                var mensagemFormatada = formatarConteudoCustomizado(listaTitulos, listaTextos)

                if(mensagemFormatada.isNullOrBlank()){
                    Toast.makeText(this, "É necessário preencher os campos vazios", Toast.LENGTH_SHORT).show()
                }else{
                    if(checkSenhaPersonalizado.isChecked){
                        createAlertDialog(mensagemFormatada)
                    }
                    else{
                        var bundle = qrCodeCreator.createQRCode(mensagemFormatada)
                        if(bundle == null){
                            Toast.makeText(this,"Não foi possível criar o QR Code, tente novamente", Toast.LENGTH_SHORT).show()
                        }else{
                            var imagemTitulo = ImagensCustomizadas("Personalizado", R.drawable.personalizado)
                            HistoricoGerado.gravarItemNoHistoricoGerado(this,mensagemFormatada, imagemTitulo)
                            irParaResultado(bundle)
                        }
                    }

                }
               //Toast.makeText(this, str, Toast.LENGTH_LONG).show()
            }

        }

    }

    fun createAlertDialog(mensagemFormatada: String){
        var view = layoutInflater.inflate(R.layout.alert_dialog_senha_layout, null)
        var senha = view.findViewById<EditText>(R.id.edtSenha)
        android.app.AlertDialog.Builder(this).setView(view).setTitle("Senha").setMessage("Informe a senha: ")
            .setPositiveButton("Ok"){dialog, which ->
                if(!(senha.text.toString().isNullOrEmpty())){
                    var mensagemCriptografada = "Crip&"+ aplication.ogawadev.com.qrcodegenerate.business.Criptografia.criptografar(mensagemFormatada, senha.text.toString())
                    val imagemTitulo = ImagensCustomizadas("Personalizado", R.drawable.personalizado)
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

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun createEditText (descricao:String, conteudo: String){
        var textView = TextView(this)
        textView.id = View.generateViewId()//R.id.textView
        textView.textSize = 20f
        textView.text = descricao
        textView.setOnClickListener {
            var view = layoutInflater.inflate(R.layout.layout_nova_descricao_personalizado, null)
            var edtNovaDescricao = view.findViewById<EditText>(R.id.edtNovaDescricao)
            var txt = findViewById<TextView>(textView.id)
            AlertDialog.Builder(this).setView(view).setTitle("Alterar descrição.")
                .setMessage("Informa a nova descrição:")
                .setPositiveButton("Ok"){dialog, which ->
                    if(!(edtNovaDescricao.text.toString().isNullOrEmpty())){
                        txt.text = edtNovaDescricao.text.toString()
                    }
                    else{
                        Toast.makeText(this,"Valor em branco.", Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Cancel"){dialog, which ->
                    dialog.dismiss()
                }.create().show()
        }
        if(count > 0){
            var paramsTxt = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
            paramsTxt.addRule(RelativeLayout.BELOW, idsEditTexts.get(count-1))
            textView.layoutParams = paramsTxt
        }
        relativeLayout.addView(textView)
        var editText = EditText(this)
        editText.id = View.generateViewId()
        editText.background = getDrawable(R.drawable.main_bk_element)
        editText.text = Editable.Factory.getInstance().newEditable(conteudo)
        var params = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.BELOW, textView.id)
        editText.layoutParams = params
        relativeLayout.addView(editText)

        idsTextViews.add(textView.id)
        idsEditTexts.add(editText.id)
        count ++

    }

    fun createAlertDialogNovaDescricao (): String{
        var view = layoutInflater.inflate(R.layout.layout_nova_descricao_personalizado, null)
        var edtNovaDescricao = view.findViewById<EditText>(R.id.edtNovaDescricao)
        var retorno = ""
        AlertDialog.Builder(this).setView(view).setTitle("Alterar descrição.")
            .setMessage("Informa a nova descrição:")
            .setPositiveButton("Ok"){dialog, which ->
                if(!(edtNovaDescricao.text.toString().isNullOrEmpty())){
                    retorno = edtNovaDescricao.text.toString()
                }
                else{
                    Toast.makeText(this,"Valor em branco.", Toast.LENGTH_SHORT).show()
                    retorno = createAlertDialogNovaDescricao()
                }
            }
            .setNegativeButton("Cancel"){dialog, which ->
                dialog.dismiss()
            }.create().show()

        return retorno
    }

    private fun irParaResultado(bundle : Bundle){
        var intent  = Intent(this, ResultadoQRCode::class.java)
        intent.putExtra("qrCodeImg", bundle)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun formatarConteudoCustomizado(titulos : ArrayList<String>, textos : ArrayList<String>) : String{
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
            formatedMessage = qrCodeCreator.createCustomContent(titulos, textos)
            return formatedMessage
        }
        return formatedMessage
    }
    fun salvarModeloPersonalizado (nomeModelo: String){
        if(verificaExistenciaNomeModelo(nomeModelo)){ //Nome ja existe
            Toast.makeText(this, "Já existe um modelo personalizado com este nome!", Toast.LENGTH_LONG).show()
        }
        else{
            val sharedPreferences = getSharedPreferences("modelos", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()
            val gson = Gson()
            var txtView: TextView
            var str = nomeModelo + "&"
            for (i in 0..count-1){
                txtView = findViewById(idsTextViews.get(i)) as TextView
                str = str + txtView.text.toString() + ";"
            }
            arrayModelos.add(str)
            val json = gson.toJson(arrayModelos)
            editor.putString("listaModelos", json)
            editor.apply()
            Toast.makeText(this, "Modelo salvo!", Toast.LENGTH_SHORT).show()
            modeloSalvo = true;
        }
    }

    override fun onBackPressed() {
        if(idsTextViews.size > 0){
            if(!isLeitura && !modeloSalvo){
                AlertDialog.Builder(this).setTitle("Alerta").setMessage("Antes de sair, deseja salvar o modelo?")
                    .setPositiveButton("Sim"){dialog, which ->
                        var view = layoutInflater.inflate(R.layout.alert_dialog_nome_modelo_personalizado, null)
                        var edtNomeModelo = view.findViewById<EditText>(R.id.edtNomeModelo)
                        AlertDialog.Builder(this)
                            .setTitle("Salvar Modelo")
                            .setMessage("Insira um nome para o modelo")
                            .setView(view)
                            .setPositiveButton(android.R.string.ok, DialogInterface.OnClickListener { dialog, which ->
                                salvarModeloPersonalizado(edtNomeModelo.text.toString())
                                var intent = Intent()
                                intent.putExtra("resposta", true)
                                setResult(Activity.RESULT_OK, intent)
                                finish()
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
                            }).setNegativeButton(android.R.string.cancel){
                                    dialog, which -> dialog.dismiss()
                            }.create().show()
                    }
                    .setNegativeButton("Não"){dialog, which ->
                        intentResposta()
                        dialog.dismiss()
                    }.create().show()
            }
            else{
                intentResposta()
            }

        }
        else{
            intentResposta()
        }


    }

    fun intentResposta(){
        var intent = Intent()
        intent.putExtra("resposta", true)
        setResult(Activity.RESULT_OK, intent)
        finish()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun carregarModelos (){
        var sharedPreferences = getSharedPreferences("modelos", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("listaModelos", null)
        val type = object: TypeToken<ArrayList<String>>(){}.type
        if(json != null){
            arrayModelos = gson.fromJson(json, type)
        }

    }

    fun verificaExistenciaNomeModelo(nomeModelo: String): Boolean{ // Retorna true para nome existente e false para não existente
        carregarModelos()
        if(!(arrayModelos.isNullOrEmpty())){
            for (i in 0..arrayModelos.size-1){
                var quebraNome = arrayModelos.get(i).split("&")
                if(quebraNome.get(0).equals(nomeModelo)){
                    return true
                }
            }
        }

        return false
    }
}
