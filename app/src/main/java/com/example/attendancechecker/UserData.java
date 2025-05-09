package com.example.attendancechecker;

import java.util.ArrayList;
import java.util.List;

public class UserData {
    int STUDENT_ID =0;
    int GROUP_ID = 1;
    int SUB_GROUP_ID = 2;
    int ADDRESS = 3;
    int FIO = 4;
    String login;
    String password;
    String role;
    int group_id;
    int self_id;
    boolean changed;
    boolean logged_in;
    List<Object[]>  studentsData;

    public UserData(String login, String password){
        changed = true;
        this.login = login;
        this.password = password;


    }
    //True uspeh , false net
    public boolean updateData(){
        if (logged_in){
            getStudentsData();
            return  true;
        }
        else{
            return false;
        }

    }
    //s-староста. t-teacher, d-director
    public boolean checkLogin(){
        this.role = "s";
        this.group_id = 2;
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
    public boolean requestWiring(String address, int student_id){
        return true;
    }
    //Получение данных о студентах
    //[0] - student_id[int]
    //[1] - group_id[int]
    //[2] - sub_group_id[int]
    //[3] - device_adress[string]
    //[4] - name[string] + surname[string] +patronymic[string]
    //TODO сделать api запросики
    public void getStudentsData(){
        this.studentsData = new ArrayList<Object[]>();
        this.studentsData.add(new Object[]{1234,1,2,"-","I am First"});
        this.studentsData.add(new Object[]{5678,1,2,"DA:4C:10:DE:17:00","I am Second"});
        this.studentsData.add(new Object[]{4444,1,2,"-","I am Third"});
        this.studentsData.add(new Object[]{5555,2,2,"DA:4C:10:DE:17:00","I am Fourth"});
    }
}
