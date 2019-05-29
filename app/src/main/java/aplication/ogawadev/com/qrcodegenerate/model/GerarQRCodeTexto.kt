package aplication.ogawadev.com.qrcodegenerate.model

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeCreator
import kotlinx.android.synthetic.main.activity_gerar_qrcode_texto.*

class GerarQRCodeTexto : AppCompatActivity() {
    private var qrCodeCreator = QRCodeCreator()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_gerar_qrcode_texto, null)
        setContentView(view)
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })

        var resultadoQRCode = intent.getStringExtra("qrcode")

        if(!(resultadoQRCode.isNullOrEmpty())){
            linearTexto2.visibility = View.INVISIBLE
            btnGerarTexto.visibility = View.INVISIBLE
            edtTexto.setText(resultadoQRCode)
            btnEnviarTexto.visibility = View.VISIBLE

            btnEnviarTexto.setOnClickListener {
                var intent = Intent(Intent.ACTION_SEND)
                intent.setType("text/plain")
                intent.putExtra(Intent.EXTRA_TEXT,resultadoQRCode)
                if(intent.resolveActivity(packageManager) != null){
                    startActivity(Intent.createChooser(intent,"Enviar texto para:"))
                }
                /* ALGORITMO IMPRESSORA
                var intent = Intent(Intent.ACTION_VIEW)
                intent.setType("text/plain")
                intent.putExtra(Intent.EXTRA_TEXT, resultadoQRCode)

                if(intent.resolveActivity(packageManager) != null){
                    startActivity(Intent.createChooser(intent,"Enviar texto para:"))
                }
                */
            }

        }
        btnVoltarTexto.setOnClickListener {
            onBackPressed()
        }

        btnGerarTexto.setOnClickListener{
            var msg = edtTexto.text.toString()
            var mensagemFormatada = formatarConteudoTexto(msg)
            if(mensagemFormatada.isNullOrBlank()){
                Toast.makeText(this, "É necessário preencher os campos", Toast.LENGTH_SHORT).show()
            }else{
                if(checkSenhaTexto.isChecked){
                    createAlertDialog(mensagemFormatada)
                }
                else{
                    var bundle = qrCodeCreator.createQRCode(mensagemFormatada)
                    if(bundle == null){
                        Toast.makeText(this,"Não foi possível criar o QR Code, tente novamente", Toast.LENGTH_SHORT).show()
                    }else{
                        var imagemTitulo = ImagensCustomizadas("Texto", R.drawable.texto)
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
                    val imagemTitulo = ImagensCustomizadas("Texto", R.drawable.texto)
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

    private fun formatarConteudoTexto(msg : String) : String {
        var formatedMessage = ""

        if (!(msg.isNullOrEmpty() || msg.isNullOrBlank())) {
            formatedMessage = qrCodeCreator.createTextContent(msg)
            return formatedMessage

        }
        return formatedMessage
    }
}
