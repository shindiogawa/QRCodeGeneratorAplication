package aplication.ogawadev.com.qrcodegenerate.model

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.annotation.RequiresApi
import android.text.Editable
import kotlinx.android.synthetic.main.activity_gerar_qrcode_contato.*
import android.content.pm.PackageManager
import android.app.AlertDialog
import android.content.DialogInterface
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeCreator


class GerarQRCodeContato : AppCompatActivity() {

    lateinit var contactUri: Uri
    private var contactID: String? = null
    private var qrCodeCreator = QRCodeCreator()
    private var nomeEnvio = ""
    private var telefoneEnvio = ""
    private var emailEnvio = ""
    val READ_CONTACTS_RESULT_CODE = 123
    private var permissoes = arrayOf(Manifest.permission.READ_CONTACTS)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_gerar_qrcode_contato, null)
        setContentView(view)
       // ActivityCompat.requestPermissions(this, permissoes, READ_CONTACTS_RESULT_CODE)
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })
        var resultadoQRCode = intent.getStringExtra("qrcode")
        if(!(resultadoQRCode.isNullOrEmpty())){
            btnImportarContato.visibility = View.INVISIBLE
            txtImportarContato.visibility = View.INVISIBLE
            linearContato3.visibility = View.INVISIBLE
            btnGerarContato.visibility = View.INVISIBLE


            var qrCodeQuebrado = resultadoQRCode.split(";")
            Log.d("qrCodeQuebrado", "qrCodeQuebrado = ${qrCodeQuebrado}")
            for(conteudo in qrCodeQuebrado){
                if(conteudo.startsWith("${QRCodeCreator.CONTACT}${QRCodeCreator.CONTACT_NAME}")){
                    nomeEnvio =  conteudo.replace("${QRCodeCreator.CONTACT}${QRCodeCreator.CONTACT_NAME}", "")
                }else if(conteudo.startsWith(QRCodeCreator.CONTACT_TEL)){
                    telefoneEnvio = conteudo.replace(QRCodeCreator.CONTACT_TEL, "")
                }else if(conteudo.startsWith(QRCodeCreator.CONTACT_E_MAIL)){
                    emailEnvio = conteudo.replace(QRCodeCreator.CONTACT_E_MAIL, "")
                }
            }
            edtContatoNomeCompleto.setText(nomeEnvio)
            edtContatoTelefone.setText(telefoneEnvio)
            edtContatoEmail.setText(emailEnvio)

            btnEnviarContato.visibility = View.VISIBLE

            btnEnviarContato.setOnClickListener {
                var intent = Intent(ContactsContract.Intents.Insert.ACTION)
                intent.type = ContactsContract.RawContacts.CONTENT_TYPE

                intent
                    .putExtra(ContactsContract.Intents.Insert.NAME, nomeEnvio)
                    .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .putExtra(ContactsContract.Intents.Insert.PHONE, telefoneEnvio)
                    .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                    .putExtra(ContactsContract.Intents.Insert.EMAIL, emailEnvio)
                startActivity(intent)
            }
        }


        btnVoltarContato.setOnClickListener {
            onBackPressed()
        }

        edtContatoTelefone.addTextChangedListener(
            aplication.ogawadev.com.qrcodegenerate.business.MaskEditUtil.mask(
                edtContatoTelefone,
                aplication.ogawadev.com.qrcodegenerate.business.MaskEditUtil.FORMAT_FONE
            )
        );


        btnImportarContato.setOnClickListener {
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
                val intent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
                //intent.type = ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE
                if(intent.resolveActivity(packageManager) != null){
                    startActivityForResult(intent, 1)
                }
            }

        }

        btnGerarContato.setOnClickListener {
            var nomeCompleto = edtContatoNomeCompleto.text.toString()
//            if(!(nomeCompleto.isNullOrEmpty() || nomeCompleto.isNullOrBlank())) {
//                var nomeCompletoSplit = nomeCompleto.split(" ")
//                var segundoNome = nomeCompletoSplit[1]
//                var primeiroNome = nomeCompletoSplit[0]
//                nomeCompleto = "${segundoNome},${primeiroNome}"
//            }
            var numTelefone = edtContatoTelefone.text.toString()
            var email = edtContatoEmail.text.toString()
            var mensagemFormatada = formatarConteudoContato(nomeCompleto, numTelefone, email)


            if(mensagemFormatada.isNullOrBlank()){
                Toast.makeText(this, "É necessário preencher os campos", Toast.LENGTH_SHORT).show()
            }else{
                if(checkSenhaContato.isChecked){
                    createAlertDialog(mensagemFormatada)
                }
                else{
                    var bundle = qrCodeCreator.createQRCode(mensagemFormatada)
                    if(bundle == null){
                        Toast.makeText(this,"Não foi possível criar o QR Code, tente novamente", Toast.LENGTH_SHORT).show()
                    }else{
                        var imagemTitulo = ImagensCustomizadas("Contato", R.drawable.contato)
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
                    val imagemTitulo = ImagensCustomizadas("Contato", R.drawable.contato)
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

    private fun formatarConteudoContato(nomeCompleto : String, numTelefone : String, email : String) : String{
        var formatedMessage = ""

        if(!(nomeCompleto.isNullOrEmpty() || nomeCompleto.isNullOrBlank())
            && !(numTelefone.isNullOrEmpty() || numTelefone.isNullOrBlank())
            && !(email.isNullOrEmpty() || email.isNullOrBlank())){
                formatedMessage =  qrCodeCreator.createContactContent(nomeCompleto, numTelefone, email)
                return formatedMessage
        }else if(!(nomeCompleto.isNullOrEmpty() || nomeCompleto.isNullOrBlank())
            && !(numTelefone.isNullOrEmpty() || numTelefone.isNullOrBlank())){
            formatedMessage =  qrCodeCreator.createContactContent(nomeCompleto, numTelefone)
            return formatedMessage
        }else if(!(nomeCompleto.isNullOrEmpty() || nomeCompleto.isNullOrBlank())){
            formatedMessage = qrCodeCreator.createContactContent(nomeCompleto)
            return formatedMessage
        }
        return formatedMessage
    }

    public override fun onActivityResult(reqCode: Int, resultCode: Int, data: Intent?) {
        if(reqCode == 1 && resultCode == Activity.RESULT_OK){
            contactUri = data!!.data
            getPhone()
            getName()
        }
    }

    private fun getPhone(){

        var contactNumber:String? = null

        val cursorID = contentResolver.query(
            contactUri,
            arrayOf(ContactsContract.Contacts._ID), null, null, null
        )

        if (cursorID!!.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID))
        }

        cursorID.close()

        // Using the contact ID now we will get contact phone number
        val cursorPhone = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),

            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
            ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

            arrayOf<String>(contactID!!),
            null)

        if (cursorPhone!!.moveToFirst())
        {
            contactNumber = cursorPhone!!.getString(cursorPhone!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
        }

        if(contactNumber!!.get(0).equals('+')){
            contactNumber = contactNumber.substring(4, contactNumber.length)
        }
        edtContatoTelefone.text = Editable.Factory.getInstance().newEditable(contactNumber)
        cursorPhone!!.close()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    fun getName (){
        var contactName: String? = null

        // querying contact data store
        val cursor = contentResolver.query(contactUri, null, null, null, null)

        if (cursor!!.moveToFirst()) {

            // DISPLAY_NAME = The display name for the contact.
            // HAS_PHONE_NUMBER =   An indicator of whether this contact has at least one phone number.

            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
        }

        edtContatoNomeCompleto.text = Editable.Factory.getInstance().newEditable(contactName)

        cursor.close()
    }

    fun getEmail (){
        var contactEmail:String? = null

        val cursorID = contentResolver.query(
            contactUri,
            arrayOf(ContactsContract.Contacts._ID), null, null, null
        )

        if (cursorID!!.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID))
        }

        cursorID.close()

        // Using the contact ID now we will get contact phone number
        val cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Email.ADDRESS),

            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                    ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

            arrayOf<String>(contactID!!),
            null)

        if (cursor!!.moveToFirst())
        {
            contactEmail = cursor!!.getString(cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
        }
        edtContatoEmail.text = Editable.Factory.getInstance().newEditable(contactEmail)
        cursor!!.close()
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
        builder.setMessage("Para importar contatos é necessário aceitar as permissões")
        builder.setCancelable(false)
        builder.setPositiveButton("Confirmar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {}
        })

        val dialog = builder.create()
        dialog.show()
    }
}
