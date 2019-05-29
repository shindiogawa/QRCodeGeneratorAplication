package aplication.ogawadev.com.qrcodegenerate.model


import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.print.PrintHelper
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import kotlinx.android.synthetic.main.activity_resultado_qrcode.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule

class ResultadoQRCode : AppCompatActivity() {
    private var isCompartilhar = false
    private val WRITE_READ_EXTERNAL_RESULT_CODE = 100
    private var permissoes = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_resultado_qrcode, null)
        setContentView(view)
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })

       // ActivityCompat.requestPermissions(this, permissoes, WRITE_READ_EXTERNAL_RESULT_CODE)
        var bundle = intent.getBundleExtra("qrCodeImg")
        var byteArray = bundle.getByteArray("byteArrayQrCode")
        var bitMap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        imgQrCode.setImageBitmap(bitMap)

        btnImprimir.setOnClickListener {
            btnImprimir.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
            Timer().schedule(500){
                btnImprimir.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.black))
            }
            var simpleDateFormat = SimpleDateFormat("ddMMyyyy_HHmmss")
            var dataAtual = simpleDateFormat.format(Date())
            var fileName = "${dataAtual}"
            PrintHelper(this).apply {
                scaleMode = PrintHelper.SCALE_MODE_FIT
            }.also {
                printHelper -> printHelper.printBitmap(fileName,bitMap)
            }
        }

        btnSalvarQrCode.setOnClickListener{
            if(ContextCompat.checkSelfPermission(this, permissoes[0]) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissoes[0])) {
                    AlertDialog.Builder(this).setTitle("Alerta").setMessage("Para salvar o QRCode é necessário" +
                            " permitir a escrita no celular. Deseja permitir o acesso?").setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this,
                            permissoes,
                            WRITE_READ_EXTERNAL_RESULT_CODE)

                    }).setNegativeButton(android.R.string.no){ dialog, which ->
                        dialog.dismiss()
                    }
                        .create().show()

                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        permissoes,
                        WRITE_READ_EXTERNAL_RESULT_CODE)
                }
            }
            else{
                saveOrCreateNewDirectory(bitMap, 0)
            }
        }

        btnCompartilharQrCode.setOnClickListener{
            isCompartilhar = true
            if(ContextCompat.checkSelfPermission(this, permissoes[0]) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissoes[0])) {
                    AlertDialog.Builder(this).setTitle("Alerta").setMessage("Para compartilhar o QRCode é necessário" +
                            " permitir a escrita no celular. Deseja permitir o acesso?").setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this,
                            permissoes,
                            WRITE_READ_EXTERNAL_RESULT_CODE)
                    }).setNegativeButton(android.R.string.no){ dialog, which ->
                        dialog.dismiss()
                    }
                        .create().show()

                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        permissoes,
                        WRITE_READ_EXTERNAL_RESULT_CODE)
                }
            }
            else{
                var shareIntent = Intent(Intent.ACTION_SEND)
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                else
                    shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                shareIntent.setType("image/jpeg")

                var bytes = ByteArrayOutputStream()
                bitMap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)


                //var path = MediaStore.Images.Media.insertImage(contentResolver, bitMap, "Qr Code compartilhado", null)
                var fileName = saveOrCreateNewDirectory(bitMap, 1)
                //var path = "${Environment.getExternalStorageDirectory()}/qrCodeGenerator/${fileName}"
                val path = File(
                    Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES
                    ), "qrCodeGenerator/${fileName}"
                )

                var imageUri = Uri.parse(path.toString())

                //var path = "${Environment.getExternalStorageDirectory()}${File.separator}"
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                startActivity(Intent.createChooser(shareIntent,"Compartilhar imagem"))

            }
        }

        btnVoltarResultado.setOnClickListener{
            super.onBackPressed()
        }
    }

    //botaoApertado = 0 -> botao salvar, botaoApertado = 1 -> botao compartilhar
    fun saveOrCreateNewDirectory(bitMap : Bitmap, botaoApertado: Int) : String{
        var values = ContentValues()

        //var path = "${Environment.getExternalStorageDirectory()}/qrCodeGenerator/"
        val path = File(
            Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES
            ), "qrCodeGenerator"
        )
        var dir = File(path.toString())
        if(!dir.exists()){
            dir.mkdir()
        }
        var simpleDateFormat = SimpleDateFormat("ddMMyyyy_HHmmss")
        var dataAtual = simpleDateFormat.format(Date())
        var fileName = "${dataAtual}.jpeg"
        var file = File(dir,fileName)

        if(file.exists()){
            file.delete()
        }

        values.put(MediaStore.Images.Media.DATA, file.absolutePath)
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        var out : FileOutputStream? = null
        try{
            out = FileOutputStream(file)
            bitMap.compress(Bitmap.CompressFormat.JPEG,100, out)
            out.flush()
            out.close()
            if(botaoApertado == 0)
                Toast.makeText(this, "Qr Code salvo.", Toast.LENGTH_SHORT).show()
        }catch(e : IOException){
            e.printStackTrace()
            if(botaoApertado == 0)
                Toast.makeText(this, "Não foi possível salvar o Qr Code, tente novamente!", Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, "Não foi possível compartilhar o Qr Code, tente novamente!", Toast.LENGTH_SHORT).show()
        }

        return fileName
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        for (permissaoResultado in grantResults){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao()
            }else if( permissaoResultado == PackageManager.PERMISSION_GRANTED){

                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                    var bundle = intent.getBundleExtra("qrCodeImg")
                    var byteArray = bundle.getByteArray("byteArrayQrCode")
                    var bitMap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
                    if(isCompartilhar) {
                        var shareIntent = Intent(Intent.ACTION_SEND)
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
                            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
                        else
                            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT)
                        shareIntent.setType("image/jpeg")

                        var bytes = ByteArrayOutputStream()
                        bitMap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)


                        //var path = MediaStore.Images.Media.insertImage(contentResolver, bitMap, "Qr Code compartilhado", null)
                        var fileName = saveOrCreateNewDirectory(bitMap, 1)
                        //var path = "${Environment.getExternalStorageDirectory()}/qrCodeGenerator/${fileName}"
                        val path = File(
                            Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES
                            ), "qrCodeGenerator/${fileName}"
                        )

                        var imageUri = Uri.parse(path.toString())

                        //var path = "${Environment.getExternalStorageDirectory()}${File.separator}"
                        shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
                        startActivity(Intent.createChooser(shareIntent, "Compartilhar imagem"))
                    }else{
                        saveOrCreateNewDirectory(bitMap, 0)
                    }
                }
            }
        }
    }

    private fun alertaValidacaoPermissao(){
        val builder = android.support.v7.app.AlertDialog.Builder(this)
        builder.setTitle("Permissoões Negadas")
        builder.setMessage("Para compartilhar é necessário aceitar as permissões")
        builder.setCancelable(false)
        builder.setPositiveButton("Confirmar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {}
        })

        val dialog = builder.create()
        dialog.show()
    }
}
