package com.example.secureapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.amplifyframework.auth.AuthUserAttribute;
import com.amplifyframework.auth.AuthUserAttributeKey;
import com.amplifyframework.auth.options.AuthSignUpOptions;
import com.amplifyframework.auth.result.AuthSignInResult;
import com.amplifyframework.core.Action;
import com.amplifyframework.core.Amplify;
import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

public class LoginUI extends AppCompatActivity {
    // TEXT FIELDS
    TextInputLayout username;
    TextInputLayout password;
    Button click;
    Button register;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_ui);

        // VERIFICATION PROMPT AND USERNAME FROM REGISTRATION
        Intent intent = getIntent();
        String verifyPrompt = intent.getStringExtra(Register.EXTRA_TEXT);
        TextView textView = findViewById(R.id.verifyPrompt);
        textView.setText(verifyPrompt);

        // ASSIGN VARIABLES
        username = findViewById(R.id.UsernameLogin);
        password = findViewById(R.id.PasswordLogin);
        click = findViewById(R.id.LoginButton);
        register = findViewById(R.id.RegisterButton);

        // CALL SIGN IN FUNCTION ON BUTTON CLICK
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInUser(username.getEditText().getText().toString(), password.getEditText().getText().toString());
            }
        });

        // GO TO REGISTER PAGE
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginUI.this, Register.class));
            }
        });
    }

    public void signInUser(String username, String password) {
        System.out.println("Starting signin for: " + username);

        // SIGNING IN USER
        try {
            Amplify.Auth.signIn(
                    username,
                    password,
                    result -> {
                        Log.i("AuthQuickstart", result.isSignInComplete() ? "Sign in succeeded" : "Sign in not complete");
                        loginProcess(result);
                    },
                    error -> {
                        Log.e("AuthQuickStart", "Sign in failed", error);
                        // TODO ADD ALERT DIALOG
                     /* String invalidParameter = Objects.requireNonNull(error.getCause()).toString();
                        invalidParameter = invalidParameter.substring(invalidParameter.indexOf(": ") + 1, invalidParameter.indexOf("(") - 1);
                        System.out.println("LOGIN ERROR: " + invalidParameter);
                        // showAlertDialog(R.layout.error_popup, invalidParameter);*/
                    }
            );
        } catch (Exception e) {
            Log.e("AuthQuickStart", "Sign up failed", e);
            // TODO ADD ALERT DIALOG
            // showAlertDialog(R.layout.error_popup, e);
        }
    }

    private void loginProcess(AuthSignInResult result) {
        Amplify.Auth.fetchAuthSession(
                success -> {
                    Log.i("AmplifyQuickstart", success.toString());
                    Log.i("SignInProcess", success.isSignedIn() ? "SIGN IN SUCCESSFUL":"SIGN IN NOT SUCCESSFUL");
                },
                error -> Log.e("AmplifyQuickstart", error.toString())
        );

        startActivity(new Intent(LoginUI.this, QRScanner.class));
    }
}