package com.example.attendancechecker;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginPage extends AppCompatActivity {
    public static UserData userData;
    private TextView error_message_login;
    private Button login_button;
    private EditText name_login_from,editTextTextPassword;

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    //TODO Проверка правильности данных. и вывод сообщения об ошибке
    protected boolean checkLogin(String login, String password,TextView error_message_login){
        if (! isNetworkConnected()){
            error_message_login.setText("Проверьте интернет-соединение");
            return false;
        }
        if (userData.checkLogin()){
            return true;
        }
        else{

                error_message_login.setText("Неверный логин или пароль");
            return false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        error_message_login = findViewById(R.id.error_message_login);
        login_button = findViewById(R.id.login_button);
        name_login_from = findViewById(R.id.name_login_from);
        editTextTextPassword = findViewById(R.id.editTextTextPassword);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String login = name_login_from.getText().toString();
                String password = editTextTextPassword.getText().toString();
                userData = new UserData(login,password);
                if (checkLogin(login,password,error_message_login)){
                    userData.updateData();
                    Intent intent = new Intent(LoginPage.this, AttendancePage.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}