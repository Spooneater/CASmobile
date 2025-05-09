package com.example.attendancechecker;


import android.os.Build;


import java.time.LocalDateTime;

public class LessonsData {
    int lesson_id;

    int time_start_hour;
    int time_start_minute;
    int time_end_hour;
    int time_end_minute;
    int group_id;
    int subgroup_id;

    String name;
    //TODO реализовать запросы
    public LessonsData(){
        this.lesson_id = 1;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.time_start_hour = LocalDateTime.now().getHour();
            this.time_end_hour = LocalDateTime.now().getHour()+1;
            this.time_start_minute = LocalDateTime.now().getMinute();
            this.time_end_minute = LocalDateTime.now().getMinute()+35;
        }
        this.name = "I am a Placeholder HI!!";
        this.group_id = 1;
        this.subgroup_id = 2;

    }
    public String getFormatedTime(){
        StringBuilder time = new StringBuilder();
        if (this.time_start_hour<10) time.append("0");
        time.append(this.time_start_hour);
        time.append(":");
        if (this.time_start_minute<10) time.append("0");
        time.append(this.time_start_minute);
        time.append("\n");
        if (this.time_end_hour<10) time.append("0");
        time.append(this.time_end_hour);
        time.append(":");
        if (this.time_end_minute<10) time.append("0");
        time.append(this.time_end_minute);
        return  time.toString();
    }
}
