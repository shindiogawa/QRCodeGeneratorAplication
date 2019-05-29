package aplication.ogawadev.com.qrcodegenerate.model

import android.Manifest
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_gerar_qrcode_mensagem.*
import android.provider.ContactsContract
import android.content.Intent
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeCreator


class GerarQRCodeMensagem : AppCompatActivity() {

    val READ_CONTACTS_RESULT_CODE = 123
    private var telefoneEnviado = ""
    private var textoEnviado = ""
    lateinit var contactUri: Uri
    private var qrCodeCreator = QRCodeCreator()
    private var permissoes = arrayOf(Manifest.permission.READ_CONTACTS)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_gerar_qrcode_mensagem, null)
        setContentView(view)
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })
        //ActivityCompat.requestPermissions(this, permissoes, READ_CONTACTS_RESULT_CODE)
        var resultadoQRCode = intent.getStringExtra("qrcode")

        if(!(resultadoQRCode.isNullOrEmpty())){
            btnContatos.visibility = View.INVISIBLE
            linearMensagem2.visibility = View.INVISIBLE
            btnGerarMensagem.visibility = View.INVISIBLE
            var novaString = resultadoQRCode.replace("SMSTO:","")
            var qrCodeQuebrado = novaString.split(":")

            Log.d("qrCodeQuebrado", "qrCodeQuebrado = ${qrCodeQuebrado}")

            for(cont in 0.. qrCodeQuebrado.size-1){
                if(!qrCodeQuebrado[cont].equals("")){
                    if(cont%2==0){
                        telefoneEnviado = qrCodeQuebrado[cont]
                    }else{
                        textoEnviado = qrCodeQuebrado[cont]
                    }
                }
            }

            txtMensagemPara.setText(telefoneEnviado)
            edtMensagem.setText(textoEnviado)

            btnEnviarMensagem.visibility = View.VISIBLE

            btnEnviarMensagem.setOnClickListener {
               var intent = Intent(Intent.ACTION_SENDTO)
                intent.setData(Uri.parse("smsto:${telefoneEnviado}"))
                intent.putExtra("sms_body", textoEnviado)
                if(intent.resolveActivity(packageManager) != null){
                    startActivity(intent)
                }
            }
        }

        btnVoltarMensagem.setOnClickListener {
            onBackPressed()
        }

        txtMensagemPara.addTextChangedListener(
            aplication.ogawadev.com.qrcodegenerate.business.MaskEditUtil.mask(
                txtMensagemPara,
                aplication.ogawadev.com.qrcodegenerate.business.MaskEditUtil.FORMAT_FONE
            )
        );

        btnContatos.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, permissoes[0]) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissoes[0])) {
                    AlertDialog.Builder(this).setTitle("Alerta").setMessage("Para poder importar o contato o aplicativo" +
                            " precisa ter acesso aos contatos. Deseja permitir o acesso?").setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this,
                            permissoes,
                            READ_CONTACTS_RESULT_CODE)
                    }).setNegativeButton(android.R.string.no){ dialog, which ->
                        dialog.dismiss()
                    }
                        .create().show()

                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        permissoes,
                        READ_CONTACTS_RESULT_CODE)
                }
            }
            else{
                val intent = Intent(Intent.ACTION_PICK)
                intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                if(intent.resolveActivity(packageManager) != null){
                    startActivityForResult(intent, 1)
                }
            }
        }

       btnGerarMensagem.setOnClickListener {
           var numContato = txtMensagemPara.text.toString()
           var mensagem =  edtMensagem.text.toString()
           var mensagemFormatada = formatarConteudoMensagem(numContato, mensagem)
           if(mensagemFormatada.isNullOrBlank()){
               Toast.makeText(this, "É necessário preencher os campos", Toast.LENGTH_SHORT).show()
           }else{
               if(checkSenhaMensagem.isChecked){
                   try{
                       createAlertDialog(mensagemFormatada)
                   } catch (e: Exception){
                       e.printStackTrace()
                   }

               }
               else{
                   var bundle = qrCodeCreator.createQRCode(mensagemFormatada)
                   if(bundle == null){
                       Toast.makeText(this,"Não foi possível criar o QR Code, tente novamente", Toast.LENGTH_SHORT).show()
                   }else{
                       var imagemTitulo = ImagensCustomizadas("Mensagem", R.drawable.mensagem)
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
                    val imagemTitulo = ImagensCustomizadas("Mensagem", R.drawable.mensagem)
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
    private fun formatarConteudoMensagem(numContato : String , mensagem : String ) : String{
        var formatedMessage = ""
        if(!(numContato.isNullOrBlank() || numContato.isNullOrEmpty())
            && !(mensagem.isNullOrBlank() || mensagem.isNullOrEmpty())
        ){
            formatedMessage = qrCodeCreator.createSMSContent(numContato, mensagem)
            return formatedMessage
        }else if(!(numContato.isNullOrBlank() || numContato.isNullOrEmpty())){
            formatedMessage = qrCodeCreator.createSMSContent(numContato)
            return formatedMessage
        }
        return formatedMessage
    }
    public override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        if(reqCode == 1 && resultCode == Activity.RESULT_OK){
            contactUri = data!!.data
            getPhoneNumber()
        }
    }

    private fun getPhoneNumber(){

        val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val cursor = contentResolver.query(contactUri, projection, null, null, null)

        if(cursor!!.moveToFirst()){
            var telefone = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            if(telefone.get(0).equals('+')){
                telefone = telefone.substring(4, telefone.length)
            }
            txtMensagemPara.text = Editable.Factory.getInstance().newEditable(telefone)

        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        for (permissaoResultado in grantResults){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao()
            }else if( permissaoResultado == PackageManager.PERMISSION_GRANTED){

                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
                    val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                    //intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                    if(intent.resolveActivity(packageManager) != null){
                        startActivityForResult(intent, 1)
                    }
                }
            }
        }
    }

    private fun alertaValidacaoPermissao(){
        val builder = android.support.v7.app.AlertDialog.Builder(this)
        builder.setTitle("Permissoões Negadas")
        builder.setMessage("Para ultizar importar contatos é necessário aceitar as permissões")
        builder.setCancelable(false)
        builder.setPositiveButton("Confirmar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {}
        })

        val dialog = builder.create()
        dialog.show()
    }
}
