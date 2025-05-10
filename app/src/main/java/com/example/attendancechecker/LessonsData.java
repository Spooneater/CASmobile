package com.example.attendancechecker;


import android.os.Build;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class LessonsData {
    LocalDate time;
    //0- id, 1 - id_gr , 2-id_sbgr, 3-название 4-погруппам или нет 5-6 нач hh mm, 7-8 hh:mm конец
    List<Object[]> lessonsData;
    UserData userData;
    HashSet<Integer> lessons_ids;
    //TODO реализовать запросы
    public LessonsData(UserData userData){
        this.userData = userData;
        int time_start_hour=0;
        int time_end_hour=0;
        int time_start_minute=0;
        int time_end_minute=0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.time = LocalDate.now();
            time_start_hour = LocalDateTime.now().getHour();
            time_end_hour = LocalDateTime.now().getHour()+1;
            time_start_minute = LocalDateTime.now().getMinute();
            time_end_minute = LocalDateTime.now().getMinute()+35;
        }
        this.lessonsData = new ArrayList<>();
        this.lessonsData.add(new Object[]{time_start_minute,1,2,"I am a Placeholder HI!!",false,time_start_hour,time_start_minute,time_end_hour,time_end_minute});
        this.lessons_ids = new HashSet<>();
        this.lessons_ids.add((Integer) lessonsData.get(0)[0]);
    }
    public String getFormatedTime(int index){
        StringBuilder time = new StringBuilder();
        if ((int)this.lessonsData.get(index)[5]<10) time.append("0");
        time.append((int)this.lessonsData.get(index)[5]);
        time.append(":");
        if ((int)this.lessonsData.get(index)[6]<10) time.append("0");
        time.append((int)this.lessonsData.get(index)[6]);
        time.append("\n");
        if ((int)this.lessonsData.get(index)[7]<10) time.append("0");
        time.append((int)this.lessonsData.get(index)[7]);
        time.append(":");
        if ((int)this.lessonsData.get(index)[8]<10) time.append("0");
        time.append((int)this.lessonsData.get(index)[8]);
        return  time.toString();
    }
}
