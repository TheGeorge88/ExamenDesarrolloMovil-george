package com.example.veterinarialemas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    TextInputEditText tieUserRegister;
    TextInputEditText tiePassRegister;
    Button btnSaveRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        tieUserRegister = findViewById(R.id.tie_user_register);
        tiePassRegister = findViewById(R.id.tie_pass_register);
        btnSaveRegister = findViewById(R.id.btn_save_register);
        btnSaveRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_save_register) {
            String user = tieUserRegister.getText().toString();
            String pass = tiePassRegister.getText().toString();
            saveUser(user, pass);
        }
    }

    private void saveUser(String user, String pass) {
        mAuth.createUserWithEmailAndPassword(user, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Se ha creado el usuario", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegisterActivity.this, "No se ha creado el usuario", Toast.LENGTH_LONG).show();
                        }
                        finish();
                    }
                })
                .addOnFailureListener(this, e -> e.printStackTrace());
    }
}