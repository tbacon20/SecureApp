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
import com.amplifyframework.auth.result.AuthSignUpResult;
import com.amplifyframework.core.Amplify;
import com.google.android.material.textfield.TextInputLayout;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Objects;

public class Register extends AppCompatActivity {
    // TEXT FIELDS
    TextInputLayout email;
    TextInputLayout username;
    TextInputLayout password;
    String website;
    Button click;
    Button login;

    public static final String EXTRA_TEXT = "com.example.secure_app.example.EXTRA_TEXT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // ASSIGN VARIABLES
        email = findViewById(R.id.EmailTextView);
        username = findViewById(R.id.UsernameTextView);
        password = findViewById(R.id.PasswordTextView);
        website = "Amazon.com";
        click = findViewById(R.id.button_register);
        login = findViewById(R.id.button_login);

        // CALL REGISTER FUNCTION ON BUTTON CLICK
        click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser(username.getEditText().getText().toString(), email.getEditText().getText().toString(), website, password.getEditText().getText().toString());
            }
        });

        // DIRECT TO LOGIN SCREEN
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, LoginUI.class));
            }
        });
    }

    public void registerUser(String username, String email, String website, String password) {
        System.out.println("Starting registration for: " + username);


        // ADDING USER ATTRIBUTES
        ArrayList<AuthUserAttribute> attributes = new ArrayList<>();
        attributes.add(new AuthUserAttribute(AuthUserAttributeKey.email(), email));
        attributes.add(new AuthUserAttribute(AuthUserAttributeKey.website(), website));

        // ASSIGNING ATTRIBUTES TO SIGNUP
        AuthSignUpOptions options = AuthSignUpOptions.builder()
                .userAttributes(attributes)
                .build();

        // SIGNING UP USER
        try {
            Amplify.Auth.signUp(username, password, options,
                    result -> {
                        Log.i("AmplifySignUp", "Result: " + result);

                        assert result.getUser() != null;
                        String userID = Objects.requireNonNull(result.getUser().getUserId());
                        System.out.println("User ID: " + userID);

                        // START LOGIN ACTIVITY
                        String verifyPrompt = "We just sent a verification email to " + email + ". Please verify your email before logging in.";
                        Intent intent = new Intent(Register.this, LoginUI.class);
                        intent.putExtra(EXTRA_TEXT, verifyPrompt);
                        startActivity(intent);
                    },
                    error -> {
                        Log.e("AmplifySignUp", "Sign up failed", error);
                       /* String invalidParameter = Objects.requireNonNull(error.getCause()).toString();
                        invalidParameter = invalidParameter.substring(invalidParameter.indexOf(": ") + 1, invalidParameter.indexOf("(") - 1);
                        System.out.println("LOGIN ERROR: " + invalidParameter);*/
                        // TODO ADD ALERT DIALOG
                        //showAlertDialog(R.layout.error_popup, invalidParameter);
                    }
            );
        } catch (Exception e) {
            Log.e("AuthQuickStart", "Sign up failed", e);
            // TODO ADD ALERT DIALOG
            //showAlertDialog(R.layout.error_popup, e);
        }
    }
}