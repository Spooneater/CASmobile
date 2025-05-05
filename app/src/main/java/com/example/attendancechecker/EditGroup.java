package com.example.attendancechecker;

import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class EditGroup extends AppCompatActivity {
    public static final int WIRED = 123456;
    public static final int NOTWIRED = 654321;
    private View editStudOverlay;
    private View textBox;
    private TextView confirmText,errorMessageConf;
    private Button confirmStudBtn,declineStudBtn;
    //Students_data[i] : i - id студента в рамках таблицы(+1); [0]-id студента используемый для привязки устройства (long); [1]-ФИО (String);[2] : привязано ли устройство(Boolean)
    protected Object[][] students_data;
    protected Boolean is_asking_to_confirm = false;
    private int chosen_student_id = -1;
    //TODO Получение данных по api будет тут
    protected String getGroupNumber(){
        return "22307place_holder";
    }
    //TODO Заполнение данных о студентах
    protected void fillStudentsData(){

        //Magic data
        int n=3;
        students_data = new Object[n][3];
        students_data[0]= new Object[]{(long) 10, "I am first", true};
        students_data[1]= new Object[]{(long) 20, "I am second", false};
        students_data[2]= new Object[]{(long) 30, "I am third", true};


    };
    //Создает и возвращает отображение которое мы добавим в таблицу
    protected LinearLayout makeTableInst(int id, String fio, boolean isWired,long student_id, Integer width){

        LinearLayout line = new LinearLayout(this);

        TextView id_text = new TextView(this);
        id_text.setText(Integer.toString(id+1));
        id_text.setGravity(Gravity.CENTER);
        id_text.setWidth(width / 6);
        id_text.setHeight(width / 6);
        id_text.setBackground(AppCompatResources.getDrawable(this,R.drawable.only_vert_border));
        line.addView(id_text);

        TextView fio_text = new TextView(this);
        fio_text.setText(fio.toString());
        fio_text.setPadding(15,0,0,0);
        fio_text.setHeight(width / 6);

        fio_text.setGravity(Gravity.CENTER_VERTICAL);
        fio_text.setWidth(width - width / 3);
        fio_text.setBackground(AppCompatResources.getDrawable(this,R.drawable.only_vert_border));
        line.addView(fio_text);

        Button edit_btn = new Button(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width/6,width/6);
        edit_btn.setLayoutParams(params);

        if (isWired) {

            edit_btn.setBackground(AppCompatResources.getDrawable(this, R.drawable.change_thing));
        }
        else{
            edit_btn.setText("");
            edit_btn.setBackground(AppCompatResources.getDrawable(this,R.drawable.plus_device));
        }
        line.setBackground(AppCompatResources.getDrawable(this,R.drawable.scrol_border));
        edit_btn.setGravity(Gravity.CENTER);

        //Вытаскиваем id студента
        edit_btn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View V){
                showConfirmBox(id);
                chosen_student_id = id;
            }
        });
        line.addView(edit_btn);

        return line;
    }
    //Показывает оверлей для подтверждениея выбора студента

    protected void showConfirmBox(Integer id){
        is_asking_to_confirm = true;
        textBox.setVisibility(View.VISIBLE);
        editStudOverlay.setVisibility(View.VISIBLE);
        confirmText.setVisibility(View.VISIBLE);
        errorMessageConf.setVisibility(View.VISIBLE);
        confirmStudBtn.setVisibility(View.VISIBLE);
        declineStudBtn.setVisibility(View.VISIBLE);
        errorMessageConf.setText("");
        confirmText.setText(String.format("Вы уверены, что хотите выбрать устройство для студента %s?", students_data[id][1]));

    };
    //Скрывает оверлей для подтверждениея выбора студента
    protected void hideConfirmBox(){
        is_asking_to_confirm = false;
        textBox.setVisibility(View.INVISIBLE);
        editStudOverlay.setVisibility(View.INVISIBLE);
        confirmText.setVisibility(View.INVISIBLE);
        errorMessageConf.setVisibility(View.INVISIBLE);
        confirmStudBtn.setVisibility(View.INVISIBLE);
        declineStudBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //При вызове "назад" мы сначала скроем оверлей, если он открыт. Иначе переходим назад
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (is_asking_to_confirm){
                    hideConfirmBox();}
                else{
                    finish();
                }
            }
        };

        super.onCreate(savedInstanceState);

        loadStuff();
        getOnBackPressedDispatcher().addCallback(this,callback);
    }
    //TODO сделать проверку включенности bluetooth
    public boolean checkIfBluetoothed(){
        return true;
    }
    //Отрисовка экрана
    protected void loadStuff(){
        setContentView(R.layout.activity_edit_group);
        textBox = findViewById(R.id.textBox);
        editStudOverlay = findViewById(R.id.editStudOverlay);
        TextView TextThing = findViewById(R.id.TextThing);
        TextThing.setText(String.format("Управление группой (%s)", getGroupNumber()));
        confirmText = findViewById(R.id.confirmText);
        errorMessageConf = findViewById(R.id.errorMessageConf);
        confirmStudBtn = findViewById(R.id.confirmStudBtn);
        declineStudBtn = findViewById(R.id.declineStudBtn);
        Button retBtn = findViewById(R.id.ret_button);
        retBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        declineStudBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideConfirmBox();
            }
        });
        confirmStudBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (! checkIfBluetoothed()){
                    errorMessageConf.setText("Невозможно начать поиск устройств с выключенным Bluetooth");
                }
                else{
                    errorMessageConf.setText("");
                    Intent intent = new Intent(EditGroup.this,pickDevice.class);
                    intent.putExtra("student_id",(long)students_data[chosen_student_id][0]);
                    intent.putExtra("student_name",(String)students_data[chosen_student_id][1]);
                    startActivity(intent);
                    hideConfirmBox();
                }
            }
        });
        hideConfirmBox();
        //Собираем данные о студентах
        fillStudentsData();
        LinearLayout tableLayoutStud = findViewById(R.id.tableLayoutStud);
        //Ждем пока всё отрисуется, чтобы можно было вытащить размер.
        tableLayoutStud.post(new Runnable() {
            @Override
            public void run() {
                Integer i =0;
                for (Object[] inst : students_data) {
                    tableLayoutStud.addView(makeTableInst(i, (String)inst[1], (boolean) inst[2], (long) inst[0], tableLayoutStud.getWidth()));
                    i = i + 1;
                } ;
            }
        });

    }
}
