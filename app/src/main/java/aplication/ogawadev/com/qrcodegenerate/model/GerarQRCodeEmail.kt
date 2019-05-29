package aplication.ogawadev.com.qrcodegenerate.model

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeCreator
import kotlinx.android.synthetic.main.activity_gerar_qrcode_email.*

class GerarQRCodeEmail : AppCompatActivity() {
    private var qrCodeCreator = QRCodeCreator()
    private var emailEnvio = ""
    private var assuntoEnvio = ""
    private  var mensagemEnvio = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_gerar_qrcode_email, null)
        setContentView(view)
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })


        var resultadoQRCode = intent.getStringExtra("qrcode")
        if(!(resultadoQRCode.isNullOrEmpty())){
            linearEmail2.visibility = View.INVISIBLE
            btnGerarEmail.visibility = View.INVISIBLE
            var qrCodeQuebrado = resultadoQRCode.split(";")

            for(conteudo in qrCodeQuebrado){
                if(conteudo.startsWith(QRCodeCreator.E_MAIL)){
                    emailEnvio = conteudo.replace(QRCodeCreator.E_MAIL,"")
                }else if(conteudo.startsWith(QRCodeCreator.E_MAIL_2)){
                    emailEnvio = conteudo.replace(QRCodeCreator.E_MAIL_2,"")
                } else if(conteudo.startsWith(QRCodeCreator.E_MAIL_SUBJECT)){
                    assuntoEnvio =  conteudo.replace(QRCodeCreator.E_MAIL_SUBJECT,"")
                }else if(conteudo.startsWith(QRCodeCreator.E_MAIL_BODY)){
                    mensagemEnvio = conteudo.replace(QRCodeCreator.E_MAIL_BODY,"")
                }
            }

            edtEmailPara.setText(emailEnvio)
            edtEmailAssunto.setText(assuntoEnvio)
            edtEmailMensagem.setText(mensagemEnvio)

            btnEnviarEmail.visibility = View.VISIBLE

            btnEnviarEmail.setOnClickListener {
                var intent = Intent(Intent.ACTION_SENDTO)
                intent.setData( Uri.parse("mailto:"))
                intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailEnvio))
                    .putExtra(Intent.EXTRA_SUBJECT, assuntoEnvio)
                    .putExtra(Intent.EXTRA_TEXT, mensagemEnvio)
                if(intent.resolveActivity(packageManager) != null){
                    startActivity(Intent.createChooser(intent,"Enviar E-mail"))
                }

            }
        }

        btnVoltarEmail.setOnClickListener {
            onBackPressed()
        }

        btnGerarEmail.setOnClickListener{
            var emailPara = edtEmailPara.text.toString()
            var assunto = edtEmailAssunto.text.toString()
            var mensagem = edtEmailMensagem.text.toString()
            var mensagemFormatada = formatarConteudoEmail(emailPara, assunto, mensagem)
            if(mensagemFormatada.isNullOrBlank()){
                Toast.makeText(this, "É necessário preencher os campos", Toast.LENGTH_SHORT).show()
            }else{
                if(checkSenhaEmail.isChecked){
                    createAlertDialog(mensagemFormatada)
                }
                else{
                    var bundle = qrCodeCreator.createQRCode(mensagemFormatada)
                    if(bundle == null){
                        Toast.makeText(this,"Não foi possível criar o QR Code, tente novamente", Toast.LENGTH_SHORT).show()
                    }else{
                        var imagemTitulo = ImagensCustomizadas("Email", R.drawable.email)
                        HistoricoGerado.gravarItemNoHistoricoGerado(this,mensagemFormatada, imagemTitulo)
                        irParaResultado(bundle)
                    }
                }

            }
        }
    }

    fun createAlertDialog(mensagemFormatada: String){
        var view = layoutInflater.inflate(R.layout.alert_dialog_senha_layout, null)
        var senha = view.findViewById<EditText>(R.id.edtSenha)
        AlertDialog.Builder(this).setView(view).setTitle("Senha").setMessage("Informe a senha: ")
            .setPositiveButton("Ok"){dialog, which ->
                if(!(senha.text.toString().isNullOrEmpty())){
                    var mensagemCriptografada = "Crip&"+ aplication.ogawadev.com.qrcodegenerate.business.Criptografia.criptografar(mensagemFormatada, senha.text.toString())
                    val imagemTitulo = ImagensCustomizadas("Email", R.drawable.email)
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

    fun irParaResultado(bundle : Bundle){
        var intent  = Intent(this, ResultadoQRCode::class.java)
        intent.putExtra("qrCodeImg", bundle)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    private fun formatarConteudoEmail(mailTo : String?, subject : String?, message : String? ) : String{
        var formatedMessage = ""
        if((!mailTo.isNullOrBlank() || !mailTo.isNullOrEmpty())
            && (!subject.isNullOrBlank() || !subject.isNullOrEmpty())
            && (!message.isNullOrBlank() || !message.isNullOrEmpty())
        ){
            formatedMessage =  qrCodeCreator.createEmailContent(mailTo, subject, message)
            return formatedMessage
        }else if((!mailTo.isNullOrBlank() || !mailTo.isNullOrEmpty())
            && (!subject.isNullOrBlank() || !subject.isNullOrEmpty())){

            formatedMessage =  qrCodeCreator.createEmailContent(mailTo, subject)
            return formatedMessage
        }else if((!mailTo.isNullOrBlank() || !mailTo.isNullOrEmpty())){
            formatedMessage =  qrCodeCreator.createEmailContent(mailTo)
            return formatedMessage
        }
        return formatedMessage
    }

}
