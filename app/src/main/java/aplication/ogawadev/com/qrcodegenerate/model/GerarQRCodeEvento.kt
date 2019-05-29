package aplication.ogawadev.com.qrcodegenerate.model

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.nfc.FormatException
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.CalendarContract
import android.view.View
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import android.widget.Toast
import aplication.ogawadev.com.qrcodegenerate.R
import aplication.ogawadev.com.qrcodegenerate.business.ImagensCustomizadas
import aplication.ogawadev.com.qrcodegenerate.business.OnSwipeTouchListener
import aplication.ogawadev.com.qrcodegenerate.business.QRCodeCreator
import kotlinx.android.synthetic.main.activity_gerar_qrcode_evento.*
import java.text.SimpleDateFormat
import java.util.*

class GerarQRCodeEvento : AppCompatActivity(){
    private lateinit var calendar : Calendar
    private var qrCodeCreator = QRCodeCreator()
    private var tituloEnviado = ""
    private var eventoInicioEnviado = ""
    private var eventoTerminoEnviado = ""
    private var localizacaoEnviado = ""
    private var descricaoEnviado = ""

    private var anoInicio = ""
    private var anoTermino = ""

    private var mesInicio = ""
    private var mesTermino = ""

    private var diaInicio = ""
    private var diaTermino = ""

    private var horaInicio = ""
    private var horaTermino = ""

    private var minutoInicio = ""
    private var minutoTermino = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var view = layoutInflater.inflate(R.layout.activity_gerar_qrcode_evento, null)
        setContentView(view)
        view.setOnTouchListener(object : OnSwipeTouchListener() {
            override fun onSwipeRight() {
                onBackPressed()
            }
        })

        val resultadoQRCode = intent.getStringExtra("qrcode")
        if(!(resultadoQRCode.isNullOrEmpty())){
            linearEvento2.visibility = View.INVISIBLE
            btnGerarEvento.visibility = View.INVISIBLE

            var qrCodeQuebrado = resultadoQRCode
                .replace("BEGIN:VEVENT","")
                .replace("SUMMARY:","")
                .replace("DTSTART:",",")
                .replace("DTEND:",",")
                .replace("LOCATION:",",")
                .replace("DESCRIPTION:",",")
                .replace("END:VEVENT","")

            var qrCodeQuebradoTratado = qrCodeQuebrado.split(",")


            for(contador in 0.. qrCodeQuebradoTratado.size-1) {
                if (contador == 0) {
                    tituloEnviado = qrCodeQuebradoTratado[0]
                } else if (contador == 1) {
                    eventoInicioEnviado = qrCodeQuebradoTratado[1]
                } else if (contador == 2) {
                    eventoTerminoEnviado = qrCodeQuebradoTratado[2]
                } else if (contador == 3) {
                    localizacaoEnviado = qrCodeQuebradoTratado[3]
                } else if (contador == 4) {
                    descricaoEnviado = qrCodeQuebradoTratado[4]
                }
            }

            var dataHoraInicio = eventoInicioEnviado.split("T")
            var dataHoraTermino = eventoTerminoEnviado.split("T")

            var dataInicio = dataHoraInicio[0]
            var dataTermino = dataHoraTermino[0]

            var timeInicio = dataHoraInicio[1]
            var timeTermino = dataHoraTermino[1]

             anoInicio = "${dataInicio[0]}${dataInicio[1]}${dataInicio[2]}${dataInicio[3]}"
             anoTermino = "${dataTermino[0]}${dataTermino[1]}${dataTermino[2]}${dataTermino[3]}"

             mesInicio = "${dataInicio[4]}${dataInicio[5]}"
             mesTermino = "${dataTermino[4]}${dataTermino[5]}"

             diaInicio = "${dataInicio[6]}${dataInicio[7]}"
             diaTermino = "${dataTermino[6]}${dataTermino[7]}"

             horaInicio = "${timeInicio[0]}${timeInicio[1]}"
             horaTermino = "${timeTermino[0]}${timeTermino[1]}"

             minutoInicio = "${timeInicio[2]}${timeInicio[3]}"
             minutoTermino = "${timeTermino[2]}${timeTermino[3]}"

            var dataHoraInicioFormatado = "${diaInicio}/${mesInicio}/${anoInicio}, ${horaInicio}:${minutoInicio}"
            var dataHoraTerminoFormatado = "${diaTermino}/${mesTermino}/${anoTermino}, ${horaTermino}:${minutoTermino}"

            edtTituloEvento.setText(tituloEnviado)
            edtTituloEvento.setFocusable(false)
            edtTituloEvento.setFocusableInTouchMode(false)

            edtInicioEvento.setText(dataHoraInicioFormatado)
            edtInicioEvento.setFocusable(false)
            edtInicioEvento.setFocusableInTouchMode(false)

            edtTerminoEvento.setText(dataHoraTerminoFormatado)
            edtTerminoEvento.setFocusable(false)
            edtTerminoEvento.setFocusableInTouchMode(false)

            edtLocalEvento.setText(localizacaoEnviado)
            edtLocalEvento.setFocusable(false)
            edtLocalEvento.setFocusableInTouchMode(false)

            edtDescricaoEvento.setText(descricaoEnviado)
            edtDescricaoEvento.setFocusable(false)
            edtDescricaoEvento.setFocusableInTouchMode(false)

            btnEnviarEvento.visibility = View.VISIBLE

            btnEnviarEvento.setOnClickListener {
                var intent = Intent(Intent.ACTION_EDIT)
                intent.type ="vnd.android.cursor.item/event"

                var beginTime = Calendar.getInstance()
                beginTime.set(anoInicio.toInt(),mesInicio.toInt()-1,diaInicio.toInt(),horaInicio.toInt(),minutoInicio.toInt())
                var startMillis = beginTime.timeInMillis
                var endTime = Calendar.getInstance()
                endTime.set(anoTermino.toInt(),mesTermino.toInt()-1,diaTermino.toInt(),horaTermino.toInt(),minutoTermino.toInt())
                var endMillis = endTime.timeInMillis
                intent.putExtra(CalendarContract.Events.TITLE, tituloEnviado)
                    .putExtra(CalendarContract.Events.DESCRIPTION, descricaoEnviado)
                    .putExtra(CalendarContract.Events.EVENT_LOCATION, localizacaoEnviado)
                    .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,startMillis )
                    .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,endMillis )
                if(intent.resolveActivity(packageManager) != null){
                    startActivity(intent)
                }

            }
        }

        btnVoltarEvento.setOnClickListener {
            onBackPressed()
        }

        btnGerarEvento.setOnClickListener {
            try {
                val dataInicio = edtInicioEvento.text.toString()
                val dataTermino = edtTerminoEvento.text.toString()
                var mensagemFormatada = ""
                if ((dataInicio.isNullOrBlank() || dataInicio.isNullOrEmpty()) || (dataTermino.isNullOrBlank() || dataTermino.isNullOrEmpty())) {
                    Toast.makeText(this, "É necessário preencher a data de início e término", Toast.LENGTH_SHORT).show()
                } else {
                    if (dataInicio.compareTo(dataTermino) > 0) {
                            Toast.makeText(this, "A data de término é menor que a data de início", Toast.LENGTH_SHORT)
                                .show()
                    }else {
                        try {
                            var titulo = edtTituloEvento.text.toString()
                            var local = edtLocalEvento.text.toString()
                            var descricao = edtDescricaoEvento.text.toString()

                            var calendarioInicioFormatada = getDataHoraFormatadaInicio(dataInicio)

                            var calendarioTerminoFormatada = getDataHoraFormatadaTermino(dataTermino)

                            mensagemFormatada = formatarConteudoEvento(
                                titulo,
                                local,
                                descricao,
                                calendarioInicioFormatada,
                                calendarioTerminoFormatada
                            )

                            if (mensagemFormatada.isNullOrBlank() || mensagemFormatada.isNullOrEmpty()) {
                                Toast.makeText(this, "É necessário preencher os campos", Toast.LENGTH_SHORT).show()
                            } else {
                                if(checkSenhaEvento.isChecked){
                                    createAlertDialog(mensagemFormatada)
                                }
                                else{
                                    var bundle = qrCodeCreator.createQRCode(mensagemFormatada)
                                    if (bundle == null) {
                                        Toast.makeText(
                                            this,
                                            "Não foi possível criar o QR Code, tente novamente",
                                            Toast.LENGTH_SHORT
                                        )
                                            .show()
                                    } else {
                                        var imagemTitulo = ImagensCustomizadas("Evento", R.drawable.evento)
                                        HistoricoGerado.gravarItemNoHistoricoGerado(this,mensagemFormatada, imagemTitulo)
                                        irParaResultado(bundle)
                                    }
                                }

                            }
                        }catch(e : Exception){
                            Toast.makeText(this, "É preciso colocar uma data e hora correta", Toast.LENGTH_SHORT).show()
                        }

                    }
                }
            }catch(e : FormatException){
                Toast.makeText(this, "É preciso colocar uma data e hora correta", Toast.LENGTH_SHORT).show()
            }
        }
        edtInicioEvento.setOnClickListener {
            calendar  = Calendar.getInstance()
            DatePickerDialog(this,
                mDateDataSetInicio, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        edtTerminoEvento.setOnClickListener {
            calendar  = Calendar.getInstance()
            DatePickerDialog(this,
                mDateDataSetFim, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show()
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
                    val imagemTitulo = ImagensCustomizadas("Evento", R.drawable.evento)
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

    private fun getDataHoraFormatadaInicio(dataInicio : String) : String{

        val calendarioInicio = dataInicio.split(", ")
        val dataInicio = calendarioInicio[0].split("/")
        val diaInicio = dataInicio[0]
        val mesInicio = dataInicio[1]
        val anoInicio = dataInicio[2]
        val horarioInicio = calendarioInicio[1].split(":")
        val horaInicio = horarioInicio[0]
        val minutoInicio = horarioInicio[1]
        val segundoInicio = "00"


        return "${anoInicio}${mesInicio}${diaInicio}T${horaInicio}${minutoInicio}${segundoInicio}"
    }
    private fun getDataHoraFormatadaTermino(dataTermino : String) : String{
        val calendarioTermino = dataTermino.split(", ")
        val dataTermino = calendarioTermino[0].split("/")
        val diaTermino = dataTermino[0]
        val mesTermino = dataTermino[1]
        val anoTermino = dataTermino[2]
        val horarioTermino = calendarioTermino[1].split(":")
        val horaTermino = horarioTermino[0]
        val minutoTermino = horarioTermino[1]
        val segundoTermino = "00"

        return "${anoTermino}${mesTermino}${diaTermino}T${horaTermino}${minutoTermino}${segundoTermino}"
    }

    private fun irParaResultado(bundle : Bundle){
        var intent  = Intent(this, ResultadoQRCode::class.java)
        intent.putExtra("qrCodeImg", bundle)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun formatarConteudoEvento(titulo : String, local : String, descricao : String, dataInicio : String, dataTermino : String) : String{
        var formatedMessage = ""

        if(!(titulo.isNullOrEmpty() || titulo.isNullOrBlank())
            && !(local.isNullOrEmpty() || local.isNullOrBlank())
            && !(descricao.isNullOrEmpty() || descricao.isNullOrBlank())
            && !(dataInicio.isNullOrEmpty() || dataInicio.isNullOrBlank())
            && !(dataTermino.isNullOrEmpty() || dataTermino.isNullOrBlank())
        ){
            formatedMessage =  qrCodeCreator.createEventContent(titulo, dataInicio, dataTermino, local, descricao)
            return formatedMessage
        }else if(!(titulo.isNullOrEmpty() || titulo.isNullOrBlank())
            && !(local.isNullOrEmpty() || local.isNullOrBlank())
            && !(dataInicio.isNullOrEmpty() || dataInicio.isNullOrBlank())
            && !(dataTermino.isNullOrEmpty() || dataTermino.isNullOrBlank())
        ){
            formatedMessage =  qrCodeCreator.createEventContent(titulo, dataInicio, dataTermino, local)
            return formatedMessage
        }else if(!(titulo.isNullOrEmpty() || titulo.isNullOrBlank())
            && !(dataInicio.isNullOrEmpty() || dataInicio.isNullOrBlank())
            && !(dataTermino.isNullOrEmpty() || dataTermino.isNullOrBlank())
        ){
            formatedMessage =  qrCodeCreator.createEventContent(titulo, dataInicio, dataTermino)
            return formatedMessage
        }
        return formatedMessage
    }

    private val mDateDataSetInicio = object : DatePickerDialog.OnDateSetListener{
        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(this@GerarQRCodeEvento, mTimeDataSetInicio, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

    }

    private val mTimeDataSetInicio = object :TimePickerDialog.OnTimeSetListener{
        override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
            edtInicioEvento.setText(simpleDateFormat.format(calendar.time))
        }

    }

    private val mDateDataSetFim = object : DatePickerDialog.OnDateSetListener{
        override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            TimePickerDialog(this@GerarQRCodeEvento, mTimeDataSetFim, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show()
        }

    }

    private val mTimeDataSetFim = object :TimePickerDialog.OnTimeSetListener{
        override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy, HH:mm", Locale.getDefault())
            edtTerminoEvento.setText(simpleDateFormat.format(calendar.time))
        }
    }
}
