package com.example.attendancechecker;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

public class AttendanceData {
    LessonsData lessonsData;
    UserData userData;
    //[0] id студента.[2]-адрес устройства [1]-ФИО [3]-"+","-","na", [4] - id_занятий
    List<Object[]> attendanceData;
    boolean is_sending;
    final Context context;
    int left_to_send;
    public AttendanceData(LessonsData lessonData,UserData userData,Context context){
        this.lessonsData = lessonData;
        this.userData = userData;
        this.context = context;
        this.attendanceData = new ArrayList<Object[]>();
        boolean notAdded;
        //Формируем
        for (Object[] oneStudAtt: userData.studentsData) {
            is_sending = false;
            notAdded = true;
            for (Object[] oneLessonData: lessonData.lessonsData){
                if (((int)oneLessonData[1] ==(int) oneStudAtt[userData.GROUP_ID]) &&
                        (((int)oneLessonData[1] == (int) oneStudAtt[userData.SUB_GROUP_ID]) || (!(boolean) oneLessonData[4]))) {
                    if (notAdded) {
                        attendanceData.add(new Object[]{
                                oneStudAtt[userData.STUDENT_ID],
                                oneStudAtt[userData.FIO],
                                oneStudAtt[userData.ADDRESS],
                                "na",
                                new ArrayList<Integer>()
                        });

                        ArrayList<Integer> thing = (ArrayList<Integer>) attendanceData.get(attendanceData.size()-1)[4];
                        thing.add((Integer) oneLessonData[0]);
                        notAdded = false;
                    }
                    else{
                        ArrayList<Integer> thing = (ArrayList<Integer>) attendanceData.get(attendanceData.size()-1)[4];
                        thing.add((Integer) oneLessonData[0]);
                    }
                }
            }
        }

    }

    public boolean matchAddressesToStudents(HashSet<String> addresses){
        for (Object[] oneStudent: attendanceData){
            if ((Objects.equals( userData.role, "s"))&&(userData.self_id == (int)oneStudent[0])){
                oneStudent[3] = "+";
                continue;
            }
            for (String address: addresses){

                if (oneStudent[2].equals(address)) {
                    oneStudent[3] = "+";
                    break;
                }

            }
            if (oneStudent[3]=="na")
                oneStudent[3]="-";
        }
        //If successful
        if (true){
            return true;
        }
        return false;
    }
    //TODO Решить какие записи я могу делать о посещаемости
    public boolean sendData() throws JSONException {
        is_sending = true;
        left_to_send = attendanceData.size();
        for (Object[] att: attendanceData){
            if (!att[3].equals("+"))
                continue;
            for (int lesson_id: (ArrayList<Integer>)att[4])
            {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("lesson_id",lesson_id);
                jsonObject.put("student_id",att[0]);
                jsonObject.put("mark",att[3]);
                JsonObjectRequest jsonObjectRequest
                        = new JsonObjectRequest(
                        Request.Method.POST,
                        userData.requestManager.url+"/attendances",
                        jsonObject,

                        new Response.Listener() {
                            @Override
                            public void onResponse(Object response) {
                                synchronized (context){
                                        if (left_to_send>1)
                                            left_to_send = left_to_send -1;
                                        else{
                                            left_to_send = 0;
                                            is_sending =false;
                                        }
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error)
                            {
                                is_sending = false;
                                error.getCause();
                            }
                        });
                userData.requestManager.queue.add(jsonObjectRequest);
            }
        }
        return true;
    }
}
