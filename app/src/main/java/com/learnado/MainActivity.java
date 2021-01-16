package com.learnado;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {
    String TAG = "Manish",userName,password;
    EditText EmailTB, PassTB;
    Button LoginB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EmailTB = findViewById(R.id.EmailTB);
        PassTB = findViewById(R.id.PassTB);
        LoginB = findViewById(R.id.Login);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            startActivity(new Intent(this, SendNotif.class));
        } else {
            LoginB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userName = EmailTB.getText().toString().trim();
                    password = PassTB.getText().toString().trim();
                    Log.d(TAG , "User: "+userName+" pass: "+password);
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(userName,password ).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            startActivity(new Intent(MainActivity.this, SendNotif.class));
                        }

                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG,"error: "+e);
                            Toast.makeText(MainActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });


        }

    }

}
