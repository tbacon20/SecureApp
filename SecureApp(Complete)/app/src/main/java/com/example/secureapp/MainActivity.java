package com.example.secureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

// AMPLIFY PLUGINS
import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin;
// import com.amplifyframework.datastore.AWSDataStorePlugin;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Amplify API
        try {
            Amplify.addPlugin(new AWSCognitoAuthPlugin());
            System.out.println("COGNITO DONE");
            Amplify.addPlugin(new AWSApiPlugin());
            System.out.println("API DONE");
            // Amplify.addPlugin(new AWSDataStorePlugin());
            // System.out.println("DATASTORE DONE");
            Amplify.configure(getApplicationContext());
            System.out.println("CONFIGURATION DONE");

            Log.i("SecureApp", "Initialized Amplify");
        } catch (AmplifyException error) {
            Log.e("SecureApp", "Could not initialize Amplify");
        }

        Amplify.Auth.fetchAuthSession(
                result -> Log.i("AmplifyQuickstart", result.toString()),
                error -> Log.e("AmplifyQuickstart", error.toString())
        );

        // Direct User To Sign in or Sign up
        /* TODO Buttons Sign in / Sign up */
        startActivity(new Intent(MainActivity.this, Register.class));
        // startActivity(new Intent(MainActivity.this, LoginUI.class));
    }
}