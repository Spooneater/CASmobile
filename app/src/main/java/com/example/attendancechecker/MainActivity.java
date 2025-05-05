package com.example.attendancechecker;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private TextView error_message_login;
    private Button login_button,group_btn,check_attendance;
    private LinearLayout table_layout;
    private EditText name_login_from,editTextTextPassword;

    private Button group_btn_tch,check_attendance_tch;
    private LinearLayout table_layout_tch;
    private EditText name_login_from_tch,editTextTextPassword_tch;

    //TODO Проверка вошёл ли уже пользователь

    protected boolean isLoggedIn(){
        return false;
    }
    //TODO Староста -true препод -false
    protected boolean isStarosta(){
        return true;
    }
    //TODO Проверка подключения к сети
    protected boolean noConnection(){
        return true;
    }
    //TODO Проверка правильности данных. и вывод сообщения об ошибке
    protected boolean checkLogin(String login, String password,TextView error_message_login){
        if (true){
            return true;
        }
        else{
            if (noConnection()){
                error_message_login.setText("Проверьте интернет-соединение");
            }
            else{
                error_message_login.setText("Неверный логин или пароль");
            }
            return false;
        }
    }
    //Штука для создания одной строки таблицы посещения
    protected LinearLayout makeTableInst(String id, String fio, String attend, Integer width){

        LinearLayout line = new LinearLayout(this);

        TextView id_text = new TextView(this);
        id_text.setText(id);
        id_text.setGravity(Gravity.CENTER);
        id_text.setWidth(width / 10);
        id_text.setHeight(width / 10);
        id_text.setBackground(AppCompatResources.getDrawable(this,R.drawable.scrol_border));
        line.addView(id_text);

        TextView fio_text = new TextView(this);
        fio_text.setText(fio.toString());
        fio_text.setPadding(5,0,0,0);
        fio_text.setHeight(width / 10);
        fio_text.setGravity(Gravity.CENTER_VERTICAL);
        fio_text.setWidth(width - width / 5);
        fio_text.setBackground(AppCompatResources.getDrawable(this,R.drawable.scrol_border));
        line.addView(fio_text);

        TextView attendance_text = new TextView(this);
        if (attend.equals("+")){
            attendance_text.setText(attend);
            attendance_text.setBackground(AppCompatResources.getDrawable(this,R.drawable.border_in_green));
        } else if (attend.equals("-")){
            attendance_text.setText("Н");
            attendance_text.setBackground(AppCompatResources.getDrawable(this,R.drawable.border_red));
        }
        else{
            attendance_text.setText(" ");
            attendance_text.setBackground(AppCompatResources.getDrawable(this,R.drawable.scrol_border));
        }
        attendance_text.setWidth(width / 10);
        attendance_text.setHeight(width / 10);
        attendance_text.setGravity(Gravity.CENTER);

        line.addView(attendance_text);

        return line;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (isLoggedIn()) {
            if (isStarosta()) mainScreenStarost();
            else mainScreenTeacher();
        }
        else{
            loginScreen();
        }
    }

    //TODO Добавить получение данных о студентишках. В ответ массив и массивов строк где [0]-номер в таблице,[1]-fio, [2]-посещение['+',"-",""]
    protected String[][] getData(){
        String[][] students_Attendance = {{},{},{}};
        students_Attendance[0]= new String[]{"1", "ФАФФОЫАФДЫОЛА", "+"};
        students_Attendance[1]= new String[]{"2", "ASDaSD", "+"};
        students_Attendance[2]= new String[]{"3", "123213", "-"};
        return students_Attendance;
    }
    //TODO Добавить кнопку для перехода в смену группы
    protected void mainScreenStarost(){
        setContentView(R.layout.activity_main_screen_startosta);
        check_attendance = findViewById(R.id.check_attendance);
        table_layout = findViewById(R.id.table_layout);
        group_btn = findViewById(R.id.Group_btn);
        ScrollView scrollable_view = findViewById(R.id.scrollView2);
        table_layout.post(new Runnable() {
            @Override
            public void run() {
                for (String[] stud : getData()){
                    table_layout.addView(makeTableInst(stud[0],stud[1],stud[2],scrollable_view.getMeasuredWidth()));
                }
            }
        });
        check_attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                mainScreenStarost();
            }
        });
        group_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View V){
                Intent intent = new Intent(MainActivity.this,EditGroup.class);
                startActivity(intent);
            }
        });

    }
    protected void mainScreenTeacher(){
        setContentView(R.layout.activity_main_screen_teacher);
        check_attendance_tch = findViewById(R.id.check_attendance_tch);
        table_layout_tch = findViewById(R.id.table_layout_tch);
        ScrollView scrollable_view_tch = findViewById(R.id.scrollView2_tch);
        table_layout_tch.post(new Runnable() {
            @Override
            public void run() {
                for (String[] stud : getData()){
                    table_layout_tch.addView(makeTableInst(stud[0],stud[1],stud[2],scrollable_view_tch.getMeasuredWidth()));
                }
            }
        });
        check_attendance_tch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                mainScreenTeacher();
            }
        });
    }
    protected void loginScreen(){
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
                if (checkLogin(login,password,error_message_login)){
                    if (isStarosta()) mainScreenStarost();
                    else mainScreenTeacher();
                }
            }
        });
    }
}