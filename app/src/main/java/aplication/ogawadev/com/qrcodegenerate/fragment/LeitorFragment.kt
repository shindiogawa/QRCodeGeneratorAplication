package aplication.ogawadev.com.qrcodegenerate.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.text.InputType
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.manager.MainActivity
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeCreator
import aplication.ogawadev.com.qrcodegenerate.model.*
import com.google.zxing.*
import java.io.IOException
import me.dm7.barcodescanner.zxing.ZXingScannerView
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.schedule


class LeitorFragment : Fragment(), ZXingScannerView.ResultHandler {

    var mainActivity: MainActivity? = null
    internal val RequestCameraPermissionID = 1001
    private var cameraId = 0
    private var flashOn = false
    private var mScannerView: ZXingScannerView? = null
    private var permissoes = arrayOf(Manifest.permission.CAMERA)
    private val SELECT_PHOTO = 100
    var barcode: String? = null
    //val configFragment = ConfigFragment()
    //val geradorFragment = GeradorFragment()

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (!(activity is MainActivity)) {
            throw RuntimeException("O contexto nao é da activity")
        }

        mainActivity = context as MainActivity
    }

    override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera()           // Stop camera on pause
    }

    override fun onResume() {
        super.onResume()
        scan(view!!)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        var view = inflater.inflate(R.layout.fragment_leitor, container, false)

        MainActivity.navMenu!!.selectedItemId = R.id.itemLer

        if (ContextCompat.checkSelfPermission(context!!, permissoes[0]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(mainActivity!!, permissoes[0])) {
                AlertDialog.Builder(context!!).setTitle("Alerta").setMessage(
                    "Para realizar a leitura é " +
                            " necessário o acesso à camera. Deseja permitir o acesso?"
                )
                    .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(
                            mainActivity!!,
                            permissoes,
                            RequestCameraPermissionID
                        )
                    }).setNegativeButton(android.R.string.no) { dialog, which ->
                        dialog.dismiss()
                    }
                    .create().show()

            } else {
                ActivityCompat.requestPermissions(
                    mainActivity!!,
                    permissoes,
                    RequestCameraPermissionID
                )
            }
        } else {
            try {

                scan(view)

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                MainActivity.setFragment(0, context!!)
            }

            override fun onSwipeLeft() {
                MainActivity.setFragment(2, context!!)
            }
        })


        var btnTrocarCamera = view.findViewById<Button>(R.id.btnTrocarCamera)

        btnTrocarCamera.setOnClickListener {
            btnTrocarCamera.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
            Handler().postDelayed({
                    btnTrocarCamera.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorIconButton))
            },500)


//            activity!!.runOnUiThread(object: Runnable{
//                override fun run() {
//
//
//                }
//            })
////


                      if (cameraId == 0)
                          cameraId = 1
                      else
                          cameraId = 0


                 mScannerView!!.stopCamera()
                  mScannerView!!.setResultHandler(this)
                  mScannerView!!.startCamera(cameraId)
              }



        var btnFlash = view.findViewById<Button>(R.id.btnFlash)
        btnFlash.setOnClickListener {
            if (flashOn) {
                flashOn = false
                btnFlash.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.colorIconButton))
            } else {
                flashOn = true
                btnFlash.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
            }


            mScannerView!!.flash = flashOn
        }

        var btnLeitorGaleria = view.findViewById<Button>(R.id.btnLeitorGaleria)
        btnLeitorGaleria.setOnClickListener {
            btnLeitorGaleria.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
            Handler().postDelayed({
                btnLeitorGaleria.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.colorIconButton))
            },500)

            var intent = Intent(context, aplication.ogawadev.com.qrcodegenerate.business.QRCodeGaleria::class.java)
            startActivity(intent)
        }

        var btnHistoricoLeitura = view.findViewById<Button>(R.id.btnHistorico)
        btnHistoricoLeitura.setOnClickListener {
            btnHistoricoLeitura.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.blue))
            Handler().postDelayed({
                btnHistoricoLeitura.backgroundTintList =
                    ColorStateList.valueOf(resources.getColor(R.color.colorIconButton))
            },500)

            var intent = Intent(context, HistoricoLeitura::class.java)
            startActivity(intent)
        }


        return view
    }

    fun scan(view: View) { //view: View
        var myRelative = view.findViewById<RelativeLayout>(R.id.myRelative)
        mScannerView = ZXingScannerView(mainActivity)
        myRelative.addView(mScannerView)
        mScannerView!!.setResultHandler(this)
        mScannerView!!.startCamera(cameraId)

    }


    override fun handleResult(p0: Result?) {

        if (!(p0!!.text.isNullOrEmpty())) {
            mScannerView!!.resumeCameraPreview(this)
            mScannerView!!.stopCamera()
            var resultado = p0.text
            var dataFormato = SimpleDateFormat("dd/MM/yyyy-HH:mm")
            var cal = Calendar.getInstance()
            var data = cal.time
            var dataString = dataFormato.format(data)
            var imagemModelo: ImagensCustomizadas? = null
            if (p0.text.toLowerCase().contains("crip&")) {
                imagemModelo = ImagensCustomizadas("Criptografado", R.drawable.crip)
                HistoricoLeitura.gravarItemNoHistoricoLeitura(context!!, resultado, imagemModelo!!, dataString)
                createAlertDialog(resultado)
            } else {
                var intent = abreActivityModelo(resultado, context, 0, false)
                startActivity(intent)
            }

        } else {
            Toast.makeText(context, "Não foi possível ler o QRCode.", Toast.LENGTH_SHORT).show()
        }

    }

    fun createAlertDialog(resultado: String) {
        var view = layoutInflater.inflate(R.layout.alert_dialog_senha_layout, null)
        var senha = view.findViewById<EditText>(R.id.edtSenha)
        var resultadoDescriptografado = ""
        senha.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        var alert = AlertDialog.Builder(context)
        alert.setView(view).setTitle("Atenção!")
            .setMessage("Este QRCode está protegido por senha. Informe para ver seu conteúdo:")
            .setPositiveButton("Ok") { dialog, which ->
                if (!(senha.text.toString().isNullOrEmpty())) {
                    try {
                        var resultadoDescrip = resultado.replace("Crip&", "")
                        resultadoDescriptografado =
                            aplication.ogawadev.com.qrcodegenerate.business.Criptografia.descriptografar(resultadoDescrip, senha.text.toString())
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (!(resultadoDescriptografado.isNullOrEmpty())) {
                        var intent = abreActivityModelo(resultadoDescriptografado, context, 0, true)
                        startActivity(intent)
                    } else {
                        Toast.makeText(context, "Senha incorreta!", Toast.LENGTH_SHORT).show()
                        createAlertDialog(resultado)
                    }

                } else {
                    Toast.makeText(context, "A senha deve ser informada.", Toast.LENGTH_SHORT).show()
                    createAlertDialog(resultado)
                }

            }.setNegativeButton("Cancel") { dialog, which ->
                mScannerView!!.startCamera(cameraId)
                dialog.dismiss()
            }.create().show()
    }

    companion object {
        //Quando chamo essa funcao lendo algum qrcode, a acao corresponde a leitura - 0
        //caso chame essa funcao ao clicar em um item, a acao corresponde a clique - 1
        fun abreActivityModelo(
            textoQrCode: String, context: Context?, acao: Int,
            jaGravouHistorico: Boolean
        ): Intent? {
            var intent: Intent? = null
            var dataFormato = SimpleDateFormat("dd/MM/yyyy-HH:mm")
            var cal = Calendar.getInstance()
            var data = cal.time
            var dataString = dataFormato.format(data)
            var imagemModelo: ImagensCustomizadas? = null

            if (textoQrCode.contains(QRCodeCreator.E_MAIL) || textoQrCode.contains(QRCodeCreator.E_MAIL_2)) { //Email
                imagemModelo = ImagensCustomizadas("Email", R.drawable.email)
                intent = Intent(context, GerarQRCodeEmail::class.java)
            } else if (textoQrCode.contains(QRCodeCreator.SMS)) { //Mensagem
                imagemModelo = ImagensCustomizadas("Mensagem", R.drawable.mensagem)
                intent = Intent(context, GerarQRCodeMensagem::class.java)
            } else if (textoQrCode.contains(QRCodeCreator.CONTACT)) {//contato
                imagemModelo = ImagensCustomizadas("Contato", R.drawable.contato)
                intent = Intent(context, GerarQRCodeContato::class.java)
            } else if (textoQrCode.contains(QRCodeCreator.TELEPHONE_NUMBER)) {//telefone
                imagemModelo = ImagensCustomizadas("Telefone", R.drawable.telefone)
                intent = Intent(context, GerarQRCodeTelefone::class.java)
            } else if (textoQrCode.contains(QRCodeCreator.GEOREFERENCE)) {//localizacao
                imagemModelo = ImagensCustomizadas("Localização", R.drawable.localizacao)
                intent = Intent(context, GerarQRCodeGeoLocalizacao::class.java)
            } else if (textoQrCode.contains(QRCodeCreator.EVENT_BEGIN) and textoQrCode.contains(QRCodeCreator.EVENT_END)) {//evento
                imagemModelo = ImagensCustomizadas("Evento", R.drawable.evento)
                intent = Intent(context, GerarQRCodeEvento::class.java)
            } else if (textoQrCode.contains(QRCodeCreator.URL) || textoQrCode.contains(QRCodeCreator.URL_2)) {//url
                imagemModelo = ImagensCustomizadas("Url", R.drawable.url)
                intent = Intent(context, GerarQRCodeUrl::class.java)
            } else if (textoQrCode.contains(QRCodeCreator.WIFI)) {//wifi
                imagemModelo = ImagensCustomizadas("Wifi", R.drawable.wifi)
                intent = Intent(context, GerarQRCodeWifi::class.java)
            } else if (textoQrCode.contains(QRCodeCreator.CUSTOM)) {//customizado
                imagemModelo = ImagensCustomizadas("Personalizado", R.drawable.personalizado)
                intent = Intent(context, GerarQRCodePersonalizado::class.java)
            } else if (textoQrCode.contains("&")) {
                var qrCodeQuebrado = textoQrCode.split("&")
                imagemModelo = ImagensCustomizadas(qrCodeQuebrado.get(0), R.drawable.personalizado)
                intent = Intent(context, GerarQRCodePersonalizadoSalvo::class.java)
            } else {
                imagemModelo = ImagensCustomizadas("Texto", R.drawable.texto)
                intent = Intent(context, GerarQRCodeTexto::class.java)
            }

            if (intent != null) {
                if (acao == 0 && jaGravouHistorico == false)
                    HistoricoLeitura.gravarItemNoHistoricoLeitura(context!!, textoQrCode, imagemModelo!!, dataString)
                intent!!.putExtra("qrcode", textoQrCode)
                return intent
            }
            return intent
        }
    }

}
