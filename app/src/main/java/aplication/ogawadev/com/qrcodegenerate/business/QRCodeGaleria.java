package aplication.ogawadev.com.qrcodegenerate.business;

import android.content.Intent;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import aplication.ogawadev.com.qrcodegenerate.model.HistoricoLeitura;
import aplication.ogawadev.com.qrcodegenerate.R;
import aplication.ogawadev.com.qrcodegenerate.fragment.LeitorFragment;
import aplication.ogawadev.com.qrcodegenerate.model.HistoricoLeitura;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ChecksumException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class QRCodeGaleria extends AppCompatActivity implements View.OnClickListener {

    //initialize variables to make them global
    private ImageButton Scan;
    private static final int SELECT_PHOTO = 100;
    //for easy manipulation of the result
    public String barcode;

    //call oncreate method
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_galeria);

        //cast neccesary variables to their views
        Scan = (ImageButton)findViewById(R.id.ScanBut);

        //set a new custom listener
        Scan.setOnClickListener(this);
        //launch gallery via intent
        Intent photoPic = new Intent(Intent.ACTION_PICK);
        photoPic.setType("image/*");
        startActivityForResult(photoPic, SELECT_PHOTO);
    }

    //do necessary coding for each ID
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.ScanBut:
                //launch gallery via intent
                Intent photoPic = new Intent(Intent.ACTION_PICK);
                photoPic.setType("image/*");
                startActivityForResult(photoPic, SELECT_PHOTO);
                break;
        }
    }

    //call the onactivity result method
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch (requestCode) {
            case SELECT_PHOTO:
                if (resultCode == RESULT_OK) {
//doing some uri parsing
                    Uri selectedImage = imageReturnedIntent.getData();
                    InputStream imageStream = null;
                    try {
                        //getting the image
                        imageStream = getContentResolver().openInputStream(selectedImage);
                    } catch (FileNotFoundException e) {
                        Toast.makeText(getApplicationContext(), "Arquivo não encontrado.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    //decoding bitmap
                    Bitmap bMap = BitmapFactory.decodeStream(imageStream);
                    Scan.setImageURI(selectedImage);// To display selected image in image view
                    int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
                    // copy pixel data from the Bitmap into the 'intArray' array
                    bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(),
                            bMap.getHeight());

                    LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(),
                            bMap.getHeight(), intArray);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                    Reader reader = new MultiFormatReader();// use this otherwise
                    // ChecksumException
                    try {
                    /*Hashtable<DecodeHintType, Object> decodeHints = new Hashtable<DecodeHintType, Object>();
                    decodeHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
                    decodeHints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);*/

                        final Result result = reader.decode(bitmap);
                        //*I have created a global string variable by the name of barcode to easily manipulate data across the application*//
                        barcode =  result.getText().toString();

                        //do something with the results for demo i created a popup dialog
                        if(barcode!=null) {
                            final String[] resultado = {barcode};
                            if(barcode.toLowerCase().contains("crip&")){
                                SimpleDateFormat dataFormato = new SimpleDateFormat("dd/MM/yyyy-HH:mm");
                                Calendar cal = Calendar.getInstance();
                                Date data = cal.getTime();
                                String dataString = dataFormato.format(data);
                                ImagensCustomizadas imagemModelo = new ImagensCustomizadas("Criptografado", R.drawable.crip);
                                HistoricoLeitura.Companion.gravarItemNoHistoricoLeitura(this, barcode, imagemModelo, dataString);
                                createAlertDialog(resultado);
                            }
                            else{
                                Intent intent = LeitorFragment.Companion.abreActivityModelo(resultado[0], this, 0, false);
                                finish();
                                startActivity(intent);
                            }

                        }
                        else
                        {
                            AlertDialog.Builder builder = new AlertDialog.Builder(this);
                            builder.setTitle("Resultado do scan");
                            builder.setIcon(R.mipmap.ic_launcher);
                            builder.setMessage("Nada encontrado, tente uma imagem diferente ou tente novamente.");
                            AlertDialog alert1 = builder.create();
                            alert1.setButton(DialogInterface.BUTTON_POSITIVE, "Done", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    onBackPressed();
                                    /*Intent i = new Intent (getBaseContext(),MainActivity.class);
                                    startActivity(i);*/
                                }
                            });

                            alert1.setCanceledOnTouchOutside(false);

                            alert1.show();

                        }
                        //the end of do something with the button statement.

                    } catch (NotFoundException e) {
                        Toast.makeText(getApplicationContext(), "Nada encontrado", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (ChecksumException e) {
                        Toast.makeText(getApplicationContext(), "Algo estranho aconteceu.", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (NullPointerException e) {
                        Toast.makeText(getApplicationContext(), "Algo estranho aconteceu", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    } catch (com.google.zxing.FormatException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    onBackPressed();
                }
        }
    }

    @Override
    public void onBackPressed(){
        finish();
    }

    public void createAlertDialog(final String[] resultado){
        final View view = View.inflate(this, R.layout.alert_dialog_senha_layout, null);
        final EditText senha = view.findViewById(R.id.edtSenha);
        senha.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(view);
        builder.setTitle("Atenção!");
        builder.setMessage("Este QRCode está protegido por senha. Informe para ver seu conteúdo:");
        AlertDialog alert = builder.create();
        alert.setButton(DialogInterface.BUTTON_POSITIVE, "Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String resultadoDescrip = "";
                if (!(senha.getText().toString().isEmpty())) {
                    try {
                        //StringTokenizer stringTokenizer = new StringTokenizer(resultado[0], "&");
                        //0String cript = stringTokenizer.nextToken();
                        String conteudoQrCode = resultado[0].replace("Crip&", "");

                        resultadoDescrip = Criptografia.descriptografar(conteudoQrCode, senha.getText().toString());

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(!(resultadoDescrip.isEmpty())){
                        Intent intent = LeitorFragment.Companion.abreActivityModelo(resultadoDescrip, getApplicationContext(), 0, true);
                        finish();
                        startActivity(intent);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), "Senha incorreta!", Toast.LENGTH_SHORT).show();
                        createAlertDialog(resultado);
                    }

                }
                else {
                    Toast.makeText(getApplicationContext(), "A senha deve ser informada.", Toast.LENGTH_SHORT).show();
                    createAlertDialog(resultado);
                }
            }
        });
        alert.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
                dialog.dismiss();
            }
        });

        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }

}


