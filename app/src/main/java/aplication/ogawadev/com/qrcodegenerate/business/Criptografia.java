package aplication.ogawadev.com.qrcodegenerate.business;

//import org.apache.commons.codec.binary.Base64;
import android.util.Base64;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.security.MessageDigest;
import java.util.Arrays;


public class Criptografia {

    private static final String ALGORITMO = "AES";

    public static String criptografar(String mensagem, String chave) throws Exception {

        final Cipher cipher = getCipher(Cipher.ENCRYPT_MODE, chave);

        final byte[] criptografado = cipher.doFinal(mensagem.getBytes());

        String retorno = StringUtils.trim(Base64.encodeToString(criptografado, Base64.DEFAULT));
        retorno = retorno + "##Baixe o nosso aplicativo QRCodeGenerate para visualizar o conteúdo do QRCode.";

        return retorno;
    }

    public static String descriptografar(String mensagem, String chave) throws Exception {

        String[] msg = mensagem.split("##");

        final Cipher cipher = getCipher(Cipher.DECRYPT_MODE, chave);

        final byte[] descriptografado = cipher.doFinal(Base64.decode(msg[0], Base64.DEFAULT));

        return new String(descriptografado, "UTF-8");
    }

    private static Cipher getCipher(final int encryptMode, final String chave) throws Exception {

        final Cipher cipher = Cipher.getInstance(ALGORITMO);
        cipher.init(encryptMode, buildKey(chave));

        return cipher;
    }

    private static Key buildKey(String chave) throws Exception {

        final MessageDigest messageDigest = MessageDigest.getInstance("md5");//SHA-256

        final byte[] key = Arrays.copyOf(messageDigest.digest(chave.getBytes("UTF-8")), 16);

        return new SecretKeySpec(key, ALGORITMO);
    }

}
