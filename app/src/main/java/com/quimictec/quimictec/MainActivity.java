package com.quimictec.quimictec;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.media.Image;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class MainActivity extends AppCompatActivity {

    //atributos
    private TextView mHeadingLabel;
    private ImageView mFingerprintImage;
    private TextView mParaLbael;

    private KeyguardManager keyguardManager;
    private FingerprintManager fingerprintManager;

    private KeyStore keyStore;
    private Cipher cipher;
    private String KEY_NAME = "AndroidKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHeadingLabel = (TextView) findViewById(R.id.headingLabel);
        mFingerprintImage = (ImageView) findViewById(R.id.fingerPrintImage);
        mParaLbael = (TextView) findViewById(R.id.paraLabel);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            //verifica se o aparelho nao tem o leitor de fingerprint
            if(!fingerprintManager.isHardwareDetected()){
                mParaLbael.setText("Leitor de biometria não encontrado no seu celular");
            }
            else if (ContextCompat.checkSelfPermission(this,Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED){
                mParaLbael.setText("Permissão negada para utilizar a biometria");
            }
            else if(!keyguardManager.isKeyguardSecure()){
                mParaLbael.setText("Adicione sua digital nas configurações do seu celular");
            }
            else if(!fingerprintManager.hasEnrolledFingerprints()){
                mParaLbael.setText("Você deve adicionar uma digital");
            }
            else{
                mParaLbael.setText("Por favor coloque seu dedo no sensor de biometria");

                FingerprintHandler fingerprintHandler = new FingerprintHandler(this);
                fingerprintHandler.startAuth(fingerprintManager, null);
            }
        }

    }
    @TargetApi(Build.VERSION_CODES.M)
    private  void generateKey() {

        try {

            keyStore = KeyStore.getInstance("AndroidKeyStore");
            KeyGenerator keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME,
                    KeyProperties.PURPOSE_ENCRYPT |
                            KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(
                            KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();

        } catch (KeyStoreException | IOException | CertificateException
                | NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | NoSuchProviderException e) {

            e.printStackTrace();

        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" + KeyProperties.BLOCK_MODE_CBC + "/" + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }


        try {

            keyStore.load(null);

            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME,
                    null);

            cipher.init(Cipher.ENCRYPT_MODE, key);

            return true;

        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException | UnrecoverableKeyException | IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }

    }

}

