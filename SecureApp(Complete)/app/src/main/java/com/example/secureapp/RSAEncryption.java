package com.example.secureapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

// KEY GENERATION
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

import javax.crypto.Cipher;

public class RSAEncryption extends AppCompatActivity {

    static String plainText = "Plain text which needs to be encrypted by Java RSA Encryption in ECB Mode";
    Button click;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rsaencryption);

        click = findViewById(R.id.button);

        click.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                try {
                    CreateKeyPair(plainText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void CreateKeyPair(String plainText) throws Exception {
        System.out.println(plainText);

        // Get an instance of the RSA key Generator
        KeyPairGenerator keyGeneration = KeyPairGenerator.getInstance("RSA");
        keyGeneration.initialize(4096);

        // Generate the KeyPair
        KeyPair pairOfKeys = keyGeneration.generateKeyPair();

        // Get the public and private key
        PublicKey publicKey = pairOfKeys.getPublic();
        PrivateKey privateKey = pairOfKeys.getPrivate();

        // Encryption
        byte[] cipherTextArray = encrypt(plainText, publicKey);
        String encryptedText = Base64.getEncoder().encodeToString(cipherTextArray);
        System.out.println("Encrypted Text : " + encryptedText);

        // Decryption
        String decryptedText = decrypt(cipherTextArray, privateKey);
        System.out.println("Decrypted Text : " + decryptedText);
    }

    public static byte[] encrypt(String plainText, PublicKey publicKey) throws Exception {
        // Get Cipher Instance RSA with ECB Mode and OAEPWITHSHA-512ANDMGF1PADDING padding
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");

        // Initialize Cipher for ENCRYPT_MODE
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        // Perform Encryption
        byte[] cipherText = cipher.doFinal(plainText.getBytes());

        return cipherText;
    }

    public static String decrypt(byte[] cipherTextArray, PrivateKey privateKey) throws Exception {
        // Get Cipher Instance RSA with ECB Mode and OAEPWITHSHA-512ANDMGF1PADDING padding
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");

        // Initialize Cipher for DECRYPT_MODE
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        // Perform Decryption
        byte[] decryptedTextArray = cipher.doFinal(cipherTextArray);

        return new String(decryptedTextArray);
    }
}