package aplication.ogawadev.com.qrcodegenerate.model

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.Editable
import android.view.View
import android.widget.EditText
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeCreator
import kotlinx.android.synthetic.main.activity_gerar_qrcode_telefone.*

class GerarQRCodeTelefone : AppCompatActivity() {
    private var telefoneEnvio = ""
    lateinit var contactUri: Uri
    private var contactID: String? = null
    val READ_CONTACTS_RESULT_CODE = 123
    private var qrCodeCreator = QRCodeCreator()
    private var permissoes = arrayOf(Manifest.permission.READ_CONTACTS)
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // ActivityCompat.requestPermissions(this, permissoes, READ_CONTACTS_RESULT_CODE)
        var view = layoutInflater.inflate(R.layout.activity_gerar_qrcode_telefone, null)
        setContentView(view)
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })

        var resultadoQRCode = intent.getStringExtra("qrcode")
        if(!(resultadoQRCode.isNullOrEmpty())){
            btnImportarTelefone.visibility = View.INVISIBLE
            txtImportarContato.visibility = View.INVISIBLE
            linearTelefone3.visibility = View.INVISIBLE
            btnGerarTelefone.visibility = View.INVISIBLE


            telefoneEnvio = resultadoQRCode.replace(QRCodeCreator.TELEPHONE_NUMBER,"")
            edtTelefone.setText(telefoneEnvio)
            btnEnviarTelefone.visibility = View.VISIBLE

            btnEnviarTelefone.setOnClickListener {
                var intent =  Intent(Intent.ACTION_DIAL)
                intent.setData(Uri.parse("tel:${telefoneEnvio}"));
                if(intent.resolveActivity(packageManager) != null){
                    startActivity(intent)
                }
            }
        }

        btnVoltarTelefone.setOnClickListener {
            onBackPressed()
        }

        edtTelefone.addTextChangedListener(
            aplication.ogawadev.com.qrcodegenerate.business.MaskEditUtil.mask(
                edtTelefone,
                aplication.ogawadev.com.qrcodegenerate.business.MaskEditUtil.FORMAT_FONE
            )
        );

        btnImportarTelefone.setOnClickListener {
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

        btnGerarTelefone.setOnClickListener {
            var numTelefone = edtTelefone.text.toString()
            var mensagemFormatada = formatarConteudoTelefone(numTelefone)
            if(mensagemFormatada.isNullOrBlank()){
                Toast.makeText(this, "É necessário preencher os campos", Toast.LENGTH_SHORT).show()
            }else{
                if(checkSenhaTelefone.isChecked){
                    createAlertDialog(mensagemFormatada)
                }
                else{
                    var bundle = qrCodeCreator.createQRCode(mensagemFormatada)
                    if(bundle == null){
                        Toast.makeText(this,"Não foi possível criar o QR Code, tente novamente", Toast.LENGTH_SHORT).show()
                    }else{
                        var imagemTitulo = ImagensCustomizadas("Telefone", R.drawable.telefone)
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
                    val imagemTitulo = ImagensCustomizadas("Telefone", R.drawable.telefone)
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

    private fun formatarConteudoTelefone(numTelefone : String) : String{
        var formatedMessage = ""

        if(!(numTelefone.isNullOrEmpty() || numTelefone.isNullOrBlank())){
            formatedMessage = qrCodeCreator.createTelContent(numTelefone)
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
        edtTelefone.text = Editable.Factory.getInstance().newEditable(contactNumber)
        cursorPhone!!.close()
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
