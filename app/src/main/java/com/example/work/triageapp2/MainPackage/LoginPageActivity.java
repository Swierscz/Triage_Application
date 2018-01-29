package com.example.work.triageapp2.MainPackage;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.work.triageapp2.R;

public class LoginPageActivity extends AppCompatActivity {
    private final static String TAG = LoginPageActivity.class.getSimpleName();
    private Button confirmButton;
    private EditText userNameEditText, passwordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
//        getSupportActionBar().hide();

        confirmButton = (Button)findViewById(R.id.confirmButtonMP);
        userNameEditText = (EditText) findViewById(R.id.userNameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkInputCorrectness();
            }
        });
    }

    private void checkInputCorrectness(){
        if(userNameEditText.getText().toString().equals("jakub")&&
                passwordEditText.getText().toString().equals("jakub")) {
            Toast.makeText(getApplicationContext(),"Confirmed",Toast.LENGTH_SHORT).show();
            launchMainActivity();
        }else{
            Toast.makeText(getApplicationContext(),"Bad Password or Login",Toast.LENGTH_SHORT).show();
        }
    }

    private void launchMainActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
        finish();
    }
}
