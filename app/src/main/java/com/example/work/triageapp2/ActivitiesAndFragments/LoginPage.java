package com.example.work.triageapp2.ActivitiesAndFragments;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.work.triageapp2.R;

public class LoginPage extends AppCompatActivity {
    Button confirmButton;
    EditText userNameEditText, passwordEditText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        //!!!
//        createMainActivity();
        getSupportActionBar().hide();


        confirmButton = (Button)findViewById(R.id.confirmButtonMP);
        userNameEditText = (EditText) findViewById(R.id.userNameEditText);
        passwordEditText = (EditText) findViewById(R.id.passwordEditText);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(userNameEditText.getText().toString().equals("")&&
                        passwordEditText.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(),"Confirmed",Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(),"Bad Password or Login",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void createMainActivity(){
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);
    }
}
