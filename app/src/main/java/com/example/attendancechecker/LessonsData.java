package com.example.attendancechecker;


import android.os.Build;
import android.os.Trace;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
    boolean is_updating_data;
    RequestManager requestManager;
    //TODO реализовать запросы
    public LessonsData(UserData userData){

        is_updating_data = true;
        this.userData = userData;
        requestManager = userData.requestManager;
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
        //this.lessonsData.add(new Object[]{time_start_minute,1,2,"I am a Placeholder HI!!",false,time_start_hour,time_start_minute,time_end_hour,time_end_minute});
        this.lessons_ids = new HashSet<>();
        //this.lessons_ids.add((Integer) lessonsData.get(0)[0]);
        getLessons();

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
    public int[] is_now(String Date,String time_start, String time_end){
        int day = Integer.parseInt(Date.split("-")[2]);
        int month = Integer.parseInt(Date.split("-")[1]);
        int year = Integer.parseInt(Date.split("-")[0]);
        int start_hour = Integer.parseInt(time_start.split(":")[0]);
        int start_minute = Integer.parseInt(time_start.split(":")[1]);
        int end_hour = Integer.parseInt(time_end.split(":")[0]);
        int end_minute = Integer.parseInt(time_end.split(":")[1]);
        LocalDateTime beninging;
        LocalDateTime ending;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            beninging = LocalDateTime.of(year,month,day,start_hour,start_minute);
            ending = LocalDateTime.of(year,month,day,end_hour,end_minute);
            if (beninging.isBefore(LocalDateTime.now())&&(ending.isAfter(LocalDateTime.now()))){
                return  new int[]{beninging.getHour(),beninging.getMinute(),ending.getHour(),ending.getMinute()};
            }
        }

        return new int[]{-1,-1,-1,-1};
    }
    public void getLessons(){

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                requestManager.url + "/lessons/",
                null,

                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        JSONArray responsed = ((JSONArray) response);
                        int[] arr;
                        for (int i = 0; i < responsed.length(); i++) {
                            try {
                                arr = is_now(((JSONObject)responsed.get(i)).getString("date"),
                                        ((JSONObject)responsed.get(i)).getString("time_start"),
                                        ((JSONObject)responsed.get(i)).getString("time_end"));
                                if (arr[0] == -1)
                                    continue;
                                if (((userData.role.equals("Преподаватель"))&&(Integer.parseInt(((JSONObject)responsed.get(i)).getString("teacher_id"))==userData.self_id))
                                        ||
                                        ((userData.role.equals("Староста")) && (Integer.parseInt(((JSONObject)responsed.get(i)).getString("group_id"))==userData.group_id_starosta)))
                                {
                                    lessonsData.add(new Object[]{
                                            Integer.parseInt(((JSONObject) responsed.get(i)).getString("id")),
                                            Integer.parseInt(((JSONObject) responsed.get(i)).getString("group_id")),
                                            Integer.parseInt(((JSONObject) responsed.get(i)).getString("subgroup")),
                                            "I am a Placeholder HI!!",
                                            false,
                                            arr[0], arr[1], arr[2], arr[3],
                                            Integer.parseInt(((JSONObject) responsed.get(i)).getString("discipline_id")),
                                    });
                                    lessons_ids.add(Integer.parseInt(((JSONObject) responsed.get(i)).getString("id")));

                                }
                            } catch (JSONException e) {
                                is_updating_data = false;
                                throw new RuntimeException(e);
                            }

                        }
                        if (responsed.length() == 0){
                            is_updating_data = false;
                        }
                        else{
                            getLessonsNames();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        is_updating_data = false;
                        error.getCause();
                    }
                });
        requestManager.queue.add(jsonArrayRequest);


    }
    public void getLessonsNames(){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                requestManager.url + "/disciplines/",
                null,

                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        JSONArray responsed = ((JSONArray) response);
                        for (Object[] oneLesson: lessonsData){
                            boolean flag = false;
                            for (int i = 0; i < responsed.length(); i++) {
                                try {
                                    if (( (int)oneLesson[9]) == Integer.parseInt(( ((JSONObject) responsed.get(i)).getString("id")))){
                                        oneLesson[3] = ((JSONObject) responsed.get(i)).getString("name");
                                        flag = true;
                                        break;
                                    }
                                } catch (JSONException e) {
                                    is_updating_data = false;
                                    throw new RuntimeException(e);

                                }
                            }
                            if (flag) continue;
                            else{
                                oneLesson[3] = "Unknown";
                            }
                        }

                        is_updating_data = false;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        is_updating_data = false;
                        error.getCause();
                    }
                });
        requestManager.queue.add(jsonArrayRequest);


    }

}
