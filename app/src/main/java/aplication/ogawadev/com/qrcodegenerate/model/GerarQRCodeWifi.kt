package aplication.ogawadev.com.qrcodegenerate.model

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.EditText
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeCreator
import kotlinx.android.synthetic.main.activity_gerar_qrcode_wifi.*

class GerarQRCodeWifi : AppCompatActivity() {

    private var qrCodeCreator = QRCodeCreator()
    private var textChecked = "nopass"
    private var tipoWifiEnvio = ""
    private var ssidEnvio = ""
    private var senhaEnvio = ""
    private var permissoes = arrayOf(Manifest.permission.ACCESS_WIFI_STATE)
    val TAG:String="MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_gerar_qrcode_wifi, null)
        setContentView(view)
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })

        var resultadoQRCode = intent.getStringExtra("qrcode")
        if(!(resultadoQRCode.isNullOrEmpty())){
            linearWifi3.visibility = View.INVISIBLE
            btnGerarWifi.visibility = View.INVISIBLE
            var qrCodeQuebrado = resultadoQRCode.split(";")


            for(conteudo in qrCodeQuebrado){
                if(conteudo.startsWith("WIFI:T:")){
                    tipoWifiEnvio = conteudo.replace("WIFI:T:","")
                    if(tipoWifiEnvio.equals("WPA")){
                        checkWpaWpa2.isChecked = true
                        checkWep.isChecked = false
                        checkNenhum.isChecked = false
                    }else if(tipoWifiEnvio.equals("WEP")){
                        checkWpaWpa2.isChecked = false
                        checkWep.isChecked = true
                        checkNenhum.isChecked = false
                    }else if(tipoWifiEnvio.equals("nopass")){
                        checkWpaWpa2.isChecked = false
                        checkWep.isChecked = false
                        checkNenhum.isChecked = true
                    }
                }else if(conteudo.startsWith("S:")){
                    ssidEnvio = conteudo.replace("S:","")
                        .replace("\"","\"")
                }else if(conteudo.startsWith("P:")){
                    senhaEnvio = conteudo.replace("P:","")
                }
            }

            edtSenhaWifi.setText(senhaEnvio)
            edtSsidWifi.setText(ssidEnvio)
            btnEnviarWifi.visibility = View.VISIBLE

        }

        btnEnviarWifi.setOnClickListener {
            Toast.makeText(this,"clicou", Toast.LENGTH_SHORT).show()
            var ssid = "\"" + ssidEnvio + "\""
            var senha = "\"" + senhaEnvio + "\""

            var connectionManager =
                aplication.ogawadev.com.qrcodegenerate.business.ConnectionManager(this)
            connectionManager.enableWifi();
            connectionManager.requestWIFIConnection(ssidEnvio,senhaEnvio);
            var intent =  Intent(Settings.ACTION_WIFI_SETTINGS)
            startActivity(intent)

        }

        btnVoltarWifi.setOnClickListener {
            onBackPressed()
        }

        checkWpaWpa2.setOnCheckedChangeListener { buttonView, isChecked ->
            if(checkWpaWpa2.isChecked){
                checkWep.isChecked = false
                checkNenhum.isChecked = false
                textChecked = "WPA"
            }
        }

        checkWep.setOnCheckedChangeListener { buttonView, isChecked ->
            if(checkWep.isChecked){
                checkWpaWpa2.isChecked = false
                checkNenhum.isChecked = false
                textChecked = "WEP"
            }
        }

        checkNenhum.setOnCheckedChangeListener { buttonView, isChecked ->
            if(checkNenhum.isChecked){
                checkWpaWpa2.isChecked = false
                checkWep.isChecked = false
            }
        }

        btnGerarWifi.setOnClickListener {
            var ssid = edtSsidWifi.text.toString()
            var senha = edtSenhaWifi.text.toString()

            var mensagemFormatada = formatarConteudoWifi(ssid, senha, textChecked)
            if(mensagemFormatada.isNullOrBlank()){
                Toast.makeText(this, "É necessário preencher os campos", Toast.LENGTH_SHORT).show()
            }else{
                if(checkSenhaWifi.isChecked){
                    createAlertDialog(mensagemFormatada)
                }
                else{
                    var bundle = qrCodeCreator.createQRCode(mensagemFormatada)
                    if(bundle == null){
                        Toast.makeText(this,"Não foi possível criar o QR Code, tente novamente", Toast.LENGTH_SHORT).show()
                    }else{
                        var imagemTitulo = ImagensCustomizadas("Wifi", R.drawable.wifi)
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
                    val imagemTitulo = ImagensCustomizadas("Wifi", R.drawable.wifi)
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

    private fun formatarConteudoWifi(ssid : String, senha : String, textChecked : String) : String {
        var formatedMessage = ""

        if (!(ssid.isNullOrEmpty() || ssid.isNullOrBlank())
            && senha != null
            && !(textChecked.isNullOrEmpty() || textChecked.isNullOrBlank())
        ){
            var ssidTratada = tratarCaracteresEspeciais(ssid)
           var senhaTratada = tratarCaracteresEspeciais(senha)
            formatedMessage = qrCodeCreator.createWiFiContent(textChecked,ssidTratada, senhaTratada)
            return formatedMessage
        }
        return formatedMessage
    }

    private fun tratarCaracteresEspeciais(senha : String) : String{
        var conteudoTratado =
            senha.replace("\\", "\\\\")
            .replace(";" , "\\;")
            .replace(":","\\:")
            .replace(",", "\\,")
            .replace("\"", "\"")
        return conteudoTratado
    }
}
