package com.example.attendancechecker;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class UserData {
    int STUDENT_ID =0;
    int GROUP_ID = 1;
    int SUB_GROUP_ID = 2;
    int ADDRESS = 3;
    int FIO = 4;
    String login;
    String password;
    String role;
    int group_id_starosta;
    String mRequestBody;
    int self_id;
    boolean changed;
    boolean logged_in;
    boolean is_wiring;
    int cnt;
    int wiring_result;
    List<Object[]>  studentsData;
    boolean logging_in_now;
    public RequestManager requestManager;
    JSONObject jsonObject;
    public JsonArrayRequest jsonArrayRequest;
    public boolean is_updating_data;
    public JsonObjectRequest jsonObjectRequest;
    public final Context context;
    public UserData(String login, String password, RequestManager requestManager,Context context){
        this.context = context;
        changed = true;
        this.jsonObject = new  JSONObject();
        this.login = login;
        this.password = password;
        this.logged_in = false;
        this.logging_in_now = false;
        this.requestManager =requestManager;
        try{
            this.jsonObject.put("login",(String)login);
            this.jsonObject.put("password",password);
        } catch (JSONException e) {

        }

        this.mRequestBody = jsonObject.toString();


    }
    //True uspeh , false net
    public boolean updateData(){
        this.is_updating_data = true;
        if (logged_in){
            getStudentsData();
            this.changed = true;
            return  true;
        }
        else{
            is_updating_data = false;
            return false;
        }

    }

    public void makeLoginRequest(){

        logging_in_now = true;
        jsonObjectRequest
                = new JsonObjectRequest(
                Request.Method.POST,
                requestManager.url+"/api/login/",
                jsonObject,

                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        logging_in_now = false;
                        logged_in = false;
                        try {
                            self_id = Integer.parseInt(((JSONObject) response).getString("id"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            role = ((JSONObject) response).getString("role");
                            if ((role != "Староста")||(role!= "Преподователь")) logged_in = false;
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        logged_in = false;
                        logging_in_now = false;
                        error.getCause();
                    }
                });
        requestManager.queue.add(jsonObjectRequest);

    }
    //s-староста. t-teacher, d-director

    public boolean checkLogin(){
        this.role = "starosta";
        this.group_id_starosta = 2;
        this.self_id = 123456;
        this.logged_in = true;
        return true;
    }
    //Для проверки обновления данных перед проверкой посещаемости
    public boolean isChanged(){
        if (changed){
            changed = false;
            return true;
        }
        return false;
    }
    //
    //TODO Сделать запрос на попытку привязки. true если получилось. false - если нет
    public void requestWiring(String address, int student_id){
        jsonArrayRequest
                = new JsonArrayRequest(
                Request.Method.GET,
                requestManager.url+"/student-groups/",
                null,

                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        JSONArray responsed = ((JSONArray) response);
                        cnt = 0;
                        for (int i = 0; i < responsed.length(); i++) {
                            try {
                                JSONObject jsondata = (JSONObject) responsed.get(i);
                                if (jsondata.getString("device_address").equals(address)) {
                                    wiring_result = 1;
                                    is_wiring =false;
                                    return;
                                }
                                if (Integer.parseInt(jsondata.getString("student_id"))== student_id){
                                    cnt+=1;
                                }

                            } catch (JSONException e) {
                                is_wiring = false;
                                throw new RuntimeException(e);
                            }
                        }
                        for (int i = 0; i < responsed.length(); i++) {
                            try {
                                JSONObject jsondata = (JSONObject) responsed.get(i);
                                if (Integer.parseInt(jsondata.getString("student_id"))== student_id){
                                    confirmWiring(address, student_id, jsondata);
                                }

                            } catch (JSONException e) {
                                is_wiring = false;
                                throw new RuntimeException(e);
                            }
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        is_wiring = false;
                        error.getCause();
                    }
                });
        requestManager.queue.add(jsonArrayRequest);
    }   //0-Все норм. 1-Адрес занят. 2-Проблемесы с интернетом
public void confirmWiring(String address, int student_id,JSONObject body) throws JSONException {
        body.put("device_address",address) ;
        JsonObjectRequest jsonObjectRequest
                = new JsonObjectRequest(
                Request.Method.PATCH,
                requestManager.url+"/api/gr_update/",
                body,

                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                    synchronized (context){
                        cnt--;
                        if (cnt==0) is_wiring = false;
                        wiring_result = 0;
                    }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        cnt--;
                        if (cnt == 0) is_wiring = false;
                        is_wiring = false;
                        wiring_result = 3;
                        error.getCause();
                    }
                });
        requestManager.queue.add(jsonObjectRequest);
    }
    //Получение данных о студентах
    //[0] - student_id[int]
    //[1] - group_id[int]
    //[2] - sub_group_id[int]
    //[3] - device_adress[string]
    //[4] - name[string] + surname[string] +patronymic[string]

    public void getStudentsData(){
        this.is_updating_data = true;
        this.studentsData = new ArrayList<Object[]>();
        if (Objects.equals(role, "Староста")){
            getStudentsDataStarostaPhase1();
        } else if (Objects.equals(role, "Преподователь")) {
            getLessons();
        }


        //this.studentsData.add(new Object[]{1234,1,2,"-","I am First"});
        //this.studentsData.add(new Object[]{5678,1,2,"DA:4C:10:DE:17:00","I am Second"});
        //this.studentsData.add(new Object[]{4444,1,2,"-","I am Third"});
        //this.studentsData.add(new Object[]{5555,2,2,"DA:4C:10:DE:17:00","I am Fourth"});
    }
    public void getStudentNames(){
        jsonArrayRequest
                = new JsonArrayRequest(
                Request.Method.GET,
                requestManager.url+"/users/",
                null,

                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {

                        JSONArray responsed = ((JSONArray) response);
                        for (int i = 0; i<responsed.length(); i++){
                            for (int j = 0; j<studentsData.size();j++){
                                try {
                                    if ((int)studentsData.get(j)[STUDENT_ID] == Integer.parseInt( ((JSONObject)responsed.get(i)).getString("id"))){
                                        studentsData.get(j)[FIO] = ((JSONObject)responsed.get(i)).getString("name") + " " + ((JSONObject)responsed.get(i)).getString("surname") + " " + ((JSONObject)responsed.get(i)).getString("patronymic");

                                    };
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        is_updating_data = false;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        is_updating_data = false;
                        error.getCause();
                    }
                });
        requestManager.queue.add(jsonArrayRequest);

    }

    public void getGroups(HashSet<Integer> groups_ids){
        jsonArrayRequest
                = new JsonArrayRequest(
                Request.Method.GET,
                requestManager.url+"/student-groups/",
                null,

                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        JSONArray responsed = ((JSONArray) response);
                        for (int i = 0; i<responsed.length(); i++){
                            try {
                                JSONObject jsondata = (JSONObject) responsed.get(i);
                                if (groups_ids.contains(Integer.parseInt(((JSONObject)responsed.get(i)).getString("group_id")))){
                                    studentsData.add(new Object[]{
                                            Integer.parseInt((jsondata).getString("student_id")),
                                            Integer.parseInt((jsondata).getString("group_id")),
                                            Integer.parseInt((jsondata).getString("subgroup")),
                                            (jsondata).getString("device_address"),
                                            ""});
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        getStudentNames();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        is_updating_data = false;
                        error.getCause();
                    }
                });
        requestManager.queue.add(jsonArrayRequest);
    }
    public void getLessons(){
        HashSet<Integer> groups_ids = new HashSet<>();
        jsonArrayRequest
                = new JsonArrayRequest(
                Request.Method.GET,
                requestManager.url+"/lessons/",
                null,

                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        JSONArray responsed = ((JSONArray) response);
                        for (int i = 0; i<responsed.length(); i++){
                            try {
                                if (Integer.parseInt(((JSONObject)responsed.get(i)).getString("teacher_id"))==self_id){
                                    groups_ids.add(Integer.parseInt(((JSONObject)responsed.get(i)).getString("group_id")));
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        getGroups(groups_ids);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        is_updating_data = false;
                        error.getCause();
                    }
                });
        requestManager.queue.add(jsonArrayRequest);


    }
    public void getStudentsDataStarostaPhase1(){
        jsonArrayRequest
                = new JsonArrayRequest(
                Request.Method.GET,
                requestManager.url+"/student-groups/",
                null,

                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        try {
                            find_group_id((JSONArray) response);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        try {
                            getStudentsDataStarostaPhase2((JSONArray) response);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        is_updating_data = false;
                        error.getCause();
                    }
                });
        requestManager.queue.add(jsonArrayRequest);

    }
    public void getStudentsDataStarostaPhase2(JSONArray response) throws JSONException {
        JSONObject jsondata;
        for (int i = 0; i<response.length(); i++){
            jsondata = (JSONObject) response.get(i);
            if (Integer.parseInt((jsondata).getString("group_id"))==group_id_starosta){
                studentsData.add(new Object[]{
                        Integer.parseInt((jsondata).getString("student_id")),
                        group_id_starosta,
                        Integer.parseInt((jsondata).getString("subgroup")),
                        (jsondata).getString("device_address"),
                        ""});
            }


        jsonArrayRequest
                = new JsonArrayRequest(
                Request.Method.GET,
                requestManager.url+"/users/",
                null,

                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        logging_in_now = false;
                        is_updating_data = false;
                        JSONArray responsed = ((JSONArray) response);
                        for (int i = 0; i<responsed.length(); i++){
                            for (int j = 0; j<studentsData.size();j++){
                                try {
                                    if ((int)studentsData.get(j)[STUDENT_ID] == Integer.parseInt( ((JSONObject)responsed.get(i)).getString("id"))){
                                        studentsData.get(j)[FIO] = ((JSONObject)responsed.get(i)).getString("name") + " " + ((JSONObject)responsed.get(i)).getString("surname") + " " + ((JSONObject)responsed.get(i)).getString("patronymic");

                                    };
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                        is_updating_data = false;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        is_updating_data = false;
                        error.getCause();
                    }
                });
        requestManager.queue.add(jsonArrayRequest);

    }

    }
    private void find_group_id(JSONArray response) throws JSONException {
        group_id_starosta = -1;
        for (int i = 0; i<response.length(); i++){
            if (Integer.parseInt(((JSONObject) response.get(i)).getString("student_id"))==self_id){
                group_id_starosta = Integer.parseInt(((JSONObject) response.get(i)).getString("group_id"));
                return;

            }

        }


    }


}
