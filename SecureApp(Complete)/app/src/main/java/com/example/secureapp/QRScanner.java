package com.example.secureapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.amplifyframework.api.rest.RestOptions;
import com.amplifyframework.core.Action;
import com.amplifyframework.core.Amplify;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class QRScanner extends AppCompatActivity {
    Button btnScan;
    Button btnAPI;
    Button btnSignOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscanner);

        btnScan = findViewById(R.id.BtnScan);
        btnAPI = findViewById(R.id.BtnAPI);
        btnSignOut = findViewById(R.id.BtnSignOut);

        btnScan.setOnClickListener(v-> {
            scanCode();
            // DecodeJSON("hello");
        });

        btnAPI.setOnClickListener(v-> {
            try {
                SendUserKeys();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });

        btnSignOut.setOnClickListener(v-> {
            String username = Amplify.Auth.getCurrentUser().getUsername();
            SignOutUser(username);
        });
    }

    // CONFIGURE SCANNER
    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Place QR in frame");
        // options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        launchScan.launch(options);
    }

    // QR SCAN OPERATION
    ActivityResultLauncher<ScanOptions> launchScan = registerForActivityResult(new ScanContract(), result -> {
        if(result.getContents() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(QRScanner.this);
            builder.setTitle("Scan Successful");
            builder.setMessage("Session ID: " + result.getContents());
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    /*try {
                        DecodeJSON(result.getContents()); // This will be result.getContents()
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }*/
                    dialogInterface.dismiss();
                }
            }).show();
        }
    });

    private void DecodeJSON(String contentsQR) throws JSONException {
        System.out.println("QR CODE: " + contentsQR);
        String finalQR = null;
        String sessionID = null;
        try {
            finalQR = new String(contentsQR.getBytes(), "UTF-8");
            // TODO GET THE SESSION ID FROM QR CODE
            sessionID = new String(contentsQR.getBytes(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        System.out.println("DECODED AS: " + sessionID);


        // MAKE THE API CALLS
        InitializeSession(sessionID);
        SendUserKeys();
    }

    // SEND THE USER SESSION VERIFICATION VIA API
    private void SendUserKeys() throws JSONException {
        /*Request for send_user_keys
        {
            "user_id" : string,
            "ecc_key_id" : string,
            "ecc_public_key" : string,
            "ttl" : int epoch number
        }*/

        // DEFINE API VARIABLES
        String username = Amplify.Auth.getCurrentUser().getUsername();
        String ECCKeyId = "ECCKEYIDSTRING";
        String ECCPublicKey = "ECCPUBLICKEY";
        int EPOCHTime = 812022110;

        // CREATE THE JSON TO BE SENT OVER API
        String JSONAPI = null;
        try {
            // CREATE JSON OBJECT
            final JSONObject JSONAPIO = new JSONObject();

            // BUILD JSON OBJECT
            JSONAPIO.put("user_id", username);
            JSONAPIO.put("ecc_key_id", ECCKeyId);
            JSONAPIO.put("ecc_public_key", ECCPublicKey);
            JSONAPIO.put("ttl", EPOCHTime);

            // CONVERT JSON OBJECT TO STRING
            JSONAPI = JSONAPIO.toString();
        } catch (JSONException e) {
            Log.e("Amplify App", "Failed to create JSONObject", e);
        }
        String finalJSONAPI = JSONAPI;

        // DEFINE API CALL
        assert JSONAPI != null;
        RestOptions postOptions = RestOptions.builder()
                .addPath("/send_user_keys")
                .addBody(JSONAPI.getBytes(StandardCharsets.UTF_8))
                .build();

        // POST OPERATION
        Amplify.API.post(postOptions,
                success -> {
                    Log.i("SendUserKeysPOST", "SENT STRING: " + finalJSONAPI);
                    Log.i("SendUserKeysPOST", "POST successful: " + success);
                    Log.i("SendUserKeysPOST", "RETURNED CODE: " + success.getCode());
                },
                error -> Log.e("SendUserKeysPOST", "POST failure" + error)
        );
    }

    private void InitializeSession(String sessionID) throws JSONException {
     /* Request for initialize_session
        {
            "clear_session_id" : string,
            "username" : user_id (string),
        }*/
        System.out.println("QR CODE: " + sessionID);

        // DEFINE API VARIABLES
        String username = Amplify.Auth.getCurrentUser().getUsername();

        // CREATE THE JSON TO BE SENT OVER API
        String JSONAPI = null;
        try {
            // CREATE JSON OBJECT
            final JSONObject JSONAPIO = new JSONObject();

            // BUILD JSON OBJECT
            JSONAPIO.put("clear_session_id", sessionID);
            JSONAPIO.put("user_id", username);

            // CONVERT JSON OBJECT TO STRING
            JSONAPI = JSONAPIO.toString();
        } catch (JSONException e) {
            Log.e("Amplify App", "Failed to create JSONObject", e);
        }
        String finalJSONAPI = JSONAPI;

        // DEFINE API CALL
        assert JSONAPI != null;
        RestOptions postOptions = RestOptions.builder()
                .addPath("/initialize_session")
                .addBody(JSONAPI.getBytes(StandardCharsets.UTF_8))
                .build();

        // POST OPERATION
        Amplify.API.post(postOptions,
                success -> {
                    Log.i("InitializeSessionPOST", "POST successful: " + success);
                    Log.i("InitializeSessionPOST", "SENT STRING: " + finalJSONAPI);
                    Log.i("InitializeSessionPOST", "RETURNED CODE: " + success.getCode());
                },
                error -> Log.e("InitializeSessionPOST", "POST failure" + error)
        );
    }
    private void SendEncryptedData(String encryptedString) throws JSONException {
        /*Request for send_encrypted_data
        {
            "secret_string" : encrypted string,
            "username" : user_id (string) <- not encrypted at all, send as clear text

        }*/

        // DEFINE API VARIABLES
        String username = Amplify.Auth.getCurrentUser().getUsername();

        // CREATE THE JSON TO BE SENT OVER API
        String JSONAPI = null;
        try {
            // CREATE JSON OBJECT
            final JSONObject JSONAPIO = new JSONObject();

            // BUILD JSON OBJECT
            JSONAPIO.put("secret_string", encryptedString);
            JSONAPIO.put("user_id", username);

            // CONVERT JSON OBJECT TO STRING
            JSONAPI = JSONAPIO.toString();
        } catch (JSONException e) {
            Log.e("Amplify App", "Failed to create JSONObject", e);
        }
        String finalJSONAPI = JSONAPI;

        // DEFINE API CALL
        assert JSONAPI != null;
        RestOptions postOptions = RestOptions.builder()
                .addPath("/send_encrypted_data")
                .addBody(JSONAPI.getBytes(StandardCharsets.UTF_8))
                .build();

        // POST OPERATION
        Amplify.API.post(postOptions,
                success -> {
                    Log.i("EncryptedDataPOST", "POST successful: " + success);
                    Log.i("EncryptedDataPOST", "SENT STRING: " + finalJSONAPI);
                    Log.i("EncryptedDataPOST", "RETURNED CODE: " + success.getCode());
                },
                error -> Log.e("EncryptedDataPOST", "POST failure" + error)
        );
    }

    public void SignOutUser(String username) {
        System.out.println("Starting signout for: " + username);

        // SIGNING OUT USER
        try {
            Amplify.Auth.signOut(
                    new Action() {
                        @Override
                        public void call() {
                            Log.i("AuthQuickstart", "Sign Out Complete");
                            startActivity(new Intent(QRScanner.this, LoginUI.class));
                        }
                    },
                    error -> {
                        Log.e("AuthQuickStart", "Sign in failed", error);
                        // TODO ADD ALERT DIALOG
                        /*String invalidParameter = Objects.requireNonNull(error.getCause()).toString();
                        invalidParameter = invalidParameter.substring(invalidParameter.indexOf(": ") + 1, invalidParameter.indexOf("(") - 1);
                        System.out.println("LOGIN ERROR: " + invalidParameter);
                        showAlertDialog(R.layout.error_popup, invalidParameter);*/
                    }
            );
        } catch (Exception e) {
            Log.e("AuthQuickStart", "Sign up failed", e);
            // TODO ADD ALERT DIALOG
            // showAlertDialog(R.layout.error_popup, e);
        }
    }
}