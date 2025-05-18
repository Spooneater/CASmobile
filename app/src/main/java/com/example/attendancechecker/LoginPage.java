package com.example.attendancechecker;

import static java.lang.Thread.sleep;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class LoginPage extends AppCompatActivity {
    private final String STOPWAITING = "hihihi";
    public static UserData userData;
    public static RequestManager requestManager;
    private TextView errorMessageLoginTextView;
    private Button loginButton;
    private EditText loginEditText, passwordEditText;
    private View overlay;

    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    //TODO Проверка правильности данных. и вывод сообщения об ошибке
    protected void checkLogin(){
        if (! isNetworkConnected()){
            errorMessageLoginTextView.setText("Проверьте интернет-соединение");
            return ;
        }
        String login = loginEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        userData = new UserData(login,password,requestManager,this);
        overlay.setVisibility(View.VISIBLE);
        userData.makeLoginRequest();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (userData.logging_in_now || userData.is_updating_data)
                        sleep(50);

                    Intent intent = new Intent();
                    intent.setAction(STOPWAITING);
                    intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
                    sendBroadcast(intent);
                } catch (InterruptedException e) {

                    throw new RuntimeException(e);
                }
            }
        }).start();
    }
    public void enter(){
        userData.updateData();
        Intent intent = new Intent(LoginPage.this, AttendancePage.class);
        startActivity(intent);
        finish();
    }
    public void setErrorMessage(){
        errorMessageLoginTextView.setText("Неверный логин или пароль");
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestManager = new RequestManager("http://192.168.117.37:5005",this);

        errorMessageLoginTextView = findViewById(R.id.error_message_login);
        loginButton = findViewById(R.id.login_button);
        loginEditText = findViewById(R.id.name_login_from);
        passwordEditText = findViewById(R.id.editTextTextPassword);
        overlay = findViewById(R.id.loginOverlay);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLogin();
            }
        });
        IntentFilter intentFilter = new IntentFilter(STOPWAITING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerReceiver(receiver,intentFilter,RECEIVER_EXPORTED);
        }

    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (STOPWAITING.equals(action)) {

                overlay.setVisibility(View.INVISIBLE);
                if (userData.logged_in)
                    enter();
                else{
                    setErrorMessage();
                }
            }
        }
    };
}