package com.example.attendancechecker;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AttendancePage extends AppCompatActivity {
    public static final String STOPWAITING = "WAITING_!@#$";
    private Button group_btn,check_attendance;
    private LinearLayout table_layout;
    private Button check_attendance_tch;
    private LinearLayout table_layout_tch;
    volatile boolean isChecking;
    boolean canClick;
    public UserData userData;
    public LessonsData lessonsData;
    public AttendanceData attendanceData;
    private BluetoothReader bluetoothReader;
    private Context context;

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
        context = this;
        canClick = true;
        lessonsData = new LessonsData();
        //Получаем вытащенные данные
        userData = LoginPage.userData;
        attendanceData = new AttendanceData(lessonsData,userData);
        IntentFilter intentFilter = new IntentFilter(STOPWAITING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerReceiver(receiver,intentFilter,RECEIVER_EXPORTED);
        }
        loadPage();

    }
    protected void loadPage(){
        LessonsData tempLessonData = new LessonsData();
        if ((tempLessonData.lesson_id != lessonsData.lesson_id)||(userData.isChanged())){
            lessonsData = new LessonsData();
            //Получаем вытащенные данные
            attendanceData = new AttendanceData(lessonsData,userData);}

        //Пока считаем что старосте старостовский экран, остальным учительский
        if (Objects.equals(userData.role, "s")) mainScreenStarost();
        else mainScreenTeacher();
    }

    //TODO Добавить получение данных о студентишках. В ответ массив и массивов строк где [0]-номер в таблице,[1]-fio, [2]-посещение['+',"-",""]
    protected List<String[]> parseData(){
        List<String[]> students_Attendance = new ArrayList<>();
        int ind = 1;
        for (Object[] stud: attendanceData.attendanceData){
            students_Attendance.add(new String[]{String.valueOf(ind),(String)stud[1],(String) stud[3]});
            ind++;
        }

        return  students_Attendance;
        //students_Attendance[0]= new String[]{"1", "ФАФФОЫАФДЫОЛА", "+"};
        //students_Attendance[1]= new String[]{"2", "ASDaSD", "+"};
        //students_Attendance[2]= new String[]{"3", "123213", "-"};
        //return students_Attendance;
    }
    protected void mainScreenStarost(){
        setContentView(R.layout.activity_main_screen_startosta);
        check_attendance = findViewById(R.id.check_attendance);
        table_layout = findViewById(R.id.table_layout);
        group_btn = findViewById(R.id.Group_btn);
        ScrollView scrollable_view = findViewById(R.id.scrollView2);
        TextView lesson_name = findViewById(R.id.lesson_name);
        TextView time_view = findViewById(R.id.time_header);
        lesson_name.setText(lessonsData.name);
        time_view.setText(lessonsData.getFormatedTime());
        table_layout.post(new Runnable() {
            @Override
            public void run() {
                for (String[] stud : parseData()){

                    table_layout.addView(makeTableInst(stud[0],stud[1],stud[2],scrollable_view.getMeasuredWidth()));
                }
            }
        });
        check_attendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {

                try {
                    readDevices();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        group_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View V){

                Intent intent = new Intent(AttendancePage.this, StudentsPage.class);
                startActivity(intent);
            }
        });
    }
    protected void mainScreenTeacher(){
        setContentView(R.layout.activity_main_screen_teacher);

        check_attendance_tch = findViewById(R.id.check_attendance_tch);
        table_layout_tch = findViewById(R.id.table_layout_tch);
        ScrollView scrollable_view_tch = findViewById(R.id.scrollView2_tch);
        TextView lesson_name = findViewById(R.id.lesson_name_tch);
        TextView time_view = findViewById(R.id.time_header_tch);
        lesson_name.setText(lessonsData.name);
        time_view.setText(lessonsData.getFormatedTime());
        table_layout_tch.post(new Runnable() {
            @Override
            public void run() {
                for (String[] stud : parseData()){
                    table_layout_tch.addView(makeTableInst(stud[0],stud[1],stud[2],scrollable_view_tch.getMeasuredWidth()));
                }
            }
        });
        check_attendance_tch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {
                if (!canClick){
                    return;
                }
                loadPage();
            }
        });
    }
    private void readDevices() throws InterruptedException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothReader = new BluetoothReader(context);
        }
        blockClicking();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    sleep(5000);
                    bluetoothReader.die();
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
    private void blockClicking(){
        if(Objects.equals(userData.role, "s")){
            group_btn.setBackgroundColor(getColor(R.color.ui_blue_grayed));
            group_btn.setEnabled(false);
            check_attendance.setBackgroundColor(getColor(R.color.ui_blue_grayed));
            check_attendance.setEnabled(false);
        }
        else{
            check_attendance_tch.setBackgroundColor(getColor(R.color.ui_blue_grayed));
            check_attendance_tch.setEnabled(false);
        }
    }
    private void allowClicking(){
        if(Objects.equals(userData.role, "s")){
            group_btn.setBackgroundColor(getColor(R.color.ui_blue));
            group_btn.setEnabled(true);

            check_attendance.setBackgroundColor(getColor(R.color.ui_blue));
            check_attendance.setEnabled(true);
        }
        else{
            check_attendance_tch.setBackgroundColor(getColor(R.color.ui_blue));
            check_attendance_tch.setEnabled(true);
        }
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (STOPWAITING.equals(action)) {
               allowClicking();
               attendanceData.matchAddressesToStudents(bluetoothReader.addresses);
               parseData();
               loadPage();

            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        loadPage();
        //Code to refresh listview
    }
}