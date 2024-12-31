package com.example.amplitec;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterPageActivity extends AppCompatActivity {

    TextInputEditText editTextEmail, editTextPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView textView;





    //@SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_page);
        mAuth= FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email_username_input);
        editTextPassword = findViewById(R.id.rpassword_input);
        buttonReg = findViewById(R.id.Register_button);
        progressBar = findViewById(R.id.progressBar);
        textView = findViewById(R.id.loginNow);
        ImageView backImage = findViewById(R.id.back_image);

        backImage.setOnClickListener(v -> {
            // Go back to the Welcome Page
            Intent intent = new Intent(RegisterPageActivity.this, MainActivity.class);
            startActivity(intent);
            finish();  // Optionally call finish to close the current activity
        });

        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), LoginPageActivity.class);
                startActivity(intent);
                finish();
            }
        });


        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.GONE);
                String email, password;
                email = String.valueOf(editTextEmail.getText().toString());
                password = String.valueOf(editTextPassword.getText().toString());

                if(TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterPageActivity.this, "Enter Email", Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterPageActivity.this, "Enter Password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {

                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(RegisterPageActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(getApplicationContext(), LoginPageActivity.class);
                                    startActivity(intent);
                                    finish();


                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(RegisterPageActivity.this, "Authentication Failed. Please recheck your Email or Password.", Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
            }
        });

    }
    @Override
    public void onBackPressed() {
        // Display a toast message or perform other actions
        Toast.makeText(this, "Use the back button provided in the app.", Toast.LENGTH_SHORT).show();
        // Do nothing else to prevent default back button behavior
    }

}

