package aplication.ogawadev.com.qrcodegenerate.business

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.manager.MainActivity
import aplication.ogawadev.com.qrcodegenerate.model.ResultadoQRCode
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.journeyapps.barcodescanner.BarcodeEncoder
import java.io.ByteArrayOutputStream
import java.lang.StringBuilder
import java.util.*

class QRCodeCreator : AppCompatActivity(){
    companion object{
        //Tag para e-mail
        //o '&' é para separar as tags e a ',' é para separar os emails dentro de CC e BCC
        // a '?' é para separar o email do CC
        var E_MAIL = "MATMSG:TO:"
        var E_MAIL_2 = "MAILTO:"
        var E_MAIL_SUBJECT = "SUB:"
        var E_MAIL_CC = "cc="
        var E_MAIL_BCC = "bcc="
        var E_MAIL_BODY = "BODY:"
        var E_MAIL_SEPARATOR = ";"
        var E_MAIL_CC_SEPARATOR = ","
        var E_MAIL_SEPARATOR_CONTENT = "&"
//        var E_MAIL = "mailto:"
//        var E_MAIL_SUBJECT = "subject="
//        var E_MAIL_CC = "cc="
//        var E_MAIL_BCC = "bcc="
//        var E_MAIL_BODY = "body="
//        var E_MAIL_SEPARATOR = "?"
//        var E_MAIL_CC_SEPARATOR = ","
//        var E_MAIL_SEPARATOR_CONTENT = "&"

        //Tag para SMS
        var SMS = "SMSTO:"
        var SMS_SEPARATOR = ":"
        //Tag para Contato

        var CONTACT = "MECARD:"
        var CONTACT_NAME = "N:"
        var CONTACT_ADDRESS = "ADR:"
        var CONTACT_TEL = "TEL:"
        var CONTACT_E_MAIL = "EMAIL:"
        var CONTACT_SEPARATOR = ";"

        //Tag para Telefone
        var TELEPHONE_NUMBER = "tel:"

        //Tag para texto
        var TEXT = ""

        //Tag para Localizacao 1° latitude, 2°longitude
        var GEOREFERENCE = "geo:"
        var GEOREFERENCE_SEPARATOR = ","
        var GEOREFERENCE_DESCRIPTION = "?q="
        //Tag para Evento
        var EVENT_BEGIN = "BEGIN:VEVENT"
        var EVENT_SUMMARY = "SUMMARY:"
        var EVENT_START_EV = "DTSTART:"
        var EVENT_END_EV = "DTEND:"
        var EVENT_LOCATION = "LOCATION:"
        var EVENT_DESCRIPTION = "DESCRIPTION:"
        var EVENT_TIME_SEPARATOR = "T"
        var EVENT_END = "END:VEVENT"

        //Tag para URL
        var URL = "http://"
        var URL_2 = "https://"
        //Tag para WiFi
        var WIFI = "WIFI:"
        var WIFI_TYPE = "T:"
        var WIFI_SSID = "S:"
        var WIFI_PASSWORD = "P:"
        var WIFI_IS_SSID_HIDDEN = "H:"
        var WIFI_SEPARATOR = ";"

        //Tag para Personalizado
        var CUSTOM = "custom"
        var CUSTOM_FIELD = "T"
        var CUSTOM_FIELD_TITLE = ":"
        var CUSTOM_FIELD_CONTENT = "&"
        var CUSTOM_CONTENT = "C"
        var CUSTOM_SEPARATOR = ";"
        var CUSTOM_END = ";"
    }
    fun createQRCode(message : String) : Bundle?{

        if(message != null && !message.isEmpty() ){
            try {
                Log.i("qrCode", "entrou antes do try")
                var multiFormatWriter : MultiFormatWriter = MultiFormatWriter()

                var bitMatrix : BitMatrix = multiFormatWriter.encode(message, BarcodeFormat.QR_CODE,500, 500)
                var barcodeEncoder : BarcodeEncoder = BarcodeEncoder()

                var bitMap : Bitmap = barcodeEncoder.createBitmap(bitMatrix)

                //var intent  = Intent(this, ResultadoQRCode::class.java)

                //var myLogo = BitmapFactory.decodeResource(resources, R.drawable.logo)
                //var merge = mergeBitmaps(myLogo, bitMap)
                var byteStream = ByteArrayOutputStream()
                bitMap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream)

                //merge.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
                var byteArray = byteStream.toByteArray()
                var bundle = Bundle()
                bundle.putByteArray("byteArrayQrCode", byteArray)
                return bundle
                // intent.putExtra("qrCodeImg", bundle)
                // startActivity(intent)
                //qrCode.setImageBitmap(bitMap)
            }catch (ex : WriterException){
                Toast.makeText(this, "Não foi possível gerar o QR Code", Toast.LENGTH_SHORT).show()
            }
        }
        return null
    }

    fun createEmailContent(email : String) : String{
        return "${E_MAIL}${email}${E_MAIL_SEPARATOR}"
    }

    fun createEmailContent(email : String, subject : String) : String{
        return "${createEmailContent(email)}${E_MAIL_SUBJECT}${subject}${E_MAIL_SEPARATOR}"
    }

    fun createEmailContent(email : String, subject : String, body : String) : String {
        return "${createEmailContent(email, subject)}${E_MAIL_BODY}${body}${E_MAIL_SEPARATOR}"
    }

    fun createSMSContent(number : String) : String{
        return "${SMS}${number}"
    }

    fun createSMSContent(number : String, message : String) : String{
        return "${createSMSContent(number)}${SMS_SEPARATOR}${message}"
    }

    fun createContactContent(fullName : String) : String{
        return "${CONTACT}${CONTACT_NAME}${fullName}${CONTACT_SEPARATOR}${CONTACT_SEPARATOR}"
    }
    fun createContactContent(fullName : String, tel : String) : String{
        return "${CONTACT}${CONTACT_NAME}${fullName}${CONTACT_SEPARATOR}${CONTACT_TEL}${tel}${CONTACT_SEPARATOR}${CONTACT_SEPARATOR}"
    }
    fun createContactContent(fullName : String, tel : String, email : String) : String{
        return "${CONTACT}${CONTACT_NAME}${fullName}${CONTACT_SEPARATOR}${CONTACT_TEL}${tel}${CONTACT_SEPARATOR}${CONTACT_E_MAIL}${email}${CONTACT_SEPARATOR}${CONTACT_SEPARATOR}"
    }
    fun createTelContent(tel : String) : String{
        return "${TELEPHONE_NUMBER}${tel}"
    }
    fun createTextContent(msg : String) : String{
        return "${msg}"
    }

    fun createGeoContent(latitude : String , longitude : String) : String{
        var latitudeString = latitude.toString()
        var latitudeFormatado = latitudeString.replace(",", ".")
        var longitudeString = longitude.toString()
        var longitudeFormatado = longitudeString.replace(",", ".")
        return "${GEOREFERENCE}${latitudeFormatado}${GEOREFERENCE_SEPARATOR}${longitudeFormatado}"
    }
    fun createGeoContent(description : String, latitude : String , longitude : String) : String{
        var latitudeString = latitude.toString()
        var latitudeFormatado = latitudeString.replace(",", ".")
        var longitudeString = longitude.toString()
        var longitudeFormatado = longitudeString.replace(",", ".")
        return "${GEOREFERENCE}${latitudeFormatado}${GEOREFERENCE_SEPARATOR}${longitudeFormatado}${GEOREFERENCE_DESCRIPTION}${description}"
    }
    fun createEventContent(summary : String) : String{
        return "${EVENT_BEGIN}\n${EVENT_SUMMARY}${summary}\n${EVENT_END}"
    }

    fun createEventContent(summary : String, day_start : String) : String{
        return "${EVENT_BEGIN}\n${EVENT_SUMMARY}${summary}\n${EVENT_START_EV}${day_start}\n${EVENT_END}"
    }
    fun createEventContent(summary : String, day_start : String, day_end : String) : String{
        return "${EVENT_BEGIN}\n${EVENT_SUMMARY}${summary}\n${EVENT_START_EV}${day_start}\n${EVENT_END_EV}${day_end}\n${EVENT_END}"
    }

    fun createEventContent(summary : String, day_start : String, day_end : String, location : String) : String{
        return "${EVENT_BEGIN}\n${EVENT_SUMMARY}${summary}\n${EVENT_START_EV}${day_start}\n${EVENT_END_EV}${day_end}\n${EVENT_LOCATION}${location}\n${EVENT_END}"
    }
    fun createEventContent(summary : String, day_start : String, day_end : String, location : String, description: String ) : String{
        return "${EVENT_BEGIN}\n${EVENT_SUMMARY}${summary}\n${EVENT_START_EV}${day_start}\n${EVENT_END_EV}${day_end}\n${EVENT_LOCATION}${location}\n${EVENT_DESCRIPTION}${description}\n${EVENT_END}"
    }

    fun createURLContent(url : String) : String{
        return "${url}"
    }

    fun createWiFiContent(type : String) : String{
        return "${WIFI}${WIFI_TYPE}${type}"
    }
    fun createWiFiContent(type : String, ssid : String) : String{
        return "${createWiFiContent(type)}${WIFI_SEPARATOR}${WIFI_SSID}${ssid}"
    }
    fun createWiFiContent(type : String, ssid : String, password : String) : String{
        return "${createWiFiContent(type, ssid)}${WIFI_SEPARATOR}${WIFI_PASSWORD}${password}${WIFI_SEPARATOR}"
    }
    fun createWiFiContent(type : String, ssid : String, password : String, isSSIDHidden : String ) : String{
        return "${createWiFiContent(type, ssid, password)}${WIFI_SEPARATOR}${WIFI_IS_SSID_HIDDEN}${isSSIDHidden}${WIFI_SEPARATOR}"
    }
    fun createCustomContent(titulos : ArrayList<String> , textos : ArrayList<String>) : String{
        var customFormatted = StringBuilder()
        var contador = 0
        while(contador <= titulos.size-1){
            var titulo = titulos[contador]
            var texto = textos[contador]
            customFormatted.append("${titulo}${CUSTOM_FIELD_TITLE}${texto}${CUSTOM_SEPARATOR}")
            contador ++
        }
        return "${CUSTOM}${CUSTOM_FIELD_CONTENT}${customFormatted}"
    }

    fun createCustomContent(tituloPersonalizado : String ,titulos : ArrayList<String> , textos : ArrayList<String>) : String{
        var customFormatted = StringBuilder()
        var contador = 0
        while(contador <= titulos.size-1){
            var titulo = titulos[contador]
            var texto = textos[contador]
            if(titulos.size-1 == 0) {
                customFormatted.append("${titulo}${CUSTOM_FIELD_TITLE}${texto}")
            }else{
                customFormatted.append("${titulo}${CUSTOM_FIELD_TITLE}${texto}${CUSTOM_SEPARATOR}")
            }
            contador ++
        }
        return "${tituloPersonalizado}${CUSTOM_FIELD_CONTENT}${customFormatted}"
    }
    //    fun createCustomContent(titulos : ArrayList<String> , textos : ArrayList<String>) : String{
//        var customFormatted = StringBuilder()
//        var contador = 0
//        while(contador <= titulos.size-1){
//            var titulo = titulos[contador]
//            var texto = textos[contador]
//            customFormatted.append("${CUSTOM_FIELD}${contador+1}${CUSTOM_FIELD_TITLE}${titulo}${CUSTOM_FIELD_CONTENT}${CUSTOM_CONTENT}${contador+1}${CUSTOM_FIELD_TITLE}${texto}${CUSTOM_SEPARATOR}")
//            contador ++
//        }
//        return "${CUSTOM}${CUSTOM_FIELD_CONTENT}${customFormatted}${CUSTOM_END}"
//    }
//    fun createCustomContent(conteudoUm : String) : String{
//        return "${CUSTOM}${CUSTOM_FIELD_ONE}${conteudoUm}${CUSTOM_SEPARATOR}${CUSTOM_END}"
//    }
//    fun createCustomContent(conteudoUm : String, conteudoDois : String) : String{
//        return "${CUSTOM}${CUSTOM_FIELD_ONE}${conteudoUm}${CUSTOM_SEPARATOR}${CUSTOM_FIELD_TWO}${conteudoDois}${CUSTOM_SEPARATOR}${CUSTOM_END}"
//    }
//    fun createCustomContent(conteudoUm : String, conteudoDois : String, conteudoTres : String) : String{
//        return "${CUSTOM}${CUSTOM_FIELD_ONE}${conteudoUm}${CUSTOM_SEPARATOR}${CUSTOM_FIELD_TWO}${conteudoDois}${CUSTOM_SEPARATOR}${CUSTOM_FIELD_THREE}${conteudoTres}${CUSTOM_SEPARATOR}${CUSTOM_END}"
//    }
//    fun createCustomContent(conteudoUm : String, conteudoDois : String, conteudoTres : String, conteudoQuatro : String) : String{
//        return "${CUSTOM}${CUSTOM_FIELD_ONE}${conteudoUm}${CUSTOM_SEPARATOR}${CUSTOM_FIELD_TWO}${conteudoDois}${CUSTOM_SEPARATOR}${CUSTOM_FIELD_THREE}${conteudoTres}${CUSTOM_SEPARATOR}${CUSTOM_FIELD_FOUR}${conteudoQuatro}${CUSTOM_SEPARATOR}${CUSTOM_END}"
//    }
//    fun createCustomContent(conteudoUm : String, conteudoDois : String, conteudoTres : String, conteudoQuatro : String, conteudoCinco : String) : String{
//        return "${CUSTOM}${CUSTOM_FIELD_ONE}${conteudoUm}${CUSTOM_SEPARATOR}${CUSTOM_FIELD_TWO}${conteudoDois}${CUSTOM_SEPARATOR}${CUSTOM_FIELD_THREE}${conteudoTres}${CUSTOM_SEPARATOR}${CUSTOM_FIELD_FOUR}${conteudoQuatro}${CUSTOM_SEPARATOR}${CUSTOM_FIELD_FIVE}${conteudoCinco}${CUSTOM_SEPARATOR}${CUSTOM_END}"
//    }
    fun mergeBitmaps(logo: Bitmap, qrCode : Bitmap) : Bitmap {

        var combined = Bitmap.createBitmap(qrCode.width, qrCode.height, qrCode.config)
        var canvas = Canvas(combined)
        var canvasWidth = canvas.width
        var canvasHeight = canvas.height

        canvas.drawBitmap(qrCode, Matrix(), null)

        var resizeLogo = Bitmap.createScaledBitmap(logo, canvasWidth / 5, canvasHeight / 5, true)
        var centreX = (canvasWidth - resizeLogo.width) / 2.0f
        var centreY = (canvasHeight - resizeLogo.height) / 9.0f

        canvas.drawBitmap(resizeLogo, centreX, centreY, null)

        return combined
    }
}