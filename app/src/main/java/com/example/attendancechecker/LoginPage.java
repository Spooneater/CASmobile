package com.example.attendancechecker;

import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginPage extends AppCompatActivity {
    public static UserData userData;
    private TextView errorMessageLoginTextView;
    private Button loginButton;
    private EditText loginEditText, passwordEditText;

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    //TODO Проверка правильности данных. и вывод сообщения об ошибке
    protected boolean checkLogin(){
        if (! isNetworkConnected()){
            errorMessageLoginTextView.setText("Проверьте интернет-соединение");
            return false;
        }
        String login = loginEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        userData = new UserData(login,password);
        if (userData.logged_in){
            return true;
        }
        else{

            errorMessageLoginTextView.setText("Неверный логин или пароль");
            return false;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        errorMessageLoginTextView = findViewById(R.id.error_message_login);
        loginButton = findViewById(R.id.login_button);
        loginEditText = findViewById(R.id.name_login_from);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkLogin()){
                    userData.updateData();
                    Intent intent = new Intent(LoginPage.this, AttendancePage.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}