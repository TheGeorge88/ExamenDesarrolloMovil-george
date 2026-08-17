package com.example.veterinarialemas;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.veterinarialemas.activity.MenuActivity;
import com.example.veterinarialemas.notification.AppFirebaseMessagingService;
import com.example.veterinarialemas.utils.SharedPreferencesManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvRegisterUser;
    TextInputEditText tieUser;
    TextInputEditText tiePass;
    MaterialButton btnIngresar;

    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boolean estaLogueado = SharedPreferencesManager.getValorEsperadoBoolean(this, "VETPREFERENCES", "isLogin");
        if (estaLogueado) {
            callMenuActivity();
        }
        tvRegisterUser = findViewById(R.id.txt_create_user);
        tvRegisterUser.setOnClickListener(this);
        tieUser = findViewById(R.id.tie_user);
        tiePass = findViewById(R.id.tie_pass);
        btnIngresar = findViewById(R.id.btn_ingresar);
        btnIngresar.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
         checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 100);
        }

        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener( token -> {
                    Log.d("FCM", "Token generado  "+token);
                    AppFirebaseMessagingService.registerTokenInBackend(token);
                });
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.txt_create_user) {
            Intent intento = new Intent(this, RegisterActivity.class);
            startActivity(intento);
        } else if (v.getId() == R.id.btn_ingresar) {
            validateUserExist();
        }
    }

    private void validateUserExist() {
        String user = tieUser.getText().toString();
        String pass = tiePass.getText().toString();
        mAuth.signInWithEmailAndPassword(user, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            SharedPreferencesManager.setValorBoolean(MainActivity.this, "VETPREFERENCES", true, "isLogin");
                            callMenuActivity();
                        } else {
                            Snackbar.make(MainActivity.this.getCurrentFocus(), "Credenciales incorrecta", Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .addOnFailureListener(this, e -> e.printStackTrace());
    }

    private void callMenuActivity() {
        Intent intento = new Intent(this, MenuActivity.class);
        startActivity(intento);
        finish();
    }
}