package aplication.ogawadev.com.qrcodegenerate.model

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeCreator

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_gerar_qrcode_geo_localizacao.*

class GerarQRCodeGeoLocalizacao : AppCompatActivity(), OnMapReadyCallback {

    private var latitudeEnvio = ""
    private var longitudeEnvio = ""
    private var mensagemEnvio = ""
    private lateinit var mMap: GoogleMap
    private var qrCodeCreator = QRCodeCreator()
    private lateinit var locationManager : LocationManager
    private lateinit var locationListener: LocationListener
    private var permissoes = arrayOf( Manifest.permission.ACCESS_FINE_LOCATION)
    private var isLeitura = false
    private var isGpsEnabled = false
    //private var isNetworkEnabled = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_gerar_qrcode_geo_localizacao, null)
        setContentView(view)
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })

        locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = object: LocationListener{
            override fun onLocationChanged(location: Location?) {

            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

            }

            override fun onProviderEnabled(provider: String?) {

            }

            override fun onProviderDisabled(provider: String?) {

            }

        }
        /*
        * Valida as Permissões, no caso o ACCES_FINE_LOCATION para pegar a localização atual do GPS
        */
        aplication.ogawadev.com.qrcodegenerate.business.Permissoes.validarPermissoes(permissoes,this,1)
        //locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        btnVoltarLocalizacao.setOnClickListener {
            onBackPressed()
        }

        btnGerarLocalizacao.setOnClickListener {
            var descricao = edtDescricaoLocalizacao.text.toString()
            var latitudeLongitude = edtLatitudeLongitude.text.toString()
            var latitude = getLatitudeLongitude(latitudeLongitude, 0)
            var longitude = getLatitudeLongitude(latitudeLongitude, 1)
            if(latitude.equals("") || longitude.equals("")){
                Toast.makeText(this, "Não foi possível criar o QR Code, tente pegar do maps", Toast.LENGTH_LONG).show()
            }else{
                var mensagemFormatada = formatarConteudoLocalizacao(descricao, latitude, longitude)
                if(mensagemFormatada.isNullOrBlank()){
                    Toast.makeText(this, "É necessário preencher os campos", Toast.LENGTH_SHORT).show()
                }else{
                    if(checkSenhaLocalizacao.isChecked){
                        createAlertDialog(mensagemFormatada)
                    }
                    else{
                        var bundle = qrCodeCreator.createQRCode(mensagemFormatada)
                        if(bundle == null){
                            Toast.makeText(this,"Não foi possível criar o QR Code, tente novamente", Toast.LENGTH_SHORT).show()
                        }else{
                            var imagemTitulo = ImagensCustomizadas("Localização", R.drawable.localizacao)
                            HistoricoGerado.gravarItemNoHistoricoGerado(this,mensagemFormatada, imagemTitulo)
                            irParaResultado(bundle)
                        }
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
        android.app.AlertDialog.Builder(this).setView(view).setTitle("Senha").setMessage("Informe a senha: ")
            .setPositiveButton("Ok"){dialog, which ->
                if(!(senha.text.toString().isNullOrEmpty())){
                    var mensagemCriptografada = "Crip&"+ aplication.ogawadev.com.qrcodegenerate.business.Criptografia.criptografar(mensagemFormatada, senha.text.toString())
                    val imagemTitulo = ImagensCustomizadas("Localização", R.drawable.localizacao)
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

    private fun formatarConteudoLocalizacao(descricao : String , latitude : String, longitude : String ) : String{
        var formatedMessage = ""
        if(!(descricao.isNullOrBlank() || descricao.isNullOrEmpty())
            && !(latitude.isNullOrBlank() || latitude.isNullOrEmpty())
            && !(longitude.isNullOrBlank() || longitude.isNullOrEmpty())
        ){
            formatedMessage = qrCodeCreator.createGeoContent(descricao, latitude,longitude)
            return formatedMessage
        }else if(!(latitude.isNullOrBlank() || longitude.isNullOrEmpty())){
            formatedMessage = qrCodeCreator.createGeoContent(latitude,longitude)
            return formatedMessage
        }
        return formatedMessage
    }
    private fun getLatitudeLongitude(latLng : String, flag : Int) : String{
        if(flag == 0) {
            val latLng = latLng.trim().split(", ")
            var latitude = latLng[0].replace(",", ".")
            return latitude
        }else if(flag == 1){
            val latLng = latLng.trim().split(", ")
            var longitude = latLng[1].replace(",", ".")
            return longitude
        }
        return ""
    }
    override fun onMapReady(googleMap: GoogleMap) {

        val resultadoQRCode = intent.getStringExtra("qrcode")
        if(!(resultadoQRCode.isNullOrEmpty())) {
            isLeitura = true
            //btnSetarLocalizacao.visibility = View.INVISIBLE
            linearLocalizacao2.visibility = View.INVISIBLE
            btnGerarLocalizacao.visibility = View.INVISIBLE
            btnSetarLocalizacao.visibility = View.INVISIBLE

            var qrCodeQuebrado = resultadoQRCode.split(",")
            var qrCodeQuebradoLongitude = qrCodeQuebrado[1].split(QRCodeCreator.GEOREFERENCE_DESCRIPTION)

            for (conteudo in qrCodeQuebrado) {
                if (conteudo.startsWith(QRCodeCreator.GEOREFERENCE)) {
                    latitudeEnvio = conteudo.replace(QRCodeCreator.GEOREFERENCE, "")
                } else if (conteudo.startsWith(QRCodeCreator.GEOREFERENCE_DESCRIPTION)) {
                    mensagemEnvio = conteudo.replace(QRCodeCreator.GEOREFERENCE_DESCRIPTION, "")
                } else {
                    longitudeEnvio = conteudo
                }
            }
            if (qrCodeQuebradoLongitude.size == 2) {
                longitudeEnvio = qrCodeQuebradoLongitude[0]
                mensagemEnvio = qrCodeQuebradoLongitude[1]
            } else if (qrCodeQuebradoLongitude.size == 1) {
                longitudeEnvio = qrCodeQuebradoLongitude[0]
            }

            edtDescricaoLocalizacao.setText(mensagemEnvio)
            edtLatitudeLongitude.setText("${latitudeEnvio}, ${longitudeEnvio}")


            //try {
            val latitude = latitudeEnvio.replace(",", ".").toDouble()
            val longitude = longitudeEnvio.replace(",", ".").toDouble()
            val myLocale = LatLng(latitude, longitude)
            mMap = googleMap
            mMap.clear()
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            mMap.moveCamera(//zoom = 2.0 até 21.0
                CameraUpdateFactory.newLatLngZoom(myLocale, 15f)
            )
            val options = MarkerOptions().position(myLocale).title("Meu local")
            mMap.addMarker(options)
            // } catch (e: Exception) {
            //     Toast.makeText(this, "Essa coordenada não é válida, tente novamente", Toast.LENGTH_LONG).show()
            // }

            btnEnviarLocalizacao.visibility = View.VISIBLE

            btnEnviarLocalizacao.setOnClickListener {
                var gmmIntentUri = Uri.parse("geo:0,0?q=${latitudeEnvio},${longitudeEnvio}(${Uri.encode(mensagemEnvio)})")
                var intent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                intent.setPackage("com.google.android.apps.maps")

                if(intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                }
            }
        }
        if(!isLeitura) {
            mMap = googleMap

            //Define o mapa como normal
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

            checkGpsAndConnection()
            if(isGpsEnabled){
                inicializaLocalInicial()
            }
            //Se segurar um tempo no mapa, muda o ponto de localização
            mMap.setOnMapLongClickListener(object : GoogleMap.OnMapLongClickListener {
                override fun onMapLongClick(latLng: LatLng) {
                    mMap.clear()
                    val options = MarkerOptions().position(latLng).title("Meu local")
                    edtLatitudeLongitude.setText("${latLng.latitude}, ${latLng.longitude}")
                    mMap.addMarker(options)
                }
            })

            btnSetarLocalizacao.setOnClickListener {
                if (!edtLatitudeLongitude.text.isNullOrEmpty() || !edtLatitudeLongitude.text.isNullOrBlank()) {
                    val localizacaoPersonalizada = edtLatitudeLongitude.text.toString()
                    //var novaLocalizacao = StringUtils.removeWhitespace(localizacaoPersonalizada)
                    val latLng = localizacaoPersonalizada.trim().split(", ")
                    try {
                        val latitude = latLng[0].replace(",", ".").toDouble()
                        val longitude = latLng[1].replace(",", ".").toDouble()

                        val myLocale = LatLng(latitude, longitude)
                        mMap.clear()
                        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                        mMap.moveCamera(//zoom = 2.0 até 21.0
                            CameraUpdateFactory.newLatLngZoom(myLocale, 15f)
                        )
                        val options = MarkerOptions().position(myLocale).title("Meu local")
                        mMap.addMarker(options)
                    } catch (e: Exception) {
                        Toast.makeText(this, "Insira uma coordenada válida, copie do maps", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Insira uma coordenada válida", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    private fun inicializaLocalInicial(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            try {
                if (locationManager == null) {
                    locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                }
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER,locationListener,null)
                var location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                if (location == null) {

                    Log.d("localizacao","é nulo")
                }

                var longitude = location.longitude
                var latitude = location.latitude
                var localAtual = LatLng(latitude, longitude)
                edtLatitudeLongitude.setText("${localAtual.latitude}, ${localAtual.longitude}")
                mMap.addMarker(MarkerOptions().position(localAtual).title("Meu local atual"))
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localAtual, 15f))
            }catch(e : Exception){
                Toast.makeText(this, "Falha ao achar a localização, tente novamente mais tarde", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        for (permissaoResultado in grantResults){
            if(permissaoResultado == PackageManager.PERMISSION_DENIED){
                alertaValidacaoPermissao()
            }else if( permissaoResultado == PackageManager.PERMISSION_GRANTED){

                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    if(!isLeitura) {

                        checkGpsAndConnection()
                        if(isGpsEnabled){
                            inicializaLocalInicial()
                        }
                    } else{
                        val longitude = longitudeEnvio.toDouble()
                        val latitude = latitudeEnvio.toDouble()
                        val localAtual = LatLng(latitude,longitude)
                        edtLatitudeLongitude.setText("${localAtual.latitude}, ${localAtual.longitude}")
                        mMap.addMarker(MarkerOptions().position(localAtual).title("Meu local atual"))
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(localAtual, 15f))
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(locationManager == null){
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }

        checkGpsAndConnection()
    }

    override fun onRestart() {
        super.onRestart()
        if(locationManager == null){
            locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        }
        checkGpsAndConnection()
    }

    private fun alertaValidacaoPermissao(){
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Permissoões Negadas")
        builder.setMessage("É necessário aceitar as permissões para que o mapa funcione")
        builder.setCancelable(false)
        builder.setPositiveButton("Confirmar", object: DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                finish()
            }
        })

        val dialog = builder.create()
        dialog.show()
    }

    private fun checkGpsAndConnection(){
        try{
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            //isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }catch(e : Exception){ }
        if(!isGpsEnabled){
            AlertDialog.Builder(this)
                .setMessage("É necessário ativar o GPS para funcionar o app. Deseja ativar?")
                .setPositiveButton("Confirmar", object : DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                        finish()
                    }

                })
                .setNegativeButton("Cancelar", object: DialogInterface.OnClickListener{
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        finish()
                    }
                }).show()
        }
    }
}
