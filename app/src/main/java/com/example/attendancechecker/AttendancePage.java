package com.example.attendancechecker;

import static java.lang.Thread.sleep;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AttendancePage extends AppCompatActivity {
    public static final String STOPWAITING = "WAITING_!@#$";
    private Button groupBtn, checkAttendanceBtn;
    private LinearLayout tableLayout;
    boolean canClick;
    public UserData userData;
    public LessonsData lessonsData;
    public AttendanceData attendanceData;
    private BluetoothReader bluetoothReader;
    private boolean isMessagePresent;
    //message box
    private View textBox;
    private View overlay;
    private TextView messageView;
    private Button okButton;

    protected void messageBoxHandler(int visibility, String message){
        isMessagePresent = visibility == View.VISIBLE;

        textBox.setVisibility(visibility);
        overlay.setVisibility(visibility);
        messageView.setVisibility(visibility);
        messageView.setText(message);
        okButton.setVisibility(visibility);
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
        canClick = true;
        lessonsData = new LessonsData(userData);
        //Получаем вытащенные данные
        userData = LoginPage.userData;
        attendanceData = new AttendanceData(lessonsData,userData);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isMessagePresent){
                    messageBoxHandler(View.INVISIBLE,"");}
                else{
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this,callback);
        IntentFilter intentFilter = new IntentFilter(STOPWAITING);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerReceiver(receiver,intentFilter,RECEIVER_EXPORTED);
        }
        checkDataUpdateAndShow();

    }
    protected void checkDataUpdateAndShow(){
        LessonsData tempLessonData = new LessonsData(userData);
        if ((!tempLessonData.lessons_ids.equals(lessonsData.lessons_ids))||(userData.isChanged())){
            lessonsData = tempLessonData;
            //Получаем вытащенные данные
            attendanceData = new AttendanceData(lessonsData,userData);}

        //Пока считаем что старосте старостовский экран, остальным учительский
        showPage();
    }

    // В ответ массив и массивов строк где [0]-номер в таблице,[1]-fio, [2]-посещение['+',"-",""]
    protected List<String[]> parseData(){
        List<String[]> studentsAttendance = new ArrayList<>();
        int ind = 1;
        for (Object[] stud: attendanceData.attendanceData){
            studentsAttendance.add(new String[]{String.valueOf(ind),(String)stud[1],(String) stud[3]});
            ind++;
        }
        return  studentsAttendance;
    }
    protected void showPage(){
        ScrollView scrollable_view;
        TextView lessonName;
        TextView time_view;
        if (userData.role.equals("s")){
            setContentView(R.layout.activity_main_screen_startosta);
            checkAttendanceBtn = findViewById(R.id.check_attendance);
            tableLayout = findViewById(R.id.table_layout);
            groupBtn = findViewById(R.id.Group_btn);
            scrollable_view = findViewById(R.id.scrollView2);
            lessonName = findViewById(R.id.lesson_name);
            time_view = findViewById(R.id.time_header);
            overlay = findViewById(R.id.attendanceStarostOverlay);
            textBox = findViewById(R.id.textBoxStarost);
            messageView = findViewById(R.id.confirmTextStarost);
            okButton = findViewById(R.id.confirmStarost);
            messageBoxHandler(View.INVISIBLE,"");
            groupBtn.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View V){

                    Intent intent = new Intent(AttendancePage.this, StudentsPage.class);
                    startActivity(intent);
                }
            });
        }
        else{
            setContentView(R.layout.activity_main_screen_teacher);
            checkAttendanceBtn = findViewById(R.id.check_attendance_tch);
            tableLayout = findViewById(R.id.table_layout_tch);
            scrollable_view = findViewById(R.id.scrollView2_tch);
            lessonName = findViewById(R.id.lesson_name_tch);
            time_view = findViewById(R.id.time_header_tch);
            overlay = findViewById(R.id.attendanceTchOverlay);
            textBox = findViewById(R.id.textBoxTch);
            messageView = findViewById(R.id.confirmTextTch);
            okButton = findViewById(R.id.confirmTch);
            messageBoxHandler(View.INVISIBLE,"");
        }
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V){
                messageBoxHandler(View.INVISIBLE,"");

            }
        });
        if (!lessonsData.lessonsData.isEmpty()){
            lessonName.setText((String)lessonsData.lessonsData.get(0)[3]);
            time_view.setText(lessonsData.getFormatedTime(0));}
        else{
            lessonName.setText("Сейчас не проходит занятие");
            time_view.setText("00:00\n00:00");
        }
        tableLayout.post(new Runnable() {
            @Override
            public void run() {
                for (String[] stud : parseData()){

                    tableLayout.addView(makeTableInst(stud[0],stud[1],stud[2],scrollable_view.getMeasuredWidth()));
                }
            }
        });
        checkAttendanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {

                try {

                    readDevices();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

    }


    private void readDevices() throws InterruptedException {
        if (lessonsData.lessonsData.isEmpty()){
            messageBoxHandler(View.VISIBLE,"Сейчас не проходит занятие");
            return;
        }
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            messageBoxHandler(View.VISIBLE,"Устросйство не поддерживает Bluetooth");
            return;
        } else if (!mBluetoothAdapter.isEnabled()) {
            messageBoxHandler(View.VISIBLE,"Невозможно начать поиск устройств с выключенным Bluetooth");
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bluetoothReader = new BluetoothReader(this);
        }
        changeButtonsClickability(getColor(R.color.ui_blue_grayed),false);
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
    private void changeButtonsClickability(int Color, boolean clickable){
        if(Objects.equals(userData.role, "s")){
            groupBtn.setBackgroundColor(Color);
            groupBtn.setEnabled(clickable);

        }
        checkAttendanceBtn.setBackgroundColor(Color);
        checkAttendanceBtn.setEnabled(clickable);
    }
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (STOPWAITING.equals(action)) {
               changeButtonsClickability(getColor(R.color.ui_blue),true);
               attendanceData.matchAddressesToStudents(bluetoothReader.addresses);
               parseData();
               checkDataUpdateAndShow();

            }
        }
    };
    @Override
    protected void onResume() {
        super.onResume();
        checkDataUpdateAndShow();
        //Code to refresh listview
    }
}